# nf-codespaces Architecture

This document explains the design and implementation of the nf-codespaces plugin, with a focus on the Docker-based GitHub CLI integration.

## Overview

The nf-codespaces plugin extends Nextflow to execute tasks in GitHub Codespaces environments. The key innovation is using a containerized GitHub CLI, eliminating the need for users to install `gh` locally.

## Component Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Nextflow Core                            │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              CodespacesExecutor                          │  │
│  │  • Implements Executor interface                         │  │
│  │  • Creates TaskHandlers                                  │  │
│  │  • Manages task monitoring                               │  │
│  └──────────────────────────┬───────────────────────────────┘  │
│                             │                                   │
│  ┌──────────────────────────▼───────────────────────────────┐  │
│  │          CodespacesTaskHandler                           │  │
│  │  • Manages task lifecycle                                │  │
│  │  • Orchestrates codespace operations                     │  │
│  │  • Handles file transfer                                 │  │
│  └──────────────────────────┬───────────────────────────────┘  │
│                             │                                   │
└─────────────────────────────┼───────────────────────────────────┘
                              │
        ┌─────────────────────▼─────────────────────┐
        │   Docker-based GitHub CLI Integration     │
        │                                            │
        │  executeGhCommand(ghArgs)                  │
        │  executeGhCommandWithMount(ghArgs, path)   │
        └─────────────────────┬──────────────────────┘
                              │
        ┌─────────────────────▼──────────────────────┐
        │         Docker Container                   │
        │  Image: community.wave.seqera.io/         │
        │         library/pip_gh:04f2de2fa12e5bcc    │
        │                                            │
        │  • GitHub CLI (gh)                         │
        │  • Python with pip                         │
        │  • Mounts: ~/.config/gh, working dirs      │
        └─────────────────────┬──────────────────────┘
                              │
        ┌─────────────────────▼──────────────────────┐
        │          GitHub Codespaces                 │
        │  • Remote execution environment            │
        │  • Task processing                         │
        │  • Managed via gh CLI                      │
        └────────────────────────────────────────────┘
```

## Docker-Based GitHub CLI Integration

### Motivation

The traditional approach would require users to:
1. Install the GitHub CLI (`gh`) on their system
2. Configure authentication
3. Ensure correct versions and dependencies

By using Docker, we achieve:
- **Zero local installation**: No need to install `gh` CLI
- **Version consistency**: Always use the same `gh` version
- **Portability**: Works on any system with Docker
- **Isolation**: No conflicts with system packages

### Implementation

The plugin uses two helper methods to execute GitHub CLI commands:

#### 1. `executeGhCommand(List<String> ghArgs)`

For standard `gh` operations (list, create, ssh):

```groovy
private Map executeGhCommand(List<String> ghArgs) {
    def dockerCmd = [
        'docker', 'run', '--rm',
        '-v', "${System.getProperty('user.home')}/.config/gh:/root/.config/gh:ro",
        '-e', "GITHUB_TOKEN=${System.getenv('GITHUB_TOKEN') ?: ''}",
        'community.wave.seqera.io/library/pip_gh:04f2de2fa12e5bcc'
    ] + ghArgs
    
    return executeCommand(dockerCmd)
}
```

**Key features:**
- Mounts `~/.config/gh` for GitHub CLI authentication
- Passes `GITHUB_TOKEN` environment variable
- Uses Wave container registry for fast, reproducible pulls
- `--rm` ensures cleanup after execution

#### 2. `executeGhCommandWithMount(List<String> ghArgs, String localPath)`

For file operations requiring access to local directories:

```groovy
private Map executeGhCommandWithMount(List<String> ghArgs, String localPath) {
    def dockerCmd = [
        'docker', 'run', '--rm',
        '-v', "${System.getProperty('user.home')}/.config/gh:/root/.config/gh:ro",
        '-v', "${localPath}:${localPath}:ro",
        '-e', "GITHUB_TOKEN=${System.getenv('GITHUB_TOKEN') ?: ''}",
        'community.wave.seqera.io/library/pip_gh:04f2de2fa12e5bcc'
    ] + ghArgs
    
    return executeCommand(dockerCmd)
}
```

**Additional features:**
- Mounts the local work directory for file copying
- Read-only mounts for security
- Preserves file paths for `gh codespace cp` commands

### Authentication Flow

```
User Authentication
    │
    ├─── Option 1: GITHUB_TOKEN env var
    │    └──> Passed to Docker container via -e flag
    │
    └─── Option 2: ~/.config/gh/ directory
         └──> Mounted into Docker container at /root/.config/gh
```

Both authentication methods work transparently with the Docker container.

## Task Execution Flow

1. **Task Submission**
   ```
   submit() → getOrCreateCodespace()
           → executeGhCommand(['gh', 'codespace', 'list', ...])
   ```

2. **Codespace Creation (if needed)**
   ```
   executeGhCommand(['gh', 'codespace', 'create', '-r', repo])
   ```

3. **File Transfer**
   ```
   copyTaskFiles() → executeInCodespaceSync("mkdir ...")
                   → executeGhCommandWithMount(['gh', 'codespace', 'cp', ...])
   ```

4. **Remote Execution**
   ```
   executeInCodespace() → executeGhCommand(['gh', 'codespace', 'ssh', ...])
   ```

5. **Status Monitoring**
   ```
   checkIfCompleted() → Check for .exitcode file
   ```

## Docker Image Details

### Image Source
- **Registry**: Wave Container Registry (community.wave.seqera.io)
- **Image**: `library/pip_gh:04f2de2fa12e5bcc`
- **Contents**: 
  - GitHub CLI (`gh`)
  - Python with pip
  - Standard Linux utilities

### Why Wave Registry?

Wave provides:
- Fast container pulls via optimization
- Reproducible builds with content-addressable tags
- Integration with Seqera ecosystem
- Public availability without authentication

## Security Considerations

1. **Read-only Mounts**: Configuration directories are mounted read-only (`ro`) to prevent modification
2. **Minimal Privileges**: Docker containers run with minimal required permissions
3. **Token Scope**: GitHub tokens should have minimal required scopes (`repo`, `codespace`)
4. **Container Cleanup**: `--rm` ensures containers are removed after execution
5. **No Token Persistence**: Tokens are passed via environment variables, not stored

## Performance Optimizations

1. **Image Caching**: Docker caches the `gh` image after first pull
2. **Codespace Reuse**: Existing available codespaces are reused when possible
3. **Parallel Operations**: Multiple tasks can share the same codespace

## Future Enhancements

### Planned Features

1. **Custom Docker Images**: Support for user-provided images with `gh`
2. **SSH Key Authentication**: Alternative to token-based auth
3. **Codespace Pooling**: Pre-warmed codespace pool for faster task startup
4. **Advanced Monitoring**: Real-time task progress tracking
5. **Cost Optimization**: Automatic codespace cleanup and size selection

### Potential Improvements

1. **Buildless Mode**: Use Wave to augment images on-the-fly
2. **Multi-Codespace**: Parallel execution across multiple codespaces
3. **Hybrid Execution**: Mix local and codespace execution intelligently
4. **GitHub Actions Integration**: Direct integration with Actions runners

## Debugging

### Enable Docker Logging

```bash
export NXF_DEBUG=true
nextflow run pipeline.nf
```

### Check Docker Operations

```bash
# Monitor Docker containers
docker ps -a | grep pip_gh

# Check Docker logs
docker logs <container_id>
```

### Verify Authentication

```bash
# Test GitHub CLI auth in Docker
docker run --rm \
  -v ~/.config/gh:/root/.config/gh:ro \
  community.wave.seqera.io/library/pip_gh:04f2de2fa12e5bcc \
  gh auth status
```

## Contributing

To modify the Docker integration:

1. Edit `CodespacesTaskHandler.groovy`
2. Update `executeGhCommand()` or `executeGhCommandWithMount()` methods
3. Test with `make test`
4. Document changes in this file

## References

- [GitHub CLI Documentation](https://cli.github.com/manual/)
- [GitHub Codespaces API](https://docs.github.com/en/rest/codespaces)
- [Docker Run Reference](https://docs.docker.com/engine/reference/run/)
- [Nextflow Executor API](https://www.nextflow.io/docs/latest/executor.html)
- [Wave Containers](https://www.seqera.io/wave/)
