# Setup Guide for nf-codespaces

This guide walks you through setting up and testing the nf-codespaces plugin.

## Prerequisites

1. **Docker installed**: The plugin uses Docker to run the GitHub CLI
   ```bash
   docker --version
   ```

2. **GitHub account with Codespaces access**: Ensure you have access to GitHub Codespaces

3. **GitHub authentication**: Choose one of the following methods

## Authentication Setup

**📖 For detailed step-by-step instructions, see the [Authentication Guide](../AUTHENTICATION.md)**

The Authentication Guide covers:
- Creating GitHub Personal Access Tokens with screenshots
- Setting up GitHub CLI authentication
- CI/CD integration (GitHub Actions, GitLab CI, Jenkins)
- Troubleshooting common issues
- Security best practices

### Quick Setup

**Method 1: Using Environment Variable (CI/CD)**
```bash
# 1. Get token from: https://github.com/settings/tokens
#    (Select scopes: 'repo' and 'codespace')
# 2. Set environment variable
export GITHUB_TOKEN=ghp_your_token_here
# 3. Make it persistent
echo 'export GITHUB_TOKEN=ghp_your_token_here' >> ~/.bashrc
source ~/.bashrc
```

**Method 2: Using GitHub CLI (Local Development)**
```bash
# 1. Install gh CLI (see https://github.com/cli/cli#installation)
brew install gh  # macOS
# or
sudo apt install gh  # Linux

# 2. Authenticate
gh auth login

# 3. Verify
gh auth status
```

For complete instructions, troubleshooting, and CI/CD setup, see **[Authentication Guide](../AUTHENTICATION.md)**

## Testing the Plugin

### 1. Build and Install

```bash
cd nf-codespaces
make install
```

### 2. Configure Your Pipeline

Create or modify your `nextflow.config`:

```groovy
plugins {
  id 'nf-codespaces@0.1.0'
}

process {
  executor = 'codespaces'
  ext.codespaces {
    repo = 'your-username/your-repo'  // Change this to your repo
    branch = 'main'
  }
}
```

### 3. Test with Hello Example

```bash
cd examples
nextflow run hello-codespaces.nf -c nextflow.config
```

### 4. Verify Docker Integration

Check that the plugin is using Docker correctly:

```bash
# This should show the gh container being pulled/used
docker images | grep pip_gh
```

Expected output:
```
community.wave.seqera.io/library/pip_gh   04f2de2fa12e5bcc   ...
```

## Troubleshooting

### Error: "Failed to create codespace"

- **Check authentication**: Verify `$GITHUB_TOKEN` is set or `~/.config/gh/` exists
- **Check repository access**: Ensure you have access to the specified repository
- **Check Codespaces quota**: You may have reached your Codespaces usage limit

### Error: "docker: command not found"

- Install Docker: https://docs.docker.com/get-docker/
- Ensure Docker daemon is running: `docker ps`

### Error: "Permission denied" when accessing Docker

- Add your user to the docker group:
  ```bash
  sudo usermod -aG docker $USER
  newgrp docker
  ```

### Debugging Docker Commands

To see what commands are being executed, check the Nextflow log:

```bash
nextflow run your-pipeline.nf -with-trace -with-report
```

## Advanced Configuration

### Using Different Codespace Configurations per Process

```groovy
process {
  withName: 'HEAVY_COMPUTE' {
    executor = 'codespaces'
    ext.codespaces {
      repo = 'org/compute-repo'
      branch = 'main'
    }
  }
  
  withName: 'QUICK_TASK' {
    executor = 'local'
  }
}
```

### Custom Docker Registry

If your organization uses a private Docker registry, you can modify the image used:

Edit `CodespacesTaskHandler.groovy` and change:
```groovy
'community.wave.seqera.io/library/pip_gh:04f2de2fa12e5bcc'
```

to your custom image that includes `gh` CLI.

## Next Steps

- Learn about [GitHub Codespaces](https://docs.github.com/en/codespaces)
- Explore [Nextflow executors](https://www.nextflow.io/docs/latest/executor.html)
- Contribute to the plugin: https://github.com/mribeirodantas/nf-codespaces
