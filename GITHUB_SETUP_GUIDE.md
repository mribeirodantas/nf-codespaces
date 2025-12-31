# GitHub Repository Setup Guide

This guide will help you create the GitHub repository for nf-codespaces.

## ✅ Current Status

The git repository is already initialized and committed:
- ✅ Git initialized with `main` branch
- ✅ All files committed (26 files, 2811 lines)
- ✅ Commit message prepared
- ✅ Ready to push

## 🚀 Quick Setup (3 Options)

### Option 1: Using GitHub CLI (Recommended)

If you have GitHub CLI installed:

```bash
cd /home/user/nf-codespaces
./setup-github-repo.sh
```

The script will automatically:
1. Create the repository on GitHub
2. Set up the remote
3. Push all files

### Option 2: Using GitHub Web Interface + Git

**Step 1: Create Repository on GitHub**
1. Go to https://github.com/new
2. Fill in:
   - **Repository name**: `nf-codespaces`
   - **Description**: `Nextflow plugin for executing workflows in GitHub Codespaces`
   - **Visibility**: Public ✓
3. **Important**: Do NOT initialize with README, .gitignore, or license
4. Click "Create repository"

**Step 2: Push from Command Line**
```bash
cd /home/user/nf-codespaces

# Add remote (HTTPS)
git remote add origin https://github.com/mribeirodantas/nf-codespaces.git

# Push
git push -u origin main
```

**Or using SSH:**
```bash
cd /home/user/nf-codespaces

# Add remote (SSH)
git remote add origin git@github.com:mribeirodantas/nf-codespaces.git

# Push
git push -u origin main
```

### Option 3: Using GitHub API with Personal Access Token

**Step 1: Create Personal Access Token**
1. Go to https://github.com/settings/tokens
2. Click "Generate new token" → "Generate new token (classic)"
3. Give it a name: "nf-codespaces repo creation"
4. Select scopes: `repo` (full control)
5. Generate and copy the token

**Step 2: Create Repository and Push**
```bash
cd /home/user/nf-codespaces

# Set your token
export GITHUB_TOKEN="your_token_here"

# Create repository using API
curl -X POST -H "Authorization: token $GITHUB_TOKEN" \
     -H "Accept: application/vnd.github.v3+json" \
     https://api.github.com/user/repos \
     -d '{
       "name": "nf-codespaces",
       "description": "Nextflow plugin for executing workflows in GitHub Codespaces",
       "private": false,
       "has_issues": true,
       "has_projects": true,
       "has_wiki": true
     }'

# Add remote and push
git remote add origin https://github.com/mribeirodantas/nf-codespaces.git
git push -u origin main
```

## 📋 What Will Be Pushed

### Repository Structure
```
nf-codespaces/
├── src/main/groovy/seqera/plugin/    [6 classes]
├── src/test/groovy/seqera/plugin/    [1 test]
├── validation/                        [3 files]
├── Documentation files                [8 markdown files]
├── Build configuration                [3 files]
└── Gradle wrapper                     [3 files]

Total: 26 files, 2811 lines
```

### Key Files
- ✅ All source code (6 Groovy classes)
- ✅ Test suite (1 test class)
- ✅ Validation pipeline (main.nf, nextflow.config)
- ✅ Complete documentation (8 files)
- ✅ Build system (build.gradle, settings.gradle)
- ✅ License (GPLv3 - COPYING)
- ✅ .gitignore

### Initial Commit
**Message**: "Initial commit: nf-codespaces plugin implementation"

**Includes**:
- Complete Nextflow executor for GitHub Codespaces
- Core plugin classes and infrastructure
- Workflow observer for lifecycle management
- Unit tests with Spock framework
- Validation pipeline for testing
- Comprehensive documentation (~27KB)
- Gradle build system
- Generated plugin JAR artifact (15KB)

## 🔍 Verification

After pushing, verify the repository:

1. **Check repository exists**:
   ```bash
   curl https://api.github.com/repos/mribeirodantas/nf-codespaces
   ```

2. **View on GitHub**:
   Visit: https://github.com/mribeirodantas/nf-codespaces

3. **Check files**:
   - All 26 files should be visible
   - README.md should display on homepage
   - Documentation should be accessible

## 📝 Repository Settings (After Creation)

### Recommended Settings

1. **Topics/Tags** (add these):
   - `nextflow`
   - `nextflow-plugin`
   - `github-codespaces`
   - `bioinformatics`
   - `workflow-engine`

2. **About Section**:
   - Description: "Nextflow plugin for executing workflows in GitHub Codespaces"
   - Website: (leave blank or add docs site later)
   - Topics: (add from list above)

3. **Options to Enable**:
   - ✅ Issues
   - ✅ Projects (optional)
   - ✅ Wiki (optional)
   - ✅ Discussions (optional - good for community)

4. **Branch Protection** (optional but recommended):
   - Protect `main` branch
   - Require pull request reviews
   - Require status checks

## 🏷️ Create First Release (Optional)

After pushing, create a release:

```bash
# Using GitHub CLI
gh release create v0.1.0 \
  --title "v0.1.0 - Initial Release" \
  --notes "First public release of nf-codespaces plugin.

Features:
- Nextflow executor for GitHub Codespaces
- Complete task lifecycle management
- GitHub CLI integration
- Validation pipeline
- Comprehensive documentation

Status: Beta - ready for testing" \
  build/libs/nf-codespaces-0.1.0.jar
```

Or create manually:
1. Go to: https://github.com/mribeirodantas/nf-codespaces/releases/new
2. Tag version: `v0.1.0`
3. Release title: `v0.1.0 - Initial Release`
4. Describe the release
5. Upload `build/libs/nf-codespaces-0.1.0.jar`
6. Publish release

## 🚨 Troubleshooting

### Authentication Issues

**HTTPS Authentication**:
```bash
# GitHub might prompt for credentials
# Use Personal Access Token as password
git push -u origin main
# Username: mribeirodantas
# Password: [your personal access token]
```

**SSH Authentication**:
```bash
# Make sure SSH key is added to GitHub
ssh -T git@github.com
# Should see: "Hi mribeirodantas! You've successfully authenticated..."
```

### Remote Already Exists

If you see "remote origin already exists":
```bash
git remote remove origin
git remote add origin https://github.com/mribeirodantas/nf-codespaces.git
git push -u origin main
```

### Permission Denied

- Verify you're logged into the correct GitHub account
- Check your Personal Access Token has `repo` scope
- For SSH, verify your SSH key is added to GitHub

## 📞 Need Help?

If you encounter issues:

1. Check git status: `git status`
2. Check remotes: `git remote -v`
3. Check git log: `git log --oneline`
4. Verify GitHub authentication
5. Try HTTPS if SSH fails (or vice versa)

## ✅ Success Confirmation

You'll know it worked when:
- ✅ Repository appears at: https://github.com/mribeirodantas/nf-codespaces
- ✅ All 26 files are visible
- ✅ README.md displays on homepage
- ✅ Commit history shows initial commit
- ✅ You can clone it: `git clone https://github.com/mribeirodantas/nf-codespaces.git`

## 🎉 After Setup

Once the repository is live:

1. Share the repository URL
2. Consider adding:
   - GitHub Actions for CI/CD
   - Automated testing
   - Release automation
   - Code coverage badges
3. Update documentation if needed
4. Share with the community!

---

**Repository URL (after creation)**: https://github.com/mribeirodantas/nf-codespaces

**Quick Command Summary**:
```bash
# Create on GitHub, then:
cd /home/user/nf-codespaces
git remote add origin https://github.com/mribeirodantas/nf-codespaces.git
git push -u origin main
```

---

**Current Status**: ✅ Repository ready to push  
**Next Step**: Choose one of the 3 options above and execute
