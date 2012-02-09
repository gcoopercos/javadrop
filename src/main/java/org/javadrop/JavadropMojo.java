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
package org.javadrop;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

import javax.management.IntrospectionException;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.javadrop.packaging.PackagerStrategy;
import org.javadrop.runner.RunnerStrategy;

/**
 * This goal combines one or more 'runner strategies' with one or more 'packager
 * strategies' together to produce an artifact that can be installed.
 * 
 * This plugin is intended to keep the configuration in a pom to a minimum. The
 * various strategies could get fairly complex in their processing.
 * 
 * @goal javadrop
 * @phase install
 * @requiresDependencyResolution runtime
 */
public class JavadropMojo extends AbstractMojo {
    /** @parameter default-value="${project}" */
    protected MavenProject mavenProject;

    /**
     * Defines the runners that may be packaged up to bundle the software
     * 
     * @parameter
     * @required
     */
    private List<RunnerDefinition> runnerDefinitions;
    
    
    /**
     * Defines the packagers that are used to build the software distribution archive.
     * 
     * @parameter
     * @required
     */
    private List<PackagerDefinition> packagerDefinitions;
    

    /**
     * Runner strategies. Once the pom has been configured, these are the actual functionality that does the work
     * for the defined runners.
     */
    private List<RunnerStrategy> runnerStrategies = new LinkedList<RunnerStrategy>();

    /**
     * Packager strategies. The actual packager functionality is found here.
     */
    private List<PackagerStrategy> packagerStrategies = new LinkedList<PackagerStrategy>();

    public JavadropMojo() {
    }

    /**
     * Location of the output directory for the template results.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File workingDirectory;

    public void setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * Location of the output directory for the working files.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File packageDirectory;


    public void setPackageDirectory(File packageDirectory) {
        this.packageDirectory = packageDirectory;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     */
    public void execute() throws MojoExecutionException {
        getLog().info("Javadrop Mojo processing started...");

        try {
            if ((mavenProject != null)
                    && (mavenProject.getCompileClasspathElements() != null)) {
                getLog().info("Class augmented with project.");
                for (int index = 0; index < mavenProject
                        .getCompileClasspathElements().size(); index++) {
                    String path = (String) mavenProject
                            .getCompileClasspathElements().get(index);
                    URL cpUrl = new File(path).toURL();
                    addURLToSystemClassLoader(cpUrl);
                }
            }
            initStrategies();
        } catch (MalformedURLException e) {
            getLog().warn("Can't add compile classpath elements to mojo!", e);
        } catch (DependencyResolutionRequiredException e) {
            getLog().warn("Can't add compile classpath elements to mojo!", e);
        } catch (IntrospectionException e) {
            getLog().warn("Can't add compile classpath elements to mojo!", e);
        } catch (ClassNotFoundException e) {
            getLog().warn(e);
        } catch (InstantiationException e) {
            getLog().warn(e);
        } catch (IllegalAccessException e) {
            getLog().warn(e);
        }

        // Process runner scripts in the context of a packager.
        TemplateProcessor processor = new VelocityTemplateProcessorImpl(
                getLog());
        for (PackagerStrategy packager : packagerStrategies) {
            for (RunnerStrategy runner : runnerStrategies) {
                // Convert scripts
                packager.processTemplates(runner, processor, workingDirectory);

                // Do mappings, renames, whatever in the runner.
                packager.postProcessArtifacts(runner, workingDirectory);
            }
            
            packager.createPackage(packageDirectory, workingDirectory,
                    filteredRunnerStrats(packager, runnerStrategies), getLog());
//            packager.createPackage(packageDirectory, workingDirectory,
//                    runnerStrategies, getLog());
        }
        getLog().info("Javadrop complete.");
    }

    private List<RunnerStrategy> filteredRunnerStrats(PackagerStrategy packagerStrat, List<RunnerStrategy> runnerStrats) {
        List<String> excludedRunners = packagerStrat.getPackagerDefinition().getExcludedRunners();
        LinkedList<RunnerStrategy> filteredStrats = new LinkedList<RunnerStrategy>();
        for (RunnerStrategy runStrat : runnerStrats) {
            if (runStrat.getRunnerDefinition().getRunnerName() == null ||
                   (excludedRunners.contains(runStrat.getRunnerDefinition().getRunnerName())  == false)) {
                filteredStrats.add(runStrat);
            }
        }
        return filteredStrats;
    }
    
    public void addURLToSystemClassLoader(URL url)
            throws IntrospectionException {
        URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader
                .getSystemClassLoader();
        Class classLoaderClass = URLClassLoader.class;

        try {
            Method method = classLoaderClass.getDeclaredMethod("addURL",
                    new Class[] { URL.class });
            method.setAccessible(true);
            method.invoke(systemClassLoader, new Object[] { url });
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IntrospectionException(
                    "Error when adding url to system ClassLoader ");
        }
    }

    /**
     * This creates the actual strategies used to do the work. It maps the parameters
     * found in the pom to the code that will be chugging away.
     * 
     * @throws ClassNotFoundException
     *             If given class name isn't available
     * @throws IllegalAccessException
     *             Problems creating given strategy
     * @throws InstantiationException
     *             Problems creating given strategy
     */
    private void initStrategies() throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        ClassLoader classLoader = getClass().getClassLoader();
        
        for (RunnerDefinition runnerDef : runnerDefinitions) {
            String runnerClass = runnerDef.getRunnerClass();
            Class<?> aClass = classLoader.loadClass(runnerClass);
            RunnerStrategy runnerStrat = (RunnerStrategy) aClass.newInstance();
            // Apply the mojo's parameters to the strategy so it knows how to
            // process templates.
            runnerStrat.applyParameters(runnerDef.getRunnerParameters());
            runnerStrat.set_log(getLog());
            runnerStrat.setRunnerDefinition(runnerDef);
            runnerStrategies.add(runnerStrat);
        }

        for (PackagerDefinition packagerDef : packagerDefinitions) {
            String packagerClass = packagerDef.getPackagerClass();
            Class<?> aClass = classLoader.loadClass(packagerClass);
            PackagerStrategy packagerStrat = (PackagerStrategy) aClass
                    .newInstance(); 
            // Apply the mojo's parameters to the strategyso it knows how to
            // process templates.
            packagerStrat.applyParameters(packagerDef.getPackagerParameters());
            packagerStrat.set_log(getLog());
            packagerStrat.setPackagerDefinition(packagerDef);
            packagerStrategies.add(packagerStrat);
        }
    }


    public List<RunnerDefinition> getRunnerDefinitions() {
        return runnerDefinitions;
    }

    /**
     * Check the values supplied in the pom to see if they are ok.
     * 
     * @throws MojoExecutionException
     */
    // private void validateParameters() throws MojoExecutionException
    // {
    // // Override default values with what is supplied in the .pom
    // mergedVariables.putAll(serviceVariables);
    // getLog().info("Scriptgen mojo variables");
    // for (Entry<String,String> velVariable : mergedVariables.entrySet()) {
    // getLog().info("		Definition: " + velVariable.getKey() + "=" +
    // velVariable.getValue());
    // }
    //
    // // Check required variables.
    // List<String> missingVars = new LinkedList<String>();
    // for (String reqVar : requiredVariables) {
    // if (mergedVariables.containsKey(reqVar) == false) {
    // missingVars.add(reqVar);
    // }
    // }
    //
    // if (missingVars.isEmpty() == false) {
    // StringBuilder sb = new StringBuilder();
    // boolean first = true;
    // for (String missingVar : missingVars) {
    // if (!first) sb.append(',');
    // else first = false;
    // sb.append(missingVar);
    // }
    // throw new
    // MojoExecutionException("Script generation is missing required variables: "
    // + sb.toString());
    // }
    //
    // // Apply the variables to the velocity context.
    // for (Entry<String,String> contextVar : mergedVariables.entrySet()) {
    // velContext.put(contextVar.getKey(), contextVar.getValue());
    // }
    // }

    /**
     * Determine the particular service type being targed.
     */
    // private void determineStrategy() throws MojoExecutionException
    // {
    // // Check the serviceType provided.
    // if (serviceType == null) {
    // throw new
    // MojoExecutionException("Missing 'serviceType'. Must be one of ('jetty', 'standalone')");
    // }
    // if ("jetty".equalsIgnoreCase(serviceType)) {
    // serviceStrategy = new JettyStrategy();
    // } else if ("standalone".equalsIgnoreCase(serviceType)) {
    // serviceStrategy = new MainServiceStrategy();
    // }
    // if (serviceStrategy == null) {
    // throw new
    // MojoExecutionException("Invalid 'serviceType'. Must be one of ('jetty', 'standalone')");
    // }
    // }

}
