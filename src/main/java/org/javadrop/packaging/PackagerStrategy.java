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
package org.javadrop.packaging;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.javadrop.TemplateProcessor;
import org.javadrop.runner.RunnerStrategy;

/**
 * This interface is intended to be implemented by classes that know how to
 * bundle a grouping of files in such a way that creates an artifact
 * that can be installed on a machine.
 * 
 * @author gcooperpdx
 *
 */
public interface PackagerStrategy {
	/**
	 * Apply the plugin's set of parameters to the runner
	 */
	void applyParameters(Map<String, String> paramMap);

	/**
	 * Processes the necessary packager and runner templates using the given template
	 * processor.
	 * 
	 * @param runner Runner to be packaged up. Note that there may be multiple runners for a single packager.
	 * @param processor Template engine used to process key/value pairs
	 * @param workingDirectory The end result for the processed templates
	 */
	void processTemplates(RunnerStrategy runner, TemplateProcessor processor, File workingDirectory) throws MojoExecutionException;

	/**
	 * Once all the templates have been processed this will take all the artifacts and make an
	 * archive.
	 * @param packageDirectory Where the package will be placed
	 * @param workingDirectory Where the packaged files are grabbed from
	 * @param runners The runners to create the package for
	 * @throws MojoExecutionException If there is a problem creating the package this is thrown. 
	 */
	void createPackage(File packageDirectory, File workingDirectory, Collection<RunnerStrategy> runners, Log _log) throws MojoExecutionException;	

	/**
	 * Provides a list of the names of the template files that this strategy needs converted.
	 * @param outputDirectory Where the result of the template conversion will go.
	 * @param runner Runner context for the packager files.
	 * 
	 * @return Map of source file -> destination file for velocity templates to process
	 */
	Map<File,File> getConversionFiles(File outputDirectory, RunnerStrategy runner);

	/**
	 * Sets the logger for maven error/warning messages
	 * @param log Logger used by the mojo
	 */
	void set_log(Log log);
	
}

