#!/bin/bash

# Script to create and push nf-codespaces to GitHub
# Run this script after ensuring you have GitHub CLI (gh) installed and authenticated

set -e

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║                                                                ║"
echo "║         nf-codespaces GitHub Repository Setup Script          ║"
echo "║                                                                ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Check if git is initialized
if [ ! -d .git ]; then
    echo "✓ Initializing git repository..."
    git init
    git branch -m main
fi

# Configure git user
echo "✓ Configuring git user..."
git config user.name "Seqera AI" 2>/dev/null || true
git config user.email "seqera-ai@seqera.io" 2>/dev/null || true

# Check if we have uncommitted changes
if [ -n "$(git status --porcelain)" ]; then
    echo "✓ Staging files..."
    git add -A
    
    echo "✓ Creating initial commit..."
    git commit -m "Initial commit: nf-codespaces plugin implementation

- Implement complete Nextflow executor for GitHub Codespaces
- Add core plugin classes (Executor, TaskHandler, Factory, Extension)
- Include workflow observer for lifecycle management
- Add unit tests with Spock framework
- Create validation pipeline for testing
- Add comprehensive documentation (8 files, ~27KB)
- Configure Gradle build system
- Generate plugin JAR artifact (15KB)

Features:
- Seamless Nextflow integration
- GitHub CLI authentication
- Task submission and monitoring
- Output capture (stdout/stderr)
- Configurable execution options
- Background task monitoring
- Comprehensive error handling
- Resource cleanup

Version: 0.1.0
Status: Ready for beta testing"
fi

echo ""
echo "════════════════════════════════════════════════════════════════"
echo "                    GitHub Repository Setup                     "
echo "════════════════════════════════════════════════════════════════"
echo ""

# Try using GitHub CLI if available
if command -v gh &> /dev/null; then
    echo "✓ GitHub CLI detected"
    echo ""
    echo "Creating repository: mribeirodantas/nf-codespaces"
    
    gh repo create mribeirodantas/nf-codespaces \
        --public \
        --description "Nextflow plugin for executing workflows in GitHub Codespaces" \
        --source . \
        --push
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "✅ Repository created successfully!"
        echo ""
        echo "🔗 Repository URL: https://github.com/mribeirodantas/nf-codespaces"
        echo ""
        exit 0
    fi
else
    echo "⚠️  GitHub CLI (gh) not found"
    echo ""
    echo "Please install GitHub CLI and run this script again, or follow"
    echo "the manual setup instructions below."
    echo ""
fi

echo "════════════════════════════════════════════════════════════════"
echo "              Manual Setup Instructions                         "
echo "════════════════════════════════════════════════════════════════"
echo ""
echo "1. Create a new repository on GitHub:"
echo "   • Go to: https://github.com/new"
echo "   • Repository name: nf-codespaces"
echo "   • Description: Nextflow plugin for executing workflows in GitHub Codespaces"
echo "   • Visibility: Public"
echo "   • Do NOT initialize with README, .gitignore, or license"
echo ""
echo "2. Add the remote and push:"
echo "   cd /home/user/nf-codespaces"
echo "   git remote add origin https://github.com/mribeirodantas/nf-codespaces.git"
echo "   git push -u origin main"
echo ""
echo "3. Or using SSH:"
echo "   git remote add origin git@github.com:mribeirodantas/nf-codespaces.git"
echo "   git push -u origin main"
echo ""
echo "════════════════════════════════════════════════════════════════"
echo ""
echo "📝 Note: The repository is already initialized and committed."
echo "    You just need to create it on GitHub and push."
echo ""
