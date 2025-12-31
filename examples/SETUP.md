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

### Method 1: Using Environment Variable (Recommended for CI/CD)

1. Create a GitHub Personal Access Token:
   - Go to https://github.com/settings/tokens
   - Click "Generate new token (classic)"
   - Select scopes: `repo`, `codespace`
   - Generate and copy the token

2. Set the environment variable:
   ```bash
   export GITHUB_TOKEN=ghp_your_token_here
   ```

3. Add to your shell profile for persistence:
   ```bash
   echo 'export GITHUB_TOKEN=ghp_your_token_here' >> ~/.bashrc
   source ~/.bashrc
   ```

### Method 2: Using GitHub CLI Config (Recommended for Local Development)

1. Install GitHub CLI on any machine (doesn't need to be where you run Nextflow):
   ```bash
   # macOS
   brew install gh
   
   # Linux
   curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg
   echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null
   sudo apt update
   sudo apt install gh
   ```

2. Authenticate:
   ```bash
   gh auth login
   ```
   Follow the prompts to authenticate with GitHub

3. Copy the config to your Nextflow machine (if different):
   ```bash
   # The config is stored in ~/.config/gh/
   scp -r ~/.config/gh/ user@nextflow-machine:~/.config/
   ```

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
