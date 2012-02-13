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
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;



/**
 * This implementation of the service strategy is designed to support stand-alone
 * services that have their own 'main(..)'. (Not jetty based web services) 
 * 
 * @author gcooperpdx
 *
 */
public class MainServiceStrategy extends BaseRunnerStrategy {
	
	@Override
	public Map<File, File> getConversionFiles(MavenProject mavenProject, File outputDirectory) {
		Map<File,File> conversionFiles = new HashMap<File, File>(); //super.getConversionFiles(outputDirectory, serviceName);

		conversionFiles.put(new File(getPrefix() + File.separator + "bin" + File.separator + "service_sh.vm"),
				new File(outputDirectory + File.separator + "runners" + File.separator + "bin" + File.separator + getServiceName() + ".sh"));
		
		conversionFiles.put(new File(getPrefix() + File.separator + "init.d" + File.separator + "service_template_main.vm"),
				new File(outputDirectory + File.separator + "runners" + File.separator + "init.d" + File.separator + getServiceName()));
		
		conversionFiles.put(new File("conf" + File.separator + getServiceName() + ".properties"),
				new File(outputDirectory + File.separator + "runners" + File.separator + "conf" + File.separator + getServiceName() + ".properties"));

        // Grabs the log4j from the claspath
        conversionFiles.put(new File("conf" + File.separator + getServiceName() + "-log4j.xml"),
                new File(outputDirectory + File.separator + "runners" + File.separator + "conf" + File.separator + 
                        getServiceName() + "-log4j.xml"));
        

        @SuppressWarnings("unchecked")
        Set<Artifact> artifacts = mavenProject.getArtifacts();
        Collection<File> artifactFiles = new LinkedList<File>();
        for (Artifact art : artifacts) {
            artifactFiles.add(art.getFile());
        }

        // Grabs artifacts and puts them in lib
//        Collection<File> buildFiles = getDirFiles(outputDirectory,
//                new JarFilenameFilter());
//        for (File artifact : buildFiles) {
        for (File artifactFile : artifactFiles) {
            conversionFiles.put(artifactFile,
                    new File(outputDirectory + File.separator + "lib" + File.separator + artifactFile.getName()));
        }

		return conversionFiles;
	}

	@Override
	public Map<File, Collection<File>> getInstallSet(MavenProject mavenProj, File workingDirectory) {
		Map<File, Collection<File>> installSet = super.getInstallSet(mavenProj, workingDirectory);
		Collection<File> installFiles = new ArrayList<File>();
		installFiles.add(new File(getServiceName() + ".sh"));
		installSet.put(new File("runners" + File.separator + "bin"), installFiles);

		installFiles = new ArrayList<File>();
		installFiles.add(new File(getServiceName()));
		installSet.put(new File("runners" + File.separator + "init.d"), installFiles);

		installFiles = new ArrayList<File>();
		installFiles.add(new File(getServiceName()+ ".properties"));
        installFiles.add(new File(getServiceName() + "-log4j.xml"));
		installSet.put(new File("runners" + File.separator + "conf"), installFiles);

        // Lib files
        Collection<File> libFiles = getDirFiles(new File(workingDirectory.getAbsolutePath() + File.separator + "lib"),
                new JarFilenameFilter());
        if (libFiles.size() > 0) installSet.put(new File("lib"), libFiles);
            

		return installSet;
	}

	@Override
	protected void applyDefaults() {
		super.applyDefaults();
		requiredVariables.add("SVC_MAIN_CLASS");
        runnerVariables.put("PKG_STANDALONE", getServiceName());
        runnerVariables.put("JTY_NAME", "notused");
	}
	
	private String getPrefix() {
		return "org" + File.separator + "javadrop" + File.separator + "runnerstrategy" + File.separator + "services";
	}
	
	protected String getServiceName() {
		return runnerVariables.get("SVC_NAME");
	}

}
