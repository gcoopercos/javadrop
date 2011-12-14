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
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * This set of tests checks the java application runner.
 * 
 * @author gcooperpdx
 * 
 */
public class JavaApplicationMojoTest extends JavaDropBaseTest {

    /**
     * Tests that a java application can be packaged up.
     * 
     * @throws Exception
     */
    @Test
    public void testJavaAppCreation() throws Exception {
        // Dummy up some java lib to go in the rpm
        new File(scriptOutputDir.getAbsolutePath() + File.separator + "lib")
                .mkdirs();
        File dummyFile = new File(scriptOutputDir.getAbsolutePath()
                + File.separator + "lib/dummy.jar");
        dummyFile.createNewFile();
        dummyFile = new File(scriptOutputDir.getAbsolutePath() + File.separator
                + "dummyartifact.jar");
        dummyFile.createNewFile();

        File testPom = getTestFile("src/test/resources/java_app_test_pom.xml");

        JavadropMojo mojo;
        mojo = (JavadropMojo) lookupMojo("javadrop", testPom);
        assertNotNull(mojo);
        mojo.setWorkingDirectory(scriptOutputDir);
        mojo.setPackageDirectory(scriptOutputDir);
        mojo.execute();

        // Now analyze the directories for proper contents
        File folderFile = new File(getBasedir()
                + "/target/testdata/runners/bin");
        File[] contents = folderFile.listFiles();
        assertEquals(1, contents.length);
        boolean foundAppscript = false;
        for (File runscript : contents) {
            if (runscript.getName().equals("jtestapp.sh"))
                foundAppscript = true;
        }
        assertTrue("Expecting to find app run script", foundAppscript);
        String fileResult = readFileAsString(getBasedir()
                + "/target/testdata/runners/bin/jtestapp.sh");
        assertTrue("JMX Port config not found",
                fileResult.contains("-Dcom.sun.management.jmxremote.port=1093"));

        folderFile = new File(getBasedir() + "/target/testdata/rpm/");
        contents = folderFile.listFiles();
        assertEquals(3, contents.length);
        Set<String> fileSet = new HashSet<String>();
        fileSet.add("postinstall.sh");
        fileSet.add("postremove.sh");
        fileSet.add("preinstall.sh");
        for (File fileContent : contents) {
            assertTrue(fileSet.contains(fileContent.getName()));
        }

        // Check to see if the rpm is there.
        File rpmFile = new File(getBasedir()
                + "/target/testdata/jtestapp-1.0-1309218173.noarch.rpm");
        checkRPMFile(rpmFile, "dummy.jar", "/usr/local/iovation/jtestapp/lib/");
        checkRPMFile(rpmFile, "dummyartifact.jar",
                "/usr/local/iovation/jtestapp/");
        
        // Check for the properties file
        checkRPMFile(rpmFile, "jtestapp.properties", "/usr/local/iovation/jtestapp/conf/");
    }


}
