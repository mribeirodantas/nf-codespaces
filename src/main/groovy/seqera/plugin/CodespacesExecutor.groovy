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

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.executor.Executor
import nextflow.processor.TaskHandler
import nextflow.processor.TaskMonitor
import nextflow.processor.TaskPollingMonitor
import nextflow.processor.TaskRun
import nextflow.util.Duration
import nextflow.util.ServiceName
import org.pf4j.ExtensionPoint

/**
 * GitHub Codespaces executor
 * 
 * Executes Nextflow tasks in GitHub Codespaces environments
 * 
 * @author Seqera Labs
 */
@Slf4j
@CompileStatic
@ServiceName('codespaces')
class CodespacesExecutor extends Executor implements ExtensionPoint {

    /**
     * @return The display name for this executor
     */
    @Override
    String getName() {
        return 'codespaces'
    }

    /**
     * Create the task monitor for this executor
     * 
     * @return A TaskMonitor instance for polling task status
     */
    @Override
    protected TaskMonitor createTaskMonitor() {
        return TaskPollingMonitor.create(session, name, 100, Duration.of('1s'))
    }

    /**
     * Create a task handler for the given task
     * 
     * @param task The task to be executed
     * @return A TaskHandler instance to manage the task execution
     */
    @Override
    TaskHandler createTaskHandler(TaskRun task) {
        log.debug "Creating task handler for task: ${task.name}"
        return new CodespacesTaskHandler(task, this)
    }
}
