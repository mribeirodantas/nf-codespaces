# nf-codespaces Validation Pipeline

This directory contains a simple validation pipeline to test the nf-codespaces plugin functionality.

## Purpose

The validation pipeline provides a minimal test case to verify that:
- The codespaces executor is properly recognized
- Tasks can be submitted to GitHub Codespaces
- Output is correctly captured and displayed

## Files

- `main.nf` - Simple pipeline that runs a "Hello World" process
- `nextflow.config` - Configuration file specifying the codespaces executor

## Usage

### Running the validation pipeline

From this directory, run:

```bash
nextflow run main.nf
```

Or from the plugin root directory:

```bash
nextflow run validation/main.nf
```

### Expected output

You should see:
1. Nextflow launching the workflow
2. The `sayHello` process executing on GitHub Codespaces
3. Output showing hostname and timestamp
4. Success message

Example:
```
N E X T F L O W  ~  version 25.04.7
Launching `main.nf` [silly_pasteur] DSL2 - revision: abc123

executor >  codespaces (1)
[XX/YYYYYY] process > sayHello [100%] 1 of 1 ✔

Hello from GitHub Codespaces!
Hostname: codespaces-abc123
Date: Mon Dec 31 14:02:00 UTC 2024
```

## Troubleshooting

If the validation fails:

1. **Executor not found**: Ensure the plugin is properly installed
2. **Authentication issues**: Verify GitHub authentication is configured
3. **Connection errors**: Check network connectivity to GitHub

## Extending validation

To add more comprehensive tests, consider:
- Multi-process workflows
- File input/output handling
- Resource requirement specifications
- Error handling scenarios
- Parallel execution tests
