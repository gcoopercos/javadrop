/*******************************************************************************
 * Copyright 2011 gregorycooper
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
package org.javadrop;

import java.io.File;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Implementations are responsible for filtering runner and packaging scripts through
 * some sort of template processing mechanism.
 * 
 * @author gregorycooper
 *
 */
public interface TemplateProcessor {

	/**
	 * Runs the given file template through the processor and outputs the result.
	 * 
	 * @param inputTemplateFile File to be processed.
	 * @param convertedFile File for the end product. This is expected to the be the full path. Additionally the
	 * parent directory path will be created if it doesn't exist.
	 * @param templateParams Key/value parameters for the template file
	 * @throws MojoExecutionException
	 */
	void applyVTemplate(File inputTemplateFile, File convertedFile,
			Map<String,String> templateParams)
			throws MojoExecutionException;
}
