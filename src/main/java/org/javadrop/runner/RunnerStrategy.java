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
package org.javadrop.runner;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.javadrop.RunnerDefinition;

/**
 * This interface defines a particular runner strategy. A runner strategy is responsible for
 * generating the artifacts and infrastructure needed to execute the java software on the target
 * system.  Particular implementations of this might be responsible for creating the scripts
 * needed for a unix service. Another example might be a strategy for generating run-scripts
 * for executing a java application stand-alone.
 *  
 * @author gcooperpdx
 *
 */
public interface RunnerStrategy {
    /**
     * Passed the original runner definition from the maven pom.
     * @param runnerDef Runner definition as defined in the .pom file
     */
    void setRunnerDefinition(RunnerDefinition runnerDef);
    
    /**
     * Get the original runner definition as defined in the pom
     * @return The runner definition as defined in the .pom file
     */
    RunnerDefinition getRunnerDefinition();
    
    /**
     * Returns a list of target artifacts to be renamed.
     * @param workingDirectory Target directory where the artifacts live
     * @param mavenProj The maven project. Primarily for dependency access
     * @return Mapping of old file -> new file of artifacts produced in the build.
     */
    Map<File, File> getArtifactRenames(MavenProject mavenProj, File workingDirectory);
    
	/**
	 * Provides a list of the names of the template files that this strategy needs converted. 
	 * @return Map of source file -> destination file for velocity templates to process
	 */
	Map<File,File> getConversionFiles(MavenProject mavenProject, File outputDirectory);
	
	/**
	 * Apply the plugin's set of parameters to the runner
	 */
	void applyParameters(Map<String, String> paramMap);
	
	/**
	 * Get the parameters as the runner sees them
	 * @return Key/Value pairs for processing the templates.
	 */
	Map<String,String> getParameters();

	/**
	 * Provides a set of files to be installed into the packager.
	 * @param workingDirectory Directory to grab rpm data from
	 * 
	 * @return A collection of directory -> files for how the final installation tree should look like.  Directory
	 *    is a single file for the name of the directory the files will go under. If the key is not a directory or
	 *    doesn't exist then this plugin will fail to install the files.
	 *    Collection<file> is a collection of files that will go into that directory.
	 */
	Map<File, Collection<File>> getInstallSet(MavenProject mavenProject, File workingDirectory);

	/**
	 * Sets to point at the maven logger
	 * @param log Maven logger
	 */
	void set_log(Log log);
}
