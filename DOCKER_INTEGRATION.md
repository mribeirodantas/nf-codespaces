# Docker-Based GitHub CLI Integration

## Summary

The nf-codespaces plugin now uses a Docker-based approach to run the GitHub CLI, eliminating the need for users to install `gh` on their local machines. All GitHub operations are transparently executed inside the Docker container `community.wave.seqera.io/library/pip_gh:04f2de2fa12e5bcc`.

## What Changed

### Before
Users needed to:
- Install GitHub CLI (`gh`) locally
- Configure authentication
- Ensure correct version and dependencies

### After
Users only need:
- Docker installed
- GitHub authentication (token or `~/.config/gh/` directory)

The plugin handles everything else automatically!

## Key Implementation Details

### 1. New Helper Methods

**`executeGhCommand(List<String> ghArgs)`**
- Runs any `gh` command inside Docker
- Mounts `~/.config/gh` for authentication
- Passes `GITHUB_TOKEN` environment variable

**`executeGhCommandWithMount(List<String> ghArgs, String localPath)`**
- Same as above, plus mounts local directories for file operations
- Used for `gh codespace cp` commands

### 2. Updated Methods

All methods that previously called `gh` directly now use the Docker-based helpers:
- `getOrCreateCodespace()` - List and create codespaces
- `copyTaskFiles()` - Copy files to codespaces
- `executeInCodespaceSync()` - Execute SSH commands

### 3. Docker Container

**Image**: `community.wave.seqera.io/library/pip_gh:04f2de2fa12e5bcc`

**Contains**:
- GitHub CLI (`gh`)
- Python with pip
- Standard utilities

**Benefits**:
- Fast pulls via Wave registry
- Consistent version across environments
- No local installation needed

## How It Works

### Example: Listing Codespaces

**Old approach** (required local `gh`):
```groovy
def listCmd = ['gh', 'codespace', 'list', '--json', 'name,state']
def result = executeCommand(listCmd)  // Runs gh directly on host
```

**New approach** (Docker-based):
```groovy
def listCmd = ['gh', 'codespace', 'list', '--json', 'name,state']
def result = executeGhCommand(listCmd)  // Runs gh in Docker container
```

Behind the scenes:
```bash
docker run --rm \
  -v ~/.config/gh:/root/.config/gh:ro \
  -e GITHUB_TOKEN=... \
  community.wave.seqera.io/library/pip_gh:04f2de2fa12e5bcc \
  gh codespace list --json name,state
```

### Example: Copying Files

**With volume mount**:
```groovy
def cpCmd = ['gh', 'codespace', 'cp', '-r', '-c', name, workDir, remotePath]
executeGhCommandWithMount(cpCmd, workDir)
```

Becomes:
```bash
docker run --rm \
  -v ~/.config/gh:/root/.config/gh:ro \
  -v /path/to/workDir:/path/to/workDir:ro \
  -e GITHUB_TOKEN=... \
  community.wave.seqera.io/library/pip_gh:04f2de2fa12e5bcc \
  gh codespace cp -r -c name /path/to/workDir remote:/path
```

## Authentication

Two methods work seamlessly:

### Method 1: Environment Variable
```bash
export GITHUB_TOKEN=ghp_your_token
nextflow run pipeline.nf
```

The plugin passes this to the Docker container via `-e GITHUB_TOKEN=...`

### Method 2: GitHub CLI Config
```bash
gh auth login  # One-time setup
nextflow run pipeline.nf
```

The plugin mounts `~/.config/gh` into the container at `/root/.config/gh`

## Benefits

### For Users
✅ No need to install `gh` CLI  
✅ No version conflicts  
✅ Works on any system with Docker  
✅ Consistent behavior across environments  
✅ Automatic cleanup (containers are ephemeral)  

### For Developers
✅ Easier testing (just need Docker)  
✅ Version pinning via container tag  
✅ Better isolation  
✅ Simpler CI/CD setup  

### For Enterprises
✅ Reduced deployment complexity  
✅ Better security (read-only mounts)  
✅ Easier auditing (all operations in containers)  
✅ Can use private registries  

## Testing the Changes

### 1. Verify Docker Integration
```bash
# Check if image is pulled
docker images | grep pip_gh

# Should show:
# community.wave.seqera.io/library/pip_gh   04f2de2fa12e5bcc   ...
```

### 2. Test GitHub Authentication
```bash
# Test with environment variable
export GITHUB_TOKEN=ghp_your_token
docker run --rm \
  -e GITHUB_TOKEN=$GITHUB_TOKEN \
  community.wave.seqera.io/library/pip_gh:04f2de2fa12e5bcc \
  gh auth status

# Test with config directory
docker run --rm \
  -v ~/.config/gh:/root/.config/gh:ro \
  community.wave.seqera.io/library/pip_gh:04f2de2fa12e5bcc \
  gh auth status
```

### 3. Run Example Pipeline
```bash
cd nf-codespaces
make install
cd examples
nextflow run hello-codespaces.nf
```

## Troubleshooting

### "docker: command not found"
**Solution**: Install Docker from https://docs.docker.com/get-docker/

### "permission denied" with Docker
**Solution**: 
```bash
sudo usermod -aG docker $USER
newgrp docker
```

### "Failed to authenticate with GitHub"
**Solution**: Set `GITHUB_TOKEN` or ensure `~/.config/gh/` exists and contains valid auth

### Container pull is slow
**Solution**: Wave registry should be fast, but if slow:
```bash
# Pre-pull the image
docker pull community.wave.seqera.io/library/pip_gh:04f2de2fa12e5bcc
```

## Migration Guide

If you were using the old version that required local `gh` installation:

### Before (v0.0.x)
1. Install `gh`: `brew install gh` or `apt install gh`
2. Authenticate: `gh auth login`
3. Use plugin: `nextflow run pipeline.nf`

### After (v0.1.0+)
1. Ensure Docker is installed: `docker --version`
2. Set authentication (choose one):
   - `export GITHUB_TOKEN=ghp_...` OR
   - Already have `~/.config/gh/` from previous `gh auth login`
3. Use plugin: `nextflow run pipeline.nf`

**That's it!** The plugin now handles `gh` CLI via Docker automatically.

## Future Enhancements

Planned improvements:
- [ ] Support custom Docker images with `gh`
- [ ] Optimize mount strategies for better performance
- [ ] Add Docker socket passthrough for nested Docker scenarios
- [ ] Support Docker Compose for complex setups
- [ ] Add telemetry for Docker operation timing

## Files Modified

- `src/main/groovy/seqera/plugin/CodespacesTaskHandler.groovy`
  - Added `executeGhCommand()` method
  - Added `executeGhCommandWithMount()` method
  - Updated all `gh` command calls to use Docker

- `README.md`
  - Updated prerequisites
  - Added Docker-based authentication guide
  - Clarified no local `gh` installation needed

- `examples/SETUP.md` (new)
  - Comprehensive setup guide
  - Authentication methods
  - Troubleshooting

- `ARCHITECTURE.md` (new)
  - Technical documentation
  - Docker integration details
  - Security considerations

## Credits

Docker image maintained by Seqera Labs via Wave Container Registry.

## Questions?

- Check the [ARCHITECTURE.md](ARCHITECTURE.md) for technical details
- See [examples/SETUP.md](examples/SETUP.md) for setup help
- Open an issue on GitHub for support
