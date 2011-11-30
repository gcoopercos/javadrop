/*******************************************************************************
 * Copyright 2011 iovation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.javadrop.runner.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;
import org.javadrop.runner.RunnerStrategy;

/**
 * Base class for runners. Deals with a lot of the variables that are common to
 * all the runners.
 * 
 * The general strategy with variables is that it is very rare for a variable to
 * be required. Rather, appropriate default variables should be provided. Having
 * said that, there will be variables that are required. These will be in the
 * 'requiredVariables' set.
 * 
 * @author gcooperpdx
 * 
 */
public abstract class BaseRunnerStrategy implements RunnerStrategy {

    /**
     * The variables that are used to process the runner templates
     */
    protected Map<String, String> runnerVariables   = new HashMap<String, String>();

    /**
     * Variable that are required and have no default values
     * 
     * TODO It appears these are not used.
     */
    protected Set<String>         requiredVariables = new HashSet<String>();

    private Log                   _log;

    @Override
    public Map<File, File> getArtifactRenames(File workingDirectory) {
        // Default to doing nothing
        return new HashMap<File, File>();
    }

    @Override
    public Map<File, Collection<File>> getInstallSet(File workingDirectory) {
        // TODO Need to better abstract this to eliminate the need to keep in
        // sync with 'getConversionFiles()'
        Map<File, Collection<File>> installSet = new HashMap<File, Collection<File>>();
        // Collection<File> installFiles = new ArrayList<File>();
        // installFiles.add(new File(serviceName));
        // installSet.put(new File("init.d"), installFiles);

        return installSet;
    }

    @Override
    public void applyParameters(Map<String, String> paramMap) {
        applyDefaults();
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            runnerVariables.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Map<String, String> getParameters() {
        return runnerVariables;
    }

    protected void applyDefaults() {
        runnerVariables.clear();
        // Assign appropriate defaults to common variables
        runnerVariables.put("JMX_PORT", "1098");
        runnerVariables.put("RUNNER_NAME", "runner");
        runnerVariables.put("RUNNER_INSTALL_LOC", "/usr/local/javadrop/runner");
        runnerVariables.put("JAVA_INSTALL_LOC", "/usr/java/latest");
        runnerVariables.put("RUNNER_USER", "javadrop");
        runnerVariables.put("RUNNER_GROUP", "javadrop");

        // Deprecated
        runnerVariables.put("SVC_NAME", "service");
        runnerVariables.put("SVC_INSTALL_LOC", "/usr/local/javadrop/service");
        runnerVariables.put("SVC_USER", "javadrop");
        runnerVariables.put("SVC_GROUP", "javadrop");
        // end deprecated

    }

    /**
     * Get a bunch of files from a given directory. Generally used to grab a set of generated files (libraries, e.g.)
     * @param dir Directory to grab the files from
     * @return Collection of files.
     */
    protected Collection<File> getDirFiles(File dir) {
        
        ArrayList<File> fileList = new ArrayList<File>();

        File [] dirList = dir.listFiles();
        if (dirList == null) {
            get_log().warn("Directory is missing or empty: " + dir.getAbsolutePath());
            return fileList;
        }
        for (File file : dirList) {
            fileList.add(file);
        }
        return fileList;
    }
    
    @Override
    public void set_log(Log _log) {
        this._log = _log;
    }

    public Log get_log() {
        return _log;
    }
}
