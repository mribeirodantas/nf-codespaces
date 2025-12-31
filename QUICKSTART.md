# Quick Start Guide - nf-codespaces

Get started with nf-codespaces in 5 minutes! 🚀

## Prerequisites Check

Run these commands to verify you're ready:

```bash
# 1. Check Docker is installed
docker --version
# ✓ Should show: Docker version 20.x or higher

# 2. Check Docker is running
docker ps
# ✓ Should show: Container list (even if empty)

# 3. Check GitHub authentication (choose ONE method)

# Method A: Token-based (recommended for CI/CD)
echo $GITHUB_TOKEN
# ✓ Should show: ghp_xxxxxxxxxxxxx

# Method B: GitHub CLI config (recommended for local dev)
ls ~/.config/gh/hosts.yml
# ✓ Should exist if you've run 'gh auth login' before
```

If any checks fail, see the [Setup Guide](examples/SETUP.md).

## Installation

```bash
# Clone and build the plugin
git clone https://github.com/mribeirodantas/nf-codespaces.git
cd nf-codespaces
make install
```

## Your First Pipeline

### 1. Create a simple test pipeline

Create `test.nf`:
```groovy
#!/usr/bin/env nextflow
nextflow.enable.dsl = 2

process sayHello {
    output:
    stdout

    script:
    """
    echo "Hello from Codespace!"
    hostname
    """
}

workflow {
    sayHello() | view
}
```

### 2. Create configuration

Create `nextflow.config`:
```groovy
plugins {
  id 'nf-codespaces@0.1.0'
}

process {
  executor = 'codespaces'
  ext.codespaces {
    repo = 'YOUR-USERNAME/YOUR-REPO'  // ⚠️  CHANGE THIS!
    branch = 'main'
  }
}
```

**Important**: Replace `YOUR-USERNAME/YOUR-REPO` with a GitHub repository you have access to!

### 3. Set GitHub authentication

Choose one:

```bash
# Option A: Set token (if you have one)
export GITHUB_TOKEN=ghp_your_token_here

# Option B: Use existing gh config (if you've used 'gh auth login' before)
# Nothing to do - plugin will auto-detect ~/.config/gh/
```

### 4. Run!

```bash
nextflow run test.nf
```

### 5. What happens?

```
N E X T F L O W  ~  version 24.10.4
Launching `test.nf` [...]

executor >  codespaces (1)
[xx/yyyyyy] process > sayHello [100%] 1 of 1 ✔

Hello from Codespace!
codespace-friendly-computing-machine

Completed at: ...
Success: true
```

🎉 **Success!** Your task ran in a GitHub Codespace!

## Behind the Scenes

Here's what just happened:

1. **Plugin loaded**: Nextflow loaded nf-codespaces executor
2. **Docker pulled**: Downloaded `community.wave.seqera.io/library/pip_gh:04f2de2fa12e5bcc`
3. **Codespace created**: Plugin ran `docker run ... gh codespace create ...`
4. **Files copied**: Transferred task files to codespace
5. **Task executed**: Ran your script in the codespace
6. **Results returned**: Brought back outputs to your local machine

**No local `gh` CLI was needed!** Everything runs in Docker containers. ✨

## Try More Examples

```bash
cd examples
nextflow run hello-codespaces.nf
```

## Common Issues

### "Error: Failed to create codespace"

**Cause**: Authentication or repository access issue

**Fix**:
```bash
# Test authentication manually
docker run --rm \
  -e GITHUB_TOKEN=$GITHUB_TOKEN \
  community.wave.seqera.io/library/pip_gh:04f2de2fa12e5bcc \
  gh auth status

# Or with config directory
docker run --rm \
  -v ~/.config/gh:/root/.config/gh:ro \
  community.wave.seqera.io/library/pip_gh:04f2de2fa12e5bcc \
  gh auth status
```

### "Error: docker: command not found"

**Cause**: Docker not installed

**Fix**: Install Docker from https://docs.docker.com/get-docker/

### "Error: permission denied" with Docker

**Cause**: Your user doesn't have Docker permissions

**Fix**:
```bash
sudo usermod -aG docker $USER
newgrp docker
```

### Task seems stuck

**Cause**: Codespace creation can take 1-2 minutes

**Fix**: Be patient on first run! Watch for:
```
Creating codespace for user/repo...
```

## Next Steps

### Learn More
- 📚 [Full Setup Guide](examples/SETUP.md)
- 🏗️ [Architecture Overview](ARCHITECTURE.md)  
- 🐳 [Docker Integration Details](DOCKER_INTEGRATION.md)

### Customize Your Setup
- Configure per-process executors
- Use different codespace configurations
- Optimize for your use case

### Get Help
- Open an issue: https://github.com/mribeirodantas/nf-codespaces/issues
- Read the docs: https://www.nextflow.io/docs/latest/executor.html
- Ask the community: https://nextflow.slack.com

## Configuration Reference

### Minimal Config
```groovy
plugins {
  id 'nf-codespaces@0.1.0'
}

process {
  executor = 'codespaces'
  ext.codespaces.repo = 'user/repo'
  // Optional: Use local gh CLI instead of Docker
  // ext.codespaces.useLocalGh = true
}
```

### Process-Specific Config
```groovy
process {
  // Default: run locally
  executor = 'local'
  
  // But heavy tasks go to codespaces
  withName: 'BIG_COMPUTE' {
    executor = 'codespaces'
    ext.codespaces {
      repo = 'org/compute-repo'
      branch = 'main'
    }
  }
}
```

### Multiple Repositories
```groovy
process {
  withName: 'TASK_A' {
    executor = 'codespaces'
    ext.codespaces.repo = 'org/repo-a'
  }
  
  withName: 'TASK_B' {
    executor = 'codespaces'
    ext.codespaces.repo = 'org/repo-b'
  }
}
```

## Performance Tips

### 1. Pre-pull Docker Image
```bash
docker pull community.wave.seqera.io/library/pip_gh:04f2de2fa12e5bcc
```

### 2. Reuse Codespaces
The plugin automatically reuses available codespaces - no action needed!

### 3. Keep Codespaces Running
Available codespaces start tasks immediately. Delete old ones when done:
```bash
gh codespace list
gh codespace delete -c <codespace-name>
```

## Cleanup

When you're done testing:

```bash
# List your codespaces
gh codespace list

# Delete them
gh codespace delete -c <name>

# Or delete all
gh codespace delete --all
```

---

**Happy Nextflow-ing in the Cloud!** ☁️🧬
