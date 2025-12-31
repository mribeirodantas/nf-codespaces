# nf-codespaces plugin

A Nextflow plugin that enables execution of tasks in GitHub Codespaces environments.

## Features

- Execute Nextflow tasks in ephemeral GitHub Codespaces
- Automatic codespace lifecycle management (creation, execution, cleanup)
- Transparent Docker-based GitHub CLI integration (no local `gh` installation required)
- Configuration through Nextflow config files

## Prerequisites

- GitHub account with access to Codespaces
- Docker installed on the machine running Nextflow
- GitHub authentication via one of:
  - `GITHUB_TOKEN` environment variable
  - GitHub CLI config in `~/.config/gh/` (from a previous `gh auth login`)

**Note:** The plugin uses the Docker image `community.wave.seqera.io/library/pip_gh:04f2de2fa12e5bcc` which includes the GitHub CLI. You don't need to install `gh` locally.

## Building

To build the plugin:
```bash
make assemble
```

## Testing with Nextflow

The plugin can be tested without a local Nextflow installation:

1. Build and install the plugin to your local Nextflow installation: `make install`
2. Run a pipeline with the plugin: `nextflow run hello -plugins nf-codespaces@0.1.0`

## Usage

### Configuration

Add the plugin to your `nextflow.config`:

```groovy
plugins {
  id 'nf-codespaces@0.1.0'
}

process {
  executor = 'codespaces'
  ext.codespaces {
    repo = 'your-org/your-repo'
    branch = 'main'  // optional
    useLocalGh = false  // optional: set to true to use local gh CLI instead of Docker
  }
}
```

### Authentication

The plugin needs GitHub authentication to create and manage codespaces. Set it up using one of these methods:

**Option 1: Environment variable (recommended for CI/CD)**
```bash
export GITHUB_TOKEN=ghp_your_token_here
nextflow run your-pipeline.nf
```

**Option 2: GitHub CLI authentication (recommended for local development)**
```bash
# One-time setup (from a machine with gh CLI installed)
gh auth login

# The plugin will automatically use the credentials from ~/.config/gh/
nextflow run your-pipeline.nf
```

### Using Local GH CLI (Alternative)

By default, the plugin uses Docker to run GitHub CLI commands. If you prefer to use a local `gh` installation:

**Via configuration:**
```groovy
process {
  ext.codespaces {
    repo = 'your-org/your-repo'
    useLocalGh = true  // Use local gh CLI
  }
}
```

**Via environment variable:**
```bash
export NXF_CODESPACES_USE_LOCAL_GH=true
nextflow run your-pipeline.nf
```

This requires `gh` to be installed locally:
```bash
# Install gh CLI
brew install gh          # macOS
# or
sudo apt install gh      # Linux
# or
choco install gh         # Windows

# Authenticate
gh auth login
```

### How it Works

1. When a task is submitted, the plugin uses Docker to run the GitHub CLI
2. It creates or reuses an available codespace in your specified repository
3. Task files are copied to the codespace
4. The task script is executed remotely
5. Results are tracked and returned to Nextflow

All GitHub CLI operations happen inside the Docker container, making the plugin completely transparent to the user.

## Documentation

- 🚀 **[Quick Start Guide](QUICKSTART.md)** - Get running in 5 minutes
- 📚 **[Setup Guide](examples/SETUP.md)** - Detailed installation and configuration
- 🏗️ **[Architecture](ARCHITECTURE.md)** - Technical design and implementation
- 🐳 **[Docker Integration](DOCKER_INTEGRATION.md)** - How the containerized GitHub CLI works
- 💡 **[Examples](examples/)** - Sample pipelines and configurations

## Key Features Explained

### Zero Installation Complexity
Traditional approach: Install `gh` → Configure auth → Manage versions → Deal with dependencies

With nf-codespaces: Install Docker → Set auth → Done! ✅

### Transparent Docker Integration
Every `gh` command runs automatically in a Docker container:
```bash
# What you write:
gh codespace list

# What actually runs:
docker run --rm \
  -v ~/.config/gh:/root/.config/gh:ro \
  -e GITHUB_TOKEN=... \
  community.wave.seqera.io/library/pip_gh:04f2de2fa12e5bcc \
  gh codespace list
```

You never see the Docker commands - the plugin handles everything!

### Production Ready
- ✅ Automatic codespace lifecycle management
- ✅ Secure authentication (token or CLI config)
- ✅ Read-only volume mounts for security
- ✅ Automatic container cleanup
- ✅ Consistent behavior across environments

## Publishing

Plugins can be published to a central plugin registry to make them accessible to the Nextflow community. 


Follow these steps to publish the plugin to the Nextflow Plugin Registry:

1. Create a file named `$HOME/.gradle/gradle.properties`, where $HOME is your home directory. Add the following properties:

    * `pluginRegistry.accessToken`: Your Nextflow Plugin Registry access token. 

2. Use the following command to package and create a release for your plugin on GitHub: `make release`.


> [!NOTE]
> The Nextflow Pluging registry is currently avaialable as private beta technology. Contact info@nextflow.io to learn how to get access to it.
> 