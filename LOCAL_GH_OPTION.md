# Using Local GH CLI Installation

## Overview

By default, the nf-codespaces plugin uses **Docker** to run GitHub CLI commands. This approach requires no local `gh` installation and works consistently across all platforms.

However, you can optionally configure the plugin to use a **local `gh` CLI installation** if you prefer. This document explains both options and when to use each.

## Comparison: Docker vs Local GH

### Docker-Based (Default) ✅ Recommended

**Pros:**
- ✅ No need to install `gh` locally
- ✅ Consistent behavior across environments
- ✅ Version pinned via container tag
- ✅ Easier CI/CD setup
- ✅ No version conflicts
- ✅ Automatic container cleanup

**Cons:**
- ❌ Requires Docker installed
- ❌ Slight overhead for container startup (~1s)
- ❌ Requires internet for first pull

**Use when:**
- You don't want to manage `gh` CLI versions
- You're running in CI/CD
- You want guaranteed consistency
- Docker is already available

### Local GH CLI Installation

**Pros:**
- ✅ No Docker required
- ✅ Faster execution (no container overhead)
- ✅ Direct access to local `gh` features
- ✅ Can use custom `gh` extensions

**Cons:**
- ❌ Requires manual `gh` installation
- ❌ Version management needed
- ❌ OS-specific installation steps
- ❌ Potential version conflicts

**Use when:**
- Docker is not available
- You already have `gh` installed and configured
- You need maximum performance
- You're using custom `gh` extensions

## Configuration Methods

### Method 1: Configuration File

Edit your `nextflow.config`:

```groovy
plugins {
  id 'nf-codespaces@0.1.0'
}

process {
  executor = 'codespaces'
  ext.codespaces {
    repo = 'your-org/your-repo'
    useLocalGh = true  // Enable local gh CLI
  }
}
```

### Method 2: Environment Variable

Set the environment variable (overrides config):

```bash
export NXF_CODESPACES_USE_LOCAL_GH=true
nextflow run pipeline.nf
```

### Method 3: Per-Process Configuration

Use local `gh` for specific processes only:

```groovy
process {
  // Default: use Docker
  executor = 'codespaces'
  ext.codespaces {
    repo = 'your-org/your-repo'
    useLocalGh = false
  }
  
  // But use local gh for specific process
  withName: 'SPECIFIC_TASK' {
    ext.codespaces.useLocalGh = true
  }
}
```

## Setup Instructions

### Installing Local GH CLI

#### macOS
```bash
brew install gh
gh auth login
```

#### Linux (Debian/Ubuntu)
```bash
# Add GitHub's package repository
curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | \
  sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg

echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | \
  sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null

# Install
sudo apt update
sudo apt install gh

# Authenticate
gh auth login
```

#### Linux (Fedora/CentOS/RHEL)
```bash
sudo dnf install gh
gh auth login
```

#### Windows
```powershell
# Using Chocolatey
choco install gh

# Or using Scoop
scoop install gh

# Authenticate
gh auth login
```

### Verify Installation

```bash
# Check gh is installed
gh --version

# Check authentication
gh auth status

# Test codespace access
gh codespace list
```

## Examples

### Example 1: Docker-Based (Default)

**Configuration:**
```groovy
process {
  executor = 'codespaces'
  ext.codespaces.repo = 'user/repo'
  // useLocalGh not set, defaults to false
}
```

**Requirements:**
- Docker installed
- GitHub authentication (token or config)

**Execution:**
```bash
nextflow run pipeline.nf
```

Plugin uses: `docker run ... gh codespace ...`

### Example 2: Local GH CLI

**Configuration:**
```groovy
process {
  executor = 'codespaces'
  ext.codespaces {
    repo = 'user/repo'
    useLocalGh = true
  }
}
```

**Requirements:**
- `gh` CLI installed locally
- GitHub authentication (`gh auth login`)

**Execution:**
```bash
nextflow run pipeline.nf
```

Plugin uses: `gh codespace ...` (directly)

### Example 3: Environment Variable Override

**Configuration:**
```groovy
process {
  executor = 'codespaces'
  ext.codespaces.repo = 'user/repo'
  // useLocalGh = false (default)
}
```

**Execution with override:**
```bash
# Force local gh usage
export NXF_CODESPACES_USE_LOCAL_GH=true
nextflow run pipeline.nf
```

Plugin uses: `gh codespace ...` (environment variable takes precedence)

### Example 4: Mixed Strategy

**Configuration:**
```groovy
process {
  // Default for all processes: Docker
  executor = 'codespaces'
  ext.codespaces {
    repo = 'user/repo'
    useLocalGh = false
  }
  
  // Fast tasks: use local gh for speed
  withName: 'QUICK_CHECK' {
    ext.codespaces.useLocalGh = true
  }
  
  // Heavy tasks: use Docker for consistency
  withName: 'HEAVY_COMPUTE' {
    ext.codespaces.useLocalGh = false
  }
}
```

## Troubleshooting

### "gh: command not found" (with useLocalGh = true)

**Problem:** Local `gh` is not installed or not in PATH

**Solution:**
```bash
# Check if gh is installed
which gh

# If not found, install it (see setup instructions above)

# Verify PATH includes gh location
echo $PATH
```

### "gh auth: not authenticated" (with useLocalGh = true)

**Problem:** GitHub authentication not configured

**Solution:**
```bash
# Authenticate with GitHub
gh auth login

# Verify authentication
gh auth status

# Check codespace access
gh codespace list
```

### Docker vs Local Performance

**Benchmark** (typical results):

| Operation | Docker | Local GH | Difference |
|-----------|--------|----------|------------|
| First run | ~5s | ~2s | -3s (Docker pull) |
| Subsequent runs | ~2s | ~1s | -1s (container startup) |
| List codespaces | ~2s | ~1s | -1s |
| Create codespace | ~90s | ~88s | -2s (network bound) |
| SSH command | ~2s | ~1s | -1s |

**Recommendation:** Docker overhead is minimal, use Docker unless you have specific performance requirements.

### Switching Between Methods

You can switch freely between Docker and local `gh`:

```bash
# Use Docker
nextflow run pipeline.nf

# Switch to local gh
export NXF_CODESPACES_USE_LOCAL_GH=true
nextflow run pipeline.nf

# Switch back to Docker
unset NXF_CODESPACES_USE_LOCAL_GH
nextflow run pipeline.nf
```

## Decision Guide

### Choose **Docker-Based** if:
- ✅ You don't have `gh` installed and don't want to install it
- ✅ You want guaranteed consistency across environments
- ✅ You're running in CI/CD
- ✅ You want the easiest setup
- ✅ Docker is already available

### Choose **Local GH** if:
- ✅ Docker is not available in your environment
- ✅ You already have `gh` installed and configured
- ✅ You need maximum performance (every second counts)
- ✅ You're using custom `gh` extensions
- ✅ Your organization mandates local tools

### Still Unsure?

**Default recommendation: Use Docker** (leave `useLocalGh = false` or unset)

Reasons:
1. Easier setup (no gh installation needed)
2. More consistent (same version everywhere)
3. Better for CI/CD
4. Minimal performance difference
5. No version management needed

## FAQ

**Q: Can I use both Docker and local gh in the same pipeline?**  
A: Yes! Configure per-process using `withName` selectors.

**Q: Which method is faster?**  
A: Local `gh` is ~1-2 seconds faster per operation, but Docker overhead is minimal.

**Q: Does the environment variable override the config file?**  
A: Yes, `NXF_CODESPACES_USE_LOCAL_GH` takes precedence.

**Q: What if I have both Docker and local gh available?**  
A: Use Docker (default) unless you specifically need local `gh` features.

**Q: Can I switch between methods without changing code?**  
A: Yes, use the environment variable: `export NXF_CODESPACES_USE_LOCAL_GH=true`

**Q: What happens if useLocalGh=true but gh is not installed?**  
A: You'll get an error: "gh: command not found". Install `gh` or switch to Docker.

**Q: Does this affect authentication?**  
A: No, both methods support the same authentication (token or `~/.config/gh/`).

**Q: Which method should I use in CI/CD?**  
A: Docker is recommended for CI/CD (more consistent, easier setup).

**Q: Can I force Docker even if useLocalGh=true?**  
A: Yes, set `NXF_CODESPACES_USE_LOCAL_GH=false` in environment (overrides config).

## Best Practices

1. **Default to Docker** unless you have specific reasons to use local `gh`
2. **Document your choice** in your pipeline README
3. **Use environment variables** for easy switching without code changes
4. **Test both methods** if you expect users with different setups
5. **Keep gh updated** if using local installation
6. **Use process-specific config** for mixed strategies

## Related Documentation

- [QUICKSTART.md](QUICKSTART.md) - Getting started guide
- [DOCKER_INTEGRATION.md](DOCKER_INTEGRATION.md) - Docker implementation details
- [examples/SETUP.md](examples/SETUP.md) - Comprehensive setup instructions
- [GitHub CLI Documentation](https://cli.github.com/manual/)

## Summary

| Aspect | Docker (Default) | Local GH |
|--------|------------------|----------|
| **Setup** | Install Docker only | Install Docker + gh |
| **Performance** | ~1-2s overhead | Fastest |
| **Consistency** | Perfect | Varies by version |
| **CI/CD** | Excellent | Good |
| **Maintenance** | Zero | Manual updates |
| **Recommendation** | ✅ Use this | Only if needed |

**Bottom line:** Stick with Docker unless you have a specific reason to use local `gh`.
