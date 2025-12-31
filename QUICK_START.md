# nf-codespaces - Quick Start Guide

## 🚀 Get Started in 3 Steps

### Step 1: Build the Plugin
```bash
./gradlew build
```

### Step 2: Run the Validation Pipeline
```bash
cd validation
nextflow run main.nf
```

### Step 3: Use in Your Workflow
```groovy
// my_workflow.nf
process myTask {
    executor 'codespaces'
    
    output:
    path 'results.txt'
    
    script:
    """
    echo "Running in codespaces!" > results.txt
    """
}

workflow {
    myTask()
}
```

---

## 📋 Configuration

### Minimal Configuration
```groovy
// nextflow.config
process {
    executor = 'codespaces'
}
```

### Full Configuration
```groovy
// nextflow.config
executor {
    name = 'codespaces'
    codespace = 'my-codespace-name'      // Optional: specific codespace
    machineType = 'standardLinux32gb'     // Optional: machine type
    timeout = '1h'                        // Optional: task timeout
    pollInterval = 5000                   // Optional: polling interval (ms)
}

process {
    executor = 'codespaces'
}
```

---

## ✅ Prerequisites

1. **GitHub CLI** - Install and authenticate:
   ```bash
   # Install gh CLI
   brew install gh  # macOS
   # or
   sudo apt install gh  # Ubuntu/Debian
   
   # Authenticate
   gh auth login
   ```

2. **Active Codespace** - Create or connect to a codespace:
   ```bash
   gh codespace list
   gh codespace create
   ```

3. **Nextflow** - Version 25.04.7 or later:
   ```bash
   curl -s https://get.nextflow.io | bash
   ```

---

## 🧪 Test Your Setup

```bash
# 1. Build the plugin
./gradlew build

# 2. Check for the JAR file
ls -lh build/libs/nf-codespaces-*.jar

# 3. Run validation
cd validation
nextflow run main.nf

# Expected output:
# N E X T F L O W  ~  version 25.04.7
# executor >  codespaces (1)
# [XX/YYYYYY] process > sayHello [100%] 1 of 1 ✔
# Hello from GitHub Codespaces!
# Hostname: codespaces-abc123
# Date: Mon Dec 31 14:02:00 UTC 2024
```

---

## 📖 Example Workflows

### Example 1: Simple Data Processing
```groovy
#!/usr/bin/env nextflow
nextflow.enable.dsl=2

process processData {
    executor 'codespaces'
    
    input:
    path data_file
    
    output:
    path 'processed.txt'
    
    script:
    """
    cat ${data_file} | grep "pattern" > processed.txt
    """
}

workflow {
    channel.fromPath('data/*.txt') | processData | view
}
```

### Example 2: Multi-Step Pipeline
```groovy
#!/usr/bin/env nextflow
nextflow.enable.dsl=2

process step1 {
    executor 'codespaces'
    
    input:
    val item
    
    output:
    path 'step1.txt'
    
    script:
    """
    echo "Step 1: ${item}" > step1.txt
    """
}

process step2 {
    executor 'codespaces'
    
    input:
    path input_file
    
    output:
    path 'final.txt'
    
    script:
    """
    cat ${input_file} | tr '[:lower:]' '[:upper:]' > final.txt
    """
}

workflow {
    channel.of('hello', 'world')
        | step1
        | step2
        | view
}
```

---

## 🐛 Troubleshooting

### Plugin Not Found
```
Error: Unknown executor 'codespaces'
```
**Solution**: Rebuild the plugin and ensure it's in the plugins directory
```bash
./gradlew build
```

### GitHub CLI Not Authenticated
```
Error: gh: not authenticated
```
**Solution**: Authenticate with GitHub
```bash
gh auth login
```

### No Active Codespace
```
Error: No codespace found
```
**Solution**: Create or start a codespace
```bash
gh codespace create
# or
gh codespace list
```

### Command Execution Failed
```
Error: Failed to execute command in codespace
```
**Solution**: Check codespace is running
```bash
gh codespace list
gh codespace logs
```

---

## 📚 Further Reading

- **Full Documentation**: See `PLUGIN_IMPLEMENTATION_SUMMARY.md`
- **Validation Details**: See `validation/README.md`
- **Project Status**: See `COMPLETION_CHECKLIST.md`

---

## 💡 Tips

1. **Start Simple**: Use the validation pipeline as a template
2. **Test Locally**: Verify your workflow logic before using codespaces
3. **Monitor Logs**: Use `nextflow log` to check execution details
4. **Resource Limits**: Be aware of codespace resource quotas
5. **File Transfer**: Large files may take time to transfer

---

## 🎯 Common Use Cases

✅ **Development Workflows**: Test pipelines in isolated environments  
✅ **Collaborative Analysis**: Share computational environment with team  
✅ **Resource Scaling**: Use larger machines for intensive tasks  
✅ **Cloud Integration**: Seamless GitHub integration  
✅ **CI/CD Testing**: Automated pipeline validation  

---

## 📞 Getting Help

1. Check the validation pipeline works first
2. Review error messages carefully
3. Verify GitHub CLI authentication
4. Ensure codespace is active and accessible
5. Check network connectivity

---

**Ready to start?** Run `./gradlew build` and then `cd validation && nextflow run main.nf`! 🚀
