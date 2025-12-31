#!/usr/bin/env nextflow

/*
 * Simple validation pipeline for nf-codespaces plugin
 * 
 * This pipeline tests the GitHub Codespaces executor
 */

nextflow.enable.dsl=2

// Simple process to test codespaces executor
process sayHello {
    executor 'codespaces'
    
    output:
    stdout
    
    script:
    """
    echo "Hello from GitHub Codespaces!"
    echo "Hostname: \$(hostname)"
    echo "Date: \$(date)"
    """
}

workflow {
    sayHello | view
}
