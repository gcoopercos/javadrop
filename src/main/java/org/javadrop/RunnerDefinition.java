package org.javadrop;

import java.util.Map;


/**
 * Defines a runner.  A runner defines how a particular set of software components is configured
 * to executed. Examples of this might be: stand-alone application, headless service, jetty based service, etc...
 * 
 * This particular class encapsulates the parameters that can be defined in the .pom. They are mapped to this class
 * 
 * @author gregory
 *
 */
public class RunnerDefinition {
    /**
     * This is the class that defines the type of runner being defined
     * @parameter
     * @required
     */
    private String runnerClass;
    
    
    /**
     * This is the map of parameters for the runner. Effectively, these will be name/value pairs
     * passed to the template engine for substitutions in the run-scripts
     * 
     * @parameter
     * @required
     */
    private Map<String,String> runnerParameters;

    public Map<String,String> getRunnerParameters() {
        return runnerParameters;
    }
    
    public String getRunnerClass() {
        return runnerClass;
    }
}
