# Authentication Guide for nf-codespaces

This guide provides step-by-step instructions for setting up GitHub authentication to use the nf-codespaces plugin.

## Quick Overview

The nf-codespaces plugin needs GitHub authentication to create and manage codespaces. You have **two options**:

1. **GitHub Personal Access Token** (Recommended for CI/CD and automated workflows)
2. **GitHub CLI Authentication** (Recommended for interactive/local development)

Both methods work equally well - choose based on your use case!

---

## Option 1: GitHub Personal Access Token (Recommended for CI/CD)

### Step 1: Create a GitHub Personal Access Token

1. **Go to GitHub Settings**
   - Visit https://github.com/settings/tokens
   - Or navigate: GitHub Profile → Settings → Developer settings → Personal access tokens → Tokens (classic)

2. **Click "Generate new token (classic)"**
   - You may need to confirm your password

3. **Configure the token**
   - **Note**: Give it a descriptive name like `Nextflow Codespaces Token`
   - **Expiration**: Choose based on your security policy
     - For testing: 30 days
     - For production: 90 days or custom
     - Never use "No expiration" unless absolutely necessary
   
4. **Select scopes** - You need these permissions:
   - ✅ `repo` - Full control of private repositories
     - This includes `repo:status`, `repo_deployment`, etc.
   - ✅ `codespace` - Full control of codespaces
     - This includes `codespace:secrets`
   
   ![Token Scopes](https://docs.github.com/assets/cb-10967/mw-1440/images/help/settings/token-scopes.webp)

5. **Generate token**
   - Click "Generate token" at the bottom
   - **IMPORTANT**: Copy the token immediately! You won't be able to see it again.
   - The token will look like: `ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`

### Step 2: Set the Token as an Environment Variable

#### For Current Session (Temporary)

```bash
export GITHUB_TOKEN=ghp_your_token_here
```

#### For Persistent Use (Recommended)

Add to your shell configuration file:

**For Bash** (~/.bashrc or ~/.bash_profile):
```bash
echo 'export GITHUB_TOKEN=ghp_your_token_here' >> ~/.bashrc
source ~/.bashrc
```

**For Zsh** (~/.zshrc):
```bash
echo 'export GITHUB_TOKEN=ghp_your_token_here' >> ~/.zshrc
source ~/.zshrc
```

**For Fish** (~/.config/fish/config.fish):
```bash
echo 'set -gx GITHUB_TOKEN ghp_your_token_here' >> ~/.config/fish/config.fish
source ~/.config/fish/config.fish
```

### Step 3: Verify the Token

Test that the plugin can use your token:

```bash
# Check if token is set
echo $GITHUB_TOKEN

# Test authentication with the Docker image the plugin uses
docker run --rm \
  -e GITHUB_TOKEN=$GITHUB_TOKEN \
  community.wave.seqera.io/library/pip_gh:04f2de2fa12e5bcc \
  gh auth status
```

Expected output:
```
github.com
  ✓ Logged in to github.com as YOUR-USERNAME (oauth_token)
  ✓ Git operations protocol: https
  ✓ Token: ghp_************************************
  ✓ Token scopes: codespace, repo
```

### Step 4: Use with Nextflow

Now you can run your pipeline:

```bash
nextflow run your-pipeline.nf
```

The plugin will automatically use `$GITHUB_TOKEN` for authentication.

---

## Option 2: GitHub CLI Authentication (Recommended for Local Development)

### Step 1: Install GitHub CLI

**macOS**:
```bash
brew install gh
```

**Linux (Debian/Ubuntu)**:
```bash
# Add the official repository
curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | \
  sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg

echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] \
  https://cli.github.com/packages stable main" | \
  sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null

sudo apt update
sudo apt install gh
```

**Linux (Fedora/CentOS/RHEL)**:
```bash
sudo dnf install gh
```

**Windows**:
```bash
# Using winget
winget install --id GitHub.cli

# Or using Chocolatey
choco install gh

# Or using Scoop
scoop install gh
```

**Other platforms**: See https://github.com/cli/cli#installation

### Step 2: Authenticate with GitHub

Run the authentication wizard:

```bash
gh auth login
```

You'll be prompted with several questions:

1. **What account do you want to log into?**
   - Choose: `GitHub.com`

2. **What is your preferred protocol for Git operations?**
   - Choose: `HTTPS` (recommended) or `SSH`

3. **Authenticate GitHub CLI with your GitHub credentials?**
   - Choose: `Login with a web browser` (easiest)
   - OR `Paste an authentication token` (if you have one)

4. **If using web browser**: 
   - Copy the 8-character code shown
   - Press Enter to open browser
   - Paste the code in the browser
   - Click "Authorize github"

### Step 3: Verify Authentication

```bash
gh auth status
```

Expected output:
```
github.com
  ✓ Logged in to github.com as YOUR-USERNAME (keyring)
  ✓ Git operations protocol: https
  ✓ Token: gho_************************************
  ✓ Token scopes: gist, read:org, repo
```

### Step 4: Verify Plugin Access

The plugin will automatically detect and use your GitHub CLI configuration:

```bash
# Test that the plugin can access your gh config via Docker
docker run --rm \
  -v ~/.config/gh:/root/.config/gh:ro \
  community.wave.seqera.io/library/pip_gh:04f2de2fa12e5bcc \
  gh auth status
```

Expected output: Same as Step 3

### Step 5: Use with Nextflow

Now you can run your pipeline:

```bash
nextflow run your-pipeline.nf
```

The plugin will automatically mount your `~/.config/gh/` directory into the Docker container for authentication.

---

## Using Both Methods Together

You can have both methods set up! The plugin will prefer `$GITHUB_TOKEN` if it's set, and fall back to GitHub CLI config if not.

**Priority order**:
1. `$GITHUB_TOKEN` environment variable (if set)
2. `~/.config/gh/` directory (if exists)

This is useful for:
- **Local development**: Use GitHub CLI (`gh auth login`)
- **CI/CD**: Use token (`$GITHUB_TOKEN`)

---

## CI/CD Integration

### GitHub Actions

```yaml
name: Run Nextflow Pipeline
on: [push]

jobs:
  run:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up Docker
        uses: docker/setup-buildx-action@v2
      
      - name: Run pipeline with nf-codespaces
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          nextflow run your-pipeline.nf
```

**Note**: GitHub Actions provides `GITHUB_TOKEN` automatically, but it may have limited permissions. For codespaces, you might need to:

1. Create a Personal Access Token with `repo` and `codespace` scopes
2. Add it as a repository secret (Settings → Secrets → Actions → New repository secret)
3. Use it in your workflow: `GITHUB_TOKEN: ${{ secrets.CODESPACES_TOKEN }}`

### GitLab CI

```yaml
run_pipeline:
  image: nextflow/nextflow:latest
  services:
    - docker:dind
  variables:
    DOCKER_HOST: tcp://docker:2375
    GITHUB_TOKEN: $GITHUB_TOKEN
  script:
    - nextflow run your-pipeline.nf
```

Add `GITHUB_TOKEN` to GitLab CI/CD variables: Settings → CI/CD → Variables → Add variable

### Jenkins

```groovy
pipeline {
    agent any
    environment {
        GITHUB_TOKEN = credentials('github-token')
    }
    stages {
        stage('Run Pipeline') {
            steps {
                sh 'nextflow run your-pipeline.nf'
            }
        }
    }
}
```

Add token to Jenkins credentials: Manage Jenkins → Credentials → Add Credentials

---

## Troubleshooting

### Error: "Could not read Username for 'https://github.com'"

**Cause**: No authentication method is configured

**Solution**: 
- Set `$GITHUB_TOKEN` environment variable, OR
- Run `gh auth login` to set up GitHub CLI authentication

### Error: "HTTP 401: Bad credentials"

**Cause**: Token is invalid, expired, or has insufficient permissions

**Solution**:
1. Check token expiration: https://github.com/settings/tokens
2. Verify token has `repo` and `codespace` scopes
3. Generate a new token if needed
4. Update `$GITHUB_TOKEN` with the new token

### Error: "Resource not accessible by integration"

**Cause**: Token doesn't have permission to access codespaces

**Solution**:
1. Regenerate token with `codespace` scope selected
2. Ensure you have access to GitHub Codespaces for your account/organization

### Token Works Locally But Not in CI/CD

**Cause**: CI/CD environment variables not properly configured

**Solution**:
1. Verify the token is added to CI/CD secrets/variables
2. Check that the environment variable name is exactly `GITHUB_TOKEN`
3. Ensure the secret is not masked/redacted in logs (don't log it!)
4. Verify the CI/CD job has access to the secret (scope/permissions)

### GitHub CLI Works But Plugin Doesn't

**Cause**: Docker cannot access `~/.config/gh/` directory

**Solution**:
```bash
# Check if directory exists
ls -la ~/.config/gh/

# Check permissions (should be readable)
chmod -R u+r ~/.config/gh/

# Test Docker can mount it
docker run --rm -v ~/.config/gh:/root/.config/gh:ro alpine ls -la /root/.config/gh/
```

### Multiple GitHub Accounts

If you have multiple GitHub accounts:

**Option 1**: Use different tokens
```bash
# For account 1
export GITHUB_TOKEN=ghp_token_for_account1

# For account 2
export GITHUB_TOKEN=ghp_token_for_account2
```

**Option 2**: Use GitHub CLI with multiple hosts
```bash
# Switch between accounts
gh auth switch
```

---

## Security Best Practices

### For Personal Access Tokens

1. **Use minimal scopes**: Only `repo` and `codespace` - no more
2. **Set expiration**: Never use tokens that don't expire
3. **Rotate regularly**: Create new tokens every 90 days
4. **Don't commit tokens**: Never add tokens to git repositories
5. **Use secrets management**: 
   - Local: Use shell environment variables
   - CI/CD: Use built-in secrets/variables features
   - Production: Use tools like HashiCorp Vault, AWS Secrets Manager, etc.

### For GitHub CLI

1. **Protect config directory**: `chmod 700 ~/.config/gh`
2. **Use keyring**: GitHub CLI uses your system keyring by default (secure!)
3. **Review permissions**: Run `gh auth status` regularly
4. **Logout when done**: On shared machines, run `gh auth logout`

### For CI/CD

1. **Use repository/organization secrets**: Don't hardcode tokens
2. **Limit token scope**: Use fine-grained tokens when possible
3. **Audit access**: Review who can access secrets
4. **Rotate tokens**: Set up automatic rotation if possible
5. **Monitor usage**: Check token usage in GitHub settings

---

## Quick Reference

### Check If Authentication Is Set Up

```bash
# Method 1: Check token
echo $GITHUB_TOKEN

# Method 2: Check GitHub CLI
gh auth status

# Method 3: Test with plugin's Docker image (token)
docker run --rm \
  -e GITHUB_TOKEN=$GITHUB_TOKEN \
  community.wave.seqera.io/library/pip_gh:04f2de2fa12e5bcc \
  gh auth status

# Method 4: Test with plugin's Docker image (gh config)
docker run --rm \
  -v ~/.config/gh:/root/.config/gh:ro \
  community.wave.seqera.io/library/pip_gh:04f2de2fa12e5bcc \
  gh auth status
```

### Set Up Token Quickly

```bash
# 1. Create token: https://github.com/settings/tokens (select: repo, codespace)
# 2. Set environment variable
export GITHUB_TOKEN=ghp_your_token_here
# 3. Make it persistent
echo 'export GITHUB_TOKEN=ghp_your_token_here' >> ~/.bashrc && source ~/.bashrc
```

### Set Up GitHub CLI Quickly

```bash
# 1. Install gh
brew install gh  # macOS
# 2. Authenticate
gh auth login
# 3. Verify
gh auth status
```

---

## Need Help?

- **GitHub Personal Access Tokens**: https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token
- **GitHub CLI**: https://cli.github.com/manual/
- **GitHub Codespaces**: https://docs.github.com/en/codespaces
- **Plugin Issues**: https://github.com/mribeirodantas/nf-codespaces/issues

---

**Next Steps**: Once authentication is set up, check out the [Quick Start Guide](QUICKSTART.md) to run your first pipeline!
