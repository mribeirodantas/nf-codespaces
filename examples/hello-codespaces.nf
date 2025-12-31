#!/usr/bin/env nextflow

/*
 * Example pipeline demonstrating nf-codespaces plugin usage
 *
 * This pipeline runs simple tasks in GitHub Codespaces to validate
 * the executor functionality.
 */

nextflow.enable.dsl = 2

// Simple process that runs in a codespace
process sayHello {
    output:
    path 'hello.txt'

    script:
    """
    echo "Hello from GitHub Codespaces!" > hello.txt
    echo "Hostname: \$(hostname)" >> hello.txt
    echo "Date: \$(date)" >> hello.txt
    """
}

// Process that demonstrates file handling
process processFile {
    input:
    path input_file

    output:
    path 'result.txt'

    script:
    """
    echo "Processing file in codespace..." > result.txt
    echo "Input file contents:" >> result.txt
    cat ${input_file} >> result.txt
    echo "" >> result.txt
    echo "Lines in input: \$(wc -l < ${input_file})" >> result.txt
    """
}

// Main workflow
workflow {
    sayHello()
    processFile(sayHello.out)
    processFile.out.view { file -> 
        "Result:\n${file.text}" 
    }
}
