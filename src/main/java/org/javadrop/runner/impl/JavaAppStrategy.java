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
import java.util.Map;

/**
 * This implementation of the runner strategy is designed to support stand-alone
 * java client programs that have their own 'main(..)'.
 * 
 * @author gcooperpdx
 * 
 */
public class JavaAppStrategy extends BaseRunnerStrategy {

    /**
     * This artifact rename is used to place build artifacts into the /lib
     * directory on distribution.
     */
    @Override
    public Map<File, File> getArtifactRenames(File workingDirectory) {
        HashMap<File, File> renameMap = new HashMap<File, File>();

        Collection<File> artifactFiles = getDirFiles(
                new File(workingDirectory.getAbsolutePath()),
                new JarFilenameFilter());

        for (File origFile : artifactFiles) {
            renameMap.put(origFile,
                    new File(workingDirectory.getAbsolutePath()
                            + File.separator + "lib" + File.separator
                            + origFile.getName()));
        }

        return renameMap;
    }

    @Override
    public Map<File, File> getConversionFiles(File outputDirectory) {
        Map<File, File> conversionFiles = new HashMap<File, File>(); // super.getConversionFiles(outputDirectory,
                                                                     // serviceName);

        conversionFiles.put(new File(getPrefix() + File.separator + "bin"
                + File.separator + "java_app_sh.vm"), new File(outputDirectory
                + File.separator + "runners" + File.separator + "bin"
                + File.separator + getAppName() + ".sh"));

        // Grabs the properties file from the classpath.
        // conf/[servicename].properties
        conversionFiles.put(new File("conf" + File.separator + getAppName()
                + ".properties"), new File(outputDirectory + File.separator
                + "runners" + File.separator + "conf" + File.separator
                + getAppName() + ".properties"));

        // Grabs the log4j from the claspath
        conversionFiles.put(new File("conf" + File.separator + getAppName()
                + "-log4j.xml"), new File(outputDirectory + File.separator
                + "runners" + File.separator + "conf" + File.separator
                + getAppName() + "-log4j.xml"));

        return conversionFiles;
    }

    @Override
    public Map<File, Collection<File>> getInstallSet(File workingDirectory) {
        Map<File, Collection<File>> installSet = super
                .getInstallSet(workingDirectory);
        Collection<File> installFiles = new ArrayList<File>();
        installFiles.add(new File(getAppName() + ".sh"));
        installSet.put(new File("runners" + File.separator + "bin"),
                installFiles);

        // Lib files
        Collection<File> libFiles = getDirFiles(
                new File(workingDirectory.getAbsolutePath() + File.separator
                        + "lib"), new JarFilenameFilter());

        if (libFiles.size() > 0)
            installSet.put(new File("lib"), libFiles);

        Collection<File> artifactFiles = getDirFiles(
                new File(workingDirectory.getAbsolutePath()),
                new JarFilenameFilter());

        if (artifactFiles.size() > 0) {
            Collection<File> existingFiles = installSet.get(new File("lib"));
            if (existingFiles == null) {
                installSet.put(new File("lib"), artifactFiles);
            } else {
                existingFiles.addAll(artifactFiles);
            }
        }

        // Conf files
        installFiles = new ArrayList<File>();
        installFiles.add(new File(getAppName() + "-log4j.xml"));
        installFiles.add(new File(getAppName() + ".properties"));
        installSet.put(new File("runners" + File.separator + "conf"),
                installFiles);

        return installSet;
    }

    protected String getAppName() {
        return runnerVariables.get("APP_NAME");
    }

    @Override
    protected void applyDefaults() {
        super.applyDefaults();
        runnerVariables.put("APP_NAME", "java_app");
        requiredVariables.add("RUNNER_MAIN_CLASS");
    }

    private String getPrefix() {
        return "org" + File.separator + "javadrop" + File.separator
                + "runnerstrategy" + File.separator + "java_app";
    }

}
