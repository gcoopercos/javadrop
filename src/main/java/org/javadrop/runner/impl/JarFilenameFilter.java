package org.javadrop.runner.impl;

import java.io.File;
import java.io.FilenameFilter;

public class JarFilenameFilter implements FilenameFilter {
    
    /**
     * Gets all jar files that are not source or docs.
     */
    @Override
    public boolean accept(File dir, String name) {
        if (name.toLowerCase().endsWith("jar") && (name.toLowerCase().endsWith("-sources.jar") == false) 
                && (name.toLowerCase().endsWith("-docs.jar") == false)) {
            return true;
        }
        return false;
    }

}
