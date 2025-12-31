# Changelog

All notable changes to the nf-codespaces plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2025-12-31

### Added
- Docker-based GitHub CLI integration for zero local installation (default)
- Optional support for local `gh` CLI installation via `useLocalGh` configuration
- Environment variable `NXF_CODESPACES_USE_LOCAL_GH` to override configuration
- Transparent execution of `gh` commands inside containers
- Support for two authentication methods (GITHUB_TOKEN and ~/.config/gh/)
- Comprehensive documentation suite:
  - Quick Start Guide
  - Setup Guide
  - Architecture documentation
  - Docker integration details
- Example pipelines and configurations
- Volume mounting for file operations
- Automatic container cleanup after operations

### Changed
- **BREAKING**: Now requires Docker instead of local `gh` CLI installation
- All `gh` commands now execute inside `community.wave.seqera.io/library/pip_gh:04f2de2fa12e5bcc` container
- Improved security with read-only volume mounts
- Enhanced error handling for Docker operations

### Technical Details
- Added `executeGhCommand()` method for standard GitHub CLI operations
- Added `executeGhCommandWithMount()` method for file transfer operations
- Updated `getOrCreateCodespace()` to use Docker-based execution
- Updated `copyTaskFiles()` to mount directories for file copying
- Updated `executeInCodespaceSync()` to use Docker for SSH commands

### Configuration Options
- `ext.codespaces.useLocalGh` - Boolean to enable local `gh` CLI (default: false)
- `NXF_CODESPACES_USE_LOCAL_GH` - Environment variable override for useLocalGh
- Per-process configuration support via `withName` selectors

### Documentation
- Added QUICKSTART.md - 5-minute getting started guide
- Added examples/SETUP.md - Comprehensive setup instructions
- Added ARCHITECTURE.md - Technical design documentation
- Added DOCKER_INTEGRATION.md - Docker implementation details
- Added LOCAL_GH_OPTION.md - Guide for local gh CLI usage
- Added examples/hello-codespaces.nf - Example pipeline
- Added examples/nextflow.config - Example configuration
- Enhanced README.md with Docker integration details and local gh option

### Security
- All configuration directories mounted as read-only
- GitHub tokens passed via environment variables (not persisted)
- Container cleanup with `--rm` flag
- Minimal required token scopes documented

### Performance
- Docker image caching after first pull
- Efficient volume mounting strategies
- Codespace reuse when available

## [0.0.1] - Initial Development

### Added
- Initial plugin structure
- Basic codespace executor implementation
- Task handler with codespace lifecycle management
- Support for codespace creation and execution
- File transfer capabilities

### Known Limitations
- Required local `gh` CLI installation (addressed in 0.1.0)
- Limited error handling
- Basic documentation

---

## Migration Guide: 0.0.x → 0.1.0

### Before (0.0.x)
```bash
# Install gh CLI
brew install gh  # or apt install gh

# Authenticate
gh auth login

# Use plugin
nextflow run pipeline.nf
```

### After (0.1.0)
```bash
# Ensure Docker is running
docker --version

# Authenticate (choose one method)
export GITHUB_TOKEN=ghp_your_token
# OR keep existing ~/.config/gh/ from previous gh auth login

# Use plugin (exactly the same!)
nextflow run pipeline.nf
```

### Configuration Changes
No configuration changes required! Your existing `nextflow.config` files work as-is.

### What You Need to Do
1. ✅ Install Docker (if not already installed)
2. ✅ Set `GITHUB_TOKEN` or ensure `~/.config/gh/` exists
3. ✅ Remove local `gh` CLI (optional - no longer needed)
4. ✅ Update plugin version in `nextflow.config`:
   ```groovy
   plugins {
     id 'nf-codespaces@0.1.0'  // Updated version
   }
   ```

### Benefits of Upgrading
- ✅ No need to manage `gh` CLI installation
- ✅ Consistent behavior across all environments
- ✅ Better security with isolated containers
- ✅ Easier CI/CD integration
- ✅ Version pinning via container tags

---

## Upcoming Features

### Planned for 0.2.0
- [ ] Support for custom Docker images
- [ ] Codespace pooling for faster startup
- [ ] Advanced monitoring and telemetry
- [ ] Cost optimization features
- [ ] Multi-codespace parallel execution

### Under Consideration
- [ ] SSH key authentication support
- [ ] GitHub Actions integration
- [ ] Hybrid local/codespace execution
- [ ] Automatic codespace sizing
- [ ] Custom Wave image augmentation

---

## Support

- 🐛 **Bug Reports**: [GitHub Issues](https://github.com/mribeirodantas/nf-codespaces/issues)
- 💬 **Discussions**: [GitHub Discussions](https://github.com/mribeirodantas/nf-codespaces/discussions)
- 📧 **Contact**: Open an issue for support

## Credits

- **Plugin Framework**: Nextflow Plugin SDK
- **Docker Image**: Maintained by Seqera Labs via Wave
- **GitHub CLI**: GitHub, Inc.
- **Contributors**: See [CONTRIBUTORS.md](CONTRIBUTORS.md)
