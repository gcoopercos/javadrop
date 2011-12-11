package org.javadrop;

import java.util.Map;


/**
 * Defines a packager.  In constract to the runners, which describe how a particulare set of software components
 * are set up to run, a packager defines how a set of software components are bundled for distribution.
 * 
 * Some examples of this might be an rpm, .deb package, .zip file, etc...
 * 
 * This particular class encapsulates the parameters that can be defined in the .pom. They are mapped to this class
 * 
 * @author gregory
 *
 */
public class PackagerDefinition {
    /**
     * This is the class that defines the type of packager being defined
     * @parameter
     * @required
     */
    private String packagerClass;
    
    
    /**
     * This is the map of parameters for the runner. Effectively, these will be name/value pairs
     * passed to the template engine for substitutions in the run-scripts
     * 
     * @parameter
     * @required
     */
    private Map<String,String> packagerParameters;

    public Map<String,String> getPackagerParameters() {
        return packagerParameters;
    }
    
    public String getPackagerClass() {
        return packagerClass;
    }
}
