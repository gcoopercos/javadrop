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
import java.util.Map;

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
     * Specify the name of the packager strategies.
     * 
     * @parameter
     * @required
     */
    private List<String> packagerClasses;

    /**
     * 
     * Specify the name of the runner strategy. The actual strategy is looked
     * up. <runners> <param>runner_name1</param> <param>runner_name2</parma>
     * </runners>
     * 
     * @parameter
     * @required
     */
    private List<String> runnerClasses;

    /**
     * Runner strategies.
     */
    private List<RunnerStrategy> runnerStrategies = new LinkedList<RunnerStrategy>();

    /**
     * Packager strategies.
     */
    private List<PackagerStrategy> packagerStrategies = new LinkedList<PackagerStrategy>();

    public JavadropMojo() {
    }

    // private static final String SERVICE_NAME_VAR = "SVC_NAME";
    //
    /**
     * This gets populated from the variables/parameters in the pom. These are
     * processed by the runners and packagers. The "mojo" does nothing with them
     * directly.
     * 
     * @parameter
     * @required
     */
    private Map<String, String> javadropVariables;

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

    // /**
    // * What is the type of the target service?
    // *
    // * @parameter
    // * @required
    // */
    // private String serviceType;
    //
    // /**
    // * The velocity context provides template substitution parameters. It is
    // essentially
    // * how the variables get replaced in the templates. This plugin acts as
    // the glue
    // * between this velocity context and the template result itself.
    // */
    // private VelocityContext velContext = new VelocityContext();
    //
    // /**
    // * This strategy defines how the target service type will require and use
    // provided parameters
    // * from the pom.
    // */
    // private IRunnerStrategy serviceStrategy;

    // public InstallPackageGenMojo()
    // {
    // mergedVariables = new HashMap<String, String>();
    // mergedVariables.put("SVC_ROOT", "/usr/local/iovation");
    // mergedVariables.put("SVC_LOGROOT", "/var/log/iovation");
    // mergedVariables.put("SVC_REDIS_LOG_ROOT", "/var/log/redis");
    // mergedVariables.put("SVC_CACHE_ROOT", "/cache");
    // mergedVariables.put("BUILTIN_JAVA_OPTS",
    // "-Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false");
    // requiredVariables.add("SVC_USER");
    // requiredVariables.add("SVC_USER_ID");
    // requiredVariables.add("SVC_GROUP");
    // requiredVariables.add("SVC_GROUP_ID");
    // requiredVariables.add("SVC_MAIN_CLASS");
    // requiredVariables.add("SVC_NAME");
    // }

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

        // Apply the mojo's parameters to the runners and packagers so they know
        // how to process templates.
        for (RunnerStrategy runner : runnerStrategies) {
            runner.applyParameters(javadropVariables);
        }
        for (PackagerStrategy packager : packagerStrategies) {
            packager.applyParameters(javadropVariables);
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
                    runnerStrategies, getLog());
        }
        getLog().info("Javadrop complete.");
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
     * Create the strategy objects from the given strategy class names specified
     * in the POM.
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

        for (String runnerClass : runnerClasses) {
            Class<?> aClass = classLoader.loadClass(runnerClass);
            RunnerStrategy runnerStrat = (RunnerStrategy) aClass.newInstance();
            runnerStrat.set_log(getLog());
            runnerStrategies.add(runnerStrat);
        }

        for (String packagerClass : packagerClasses) {
            Class<?> aClass = classLoader.loadClass(packagerClass);
            PackagerStrategy packagerStrat = (PackagerStrategy) aClass
                    .newInstance();
            packagerStrat.set_log(getLog());
            packagerStrategies.add(packagerStrat);
        }
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
