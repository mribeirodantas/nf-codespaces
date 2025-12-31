/*
 * Copyright 2025, Seqera Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package seqera.plugin

import groovy.util.logging.Slf4j
import nextflow.executor.BashWrapperBuilder
import nextflow.processor.TaskHandler
import nextflow.processor.TaskRun
import nextflow.processor.TaskStatus
import nextflow.trace.TraceRecord

import java.nio.file.Path

/**
 * Task handler for GitHub Codespaces executor
 * 
 * Manages the lifecycle of a task executed in a GitHub Codespace
 * 
 * @author Seqera Labs
 */
@Slf4j
class CodespacesTaskHandler extends TaskHandler {

    private final CodespacesExecutor executor
    private String codespaceName
    private volatile boolean started
    private volatile Integer exitStatus
    
    CodespacesTaskHandler(TaskRun task, CodespacesExecutor executor) {
        super(task)
        this.executor = executor
    }

    /**
     * Submit the task for execution
     */
    @Override
    void submit() {
        log.debug "Submitting task: ${task.name}"
        
        // Get codespace configuration from task config
        def config = task.config
        def repo = config.ext?.codespaces?.repo ?: getDefaultRepo()
        def branch = config.ext?.codespaces?.branch
        
        // Create bash wrapper script
        def builder = new BashWrapperBuilder(task)
        builder.build()
        
        try {
            // For simplicity, we'll use a shared codespace or create one
            codespaceName = getOrCreateCodespace(repo, branch)
            
            // Copy task files to codespace
            copyTaskFiles()
            
            // Execute the task script in the codespace
            executeInCodespace()
            
            started = true
            status = TaskStatus.SUBMITTED
            
            log.debug "Task submitted successfully: ${task.name} in codespace: ${codespaceName}"
        }
        catch (Exception e) {
            log.error "Failed to submit task: ${task.name}", e
            status = TaskStatus.FAILED
            throw e
        }
    }

    /**
     * Check if the task is running
     */
    @Override
    boolean checkIfRunning() {
        if (!started) {
            return false
        }
        if (isCompleted()) {
            return false
        }
        return true
    }

    /**
     * Check if the task is completed
     */
    @Override
    boolean checkIfCompleted() {
        if (!started) {
            return false
        }
        
        if (exitStatus != null) {
            status = TaskStatus.COMPLETED
            task.exitStatus = exitStatus
            return true
        }
        
        // Check exit status file
        def exitFile = task.workDir.resolve('.exitcode')
        if (exitFile.exists()) {
            try {
                exitStatus = exitFile.text.trim() as Integer
                status = TaskStatus.COMPLETED
                task.exitStatus = exitStatus
                log.debug "Task completed: ${task.name} with exit status: ${exitStatus}"
                return true
            }
            catch (Exception e) {
                log.warn "Failed to read exit status for task: ${task.name}", e
            }
        }
        
        return false
    }

    /**
     * Kill the running task
     */
    @Override
    void kill() {
        log.debug "Killing task: ${task.name}"
        // In a simple implementation, we just mark it as terminated
        // In a full implementation, we would stop the process in the codespace
        if (started && !isCompleted()) {
            // Mark as terminated
            status = TaskStatus.COMPLETED
        }
    }
    
    /**
     * Kill the task (alternative method name)
     */
    void killTask() {
        kill()
    }

    /**
     * Get or create a codespace for task execution
     */
    @groovy.transform.TypeChecked(groovy.transform.TypeCheckingMode.SKIP)
    private String getOrCreateCodespace(String repo, String branch) {
        // For simplicity, try to use an existing codespace or create a new one
        def listCmd = ['gh', 'codespace', 'list', '--json', 'name,state']
        def result = executeCommand(listCmd)
        
        // Parse JSON and find a running codespace
        if (result.exitValue == 0 && result.stdout) {
            def codespaces = new groovy.json.JsonSlurper().parseText(result.stdout as String)
            def running = codespaces.find { it.state == 'Available' }
            if (running) {
                log.debug "Using existing codespace: ${running.name}"
                return running.name as String
            }
        }
        
        // Create a new codespace
        log.debug "Creating new codespace for repo: ${repo}"
        def createCmd = ['gh', 'codespace', 'create', '-r', repo]
        if (branch) {
            createCmd += ['-b', branch]
        }
        
        def createResult = executeCommand(createCmd)
        if (createResult.exitValue != 0) {
            def stderr = createResult.stderr as String
            throw new RuntimeException("Failed to create codespace: ${stderr}")
        }
        
        // Extract codespace name from output
        def stdout = createResult.stdout as String
        return stdout.trim()
    }

    /**
     * Get default repository from git config
     */
    private String getDefaultRepo() {
        // Try to get from environment or config
        def repo = System.getenv('CODESPACE_REPO')
        if (repo) {
            return repo
        }
        
        // Default fallback
        return 'user/default-repo'
    }

    /**
     * Copy task files to codespace
     */
    private void copyTaskFiles() {
        log.debug "Copying task files to codespace: ${codespaceName}"
        
        // Use gh codespace cp or scp to copy files
        def workDir = task.workDir.toString()
        def remotePath = "/tmp/nextflow-work/${task.hash}"
        
        // Create remote directory
        def mkdirCmd = "mkdir -p ${remotePath}"
        executeInCodespaceSync(mkdirCmd)
        
        // Copy work directory contents
        def remotePathStr = "${remotePath}/" as String
        def cpCmd = ['gh', 'codespace', 'cp', '-r', '-c', codespaceName, workDir, remotePathStr]
        executeCommand(cpCmd)
    }

    /**
     * Execute the task script in the codespace
     */
    private void executeInCodespace() {
        log.debug "Executing task in codespace: ${codespaceName}"
        
        def remotePath = "/tmp/nextflow-work/${task.hash}"
        def scriptPath = "${remotePath}/.command.run"
        
        // Execute the script asynchronously
        def cmd = "cd ${remotePath} && bash .command.run &"
        executeInCodespaceSync(cmd)
        
        // In a production implementation, we would track the process
        // For simplicity, we'll use a background job and check for completion
    }

    /**
     * Execute a command in the codespace synchronously
     */
    private void executeInCodespaceSync(String command) {
        def cmd = ['gh', 'codespace', 'ssh', '-c', codespaceName, '--', command]
        def result = executeCommand(cmd)
        
        if (result.exitValue != 0) {
            log.warn "Command failed in codespace: ${command}, stderr: ${result.stderr}"
        }
    }

    /**
     * Execute a command and return the result
     */
    private Map executeCommand(List<String> command) {
        def proc = command.execute()
        def stdout = new StringBuilder()
        def stderr = new StringBuilder()
        
        proc.consumeProcessOutput(stdout, stderr)
        proc.waitFor()
        
        return [
            exitValue: proc.exitValue(),
            stdout: stdout.toString(),
            stderr: stderr.toString()
        ]
    }

    /**
     * Create trace record for this task
     */
    @Override
    TraceRecord getTraceRecord() {
        def trace = super.getTraceRecord()
        trace.put('native_id', codespaceName)
        return trace
    }
}
