package org.javadrop.runner.impl;

import java.io.File;
import java.io.FilenameFilter;

public class JarFilenameFilter implements FilenameFilter {
    @Override
    public boolean accept(File dir, String name) {
        if (name.toLowerCase().endsWith("jar")) {
            return true;
        }
        return false;
    }

}
