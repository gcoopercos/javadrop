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
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/**
 * Simplistic sanity unit test for seeing that the scriptgen is doing something
 * reasonable.
 * 
 * @author gcooperpdx
 * 
 */
public class InstallPackageGenMojoTest extends JavaDropBaseTest {

    public void testNestedAnnotations() throws Exception {
        File testPom = getTestFile("src/test/resources/pom_scoped_runners.xml");
        JavadropMojo mojo;
        mojo = (JavadropMojo) lookupMojo("javadrop", testPom);
        assertNotNull(mojo);
        List<RunnerDefinition> rdefs = mojo.getRunnerDefinitions();
        assertEquals("Checking number of runners", 2, rdefs.size());
    }

    /**
     * This does a basic sanity check to see that the directory structure is
     * correct. It currently does not go into details with determining what the
     * contents of the scripts are. This would be a worthy extension.
     * 
     * @throws Exception
     */
    @Test
    public void testStandAloneScriptCreation() throws Exception {
        File testPom = getTestFile("src/test/resources/service_test_pom.xml");
        // Dummy up some java lib to go in the rpm
        new File(scriptOutputDir.getAbsolutePath() + File.separator + "lib")
                .mkdirs();
        File dummyFile = new File(scriptOutputDir.getAbsolutePath()
                + File.separator + "lib/dummy.jar");
        dummyFile.createNewFile();

        File dummyBuildJar = new File(scriptOutputDir.getAbsolutePath()
                + File.separator + "dummyartifact.jar");
        dummyBuildJar.createNewFile();

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
        assertEquals("testservice.sh", contents[0].getName());
        String fileResult = readFileAsString(getBasedir()
                + "/target/testdata/runners/bin/testservice.sh");
        assertTrue("JMX Port config not found",
                fileResult.contains("-Dcom.sun.management.jmxremote.port=1098"));

        folderFile = new File(getBasedir() + "/target/testdata/runners/init.d");
        contents = folderFile.listFiles();
        assertEquals(1, contents.length);
        assertEquals("testservice", contents[0].getName());

        // Check to make sure service script is for stand-alone mode
        fileResult = readFileAsString(getBasedir()
                + "/target/testdata/runners/init.d/testservice");
        assertFalse("Unexpected JETTY reference found",
                fileResult.contains("JETTY"));

        folderFile = new File(getBasedir() + "/target/testdata/rpm");
        contents = folderFile.listFiles();
        assertEquals(3, contents.length);
        Set<String> fileSet = new HashSet<String>();
        fileSet.add("postinstall.sh");
        fileSet.add("postremove.sh");
        fileSet.add("preinstall.sh");
        for (File fileContent : contents) {
            assertTrue(fileSet.contains(fileContent.getName()));
        }
        File rpmFile = new File(getBasedir()
                + "/target/testdata/testservice-1.0-1309218173.noarch.rpm");
        checkRPMFile(rpmFile, "dummy.jar",
                "/usr/local/iovation/testservice/lib/");
        checkRPMFile(rpmFile, "dummyartifact.jar",
                "/usr/local/iovation/testservice/lib/");
    }

    /**
     * This does a basic sanity check to see that the directory structure is
     * correct. It currently does not go into details with determining what the
     * contents of the scripts are. This would be a worthy extension.
     * 
     * @throws Exception
     */
    @Test
    public void testJettyScriptCreation() throws Exception {
        createDummyWarFile();

        File testPom = getTestFile("src/test/resources/jetty_service_test_pom.xml");

        JavadropMojo mojo;
        mojo = (JavadropMojo) lookupMojo("javadrop", testPom);
        assertNotNull(mojo);
        mojo.setWorkingDirectory(scriptOutputDir);
        mojo.setPackageDirectory(scriptOutputDir);
        mojo.execute();

        // Now analyze the directories for proper contents
        jettyResults(false, "jtytestsvc");

        // Check for web port default of 8080
        String fileResult = readFileAsString(getBasedir()
                + "/target/testdata/runners/conf/jetty-spring.xml");
        assertTrue("Missing or incorrect web port number",
                fileResult.contains("SelectChannelConnector\" p:port=\"8080\""));

    }

    private void createDummyWarFile() throws IOException {
        // Simulate the creation of the war file by the build in the target
        // directory
        // new File(scriptOutputDir.getAbsolutePath() + File.separator +
        // "war").mkdirs();
        File dummyWarFile = new File(scriptOutputDir.getAbsolutePath()
                + File.separator + "jtytestsvc-1.0.war");
        dummyWarFile.createNewFile();
    }

    /**
     * This does a basic sanity check to see that the directory structure is
     * correct for a 'multi' strategy project.
     * 
     * @throws Exception
     */
    @Test
    public void testMultiScriptCreation() throws Exception {
        createDummyWarFile();

        // Dummy up some java lib to go in the rpm
        new File(scriptOutputDir.getAbsolutePath() + File.separator + "lib")
                .mkdirs();
        File dummyFile = new File(scriptOutputDir.getAbsolutePath()
                + File.separator + "lib/dummy.jar");
        dummyFile.createNewFile();

        File dummyBuildJar = new File(scriptOutputDir.getAbsolutePath()
                + File.separator + "dummyartifact.jar");
        File testPom = getTestFile("src/test/resources/multi_runners_test_pom.xml");
        dummyBuildJar.createNewFile();

        JavadropMojo mojo;
        mojo = (JavadropMojo) lookupMojo("javadrop", testPom);
        assertNotNull(mojo);
        mojo.setWorkingDirectory(scriptOutputDir);
        mojo.setPackageDirectory(scriptOutputDir);
        mojo.execute();

        jettyResults(true, "jtestapp");
        javaAppResults(2);
        // Check for web port default of 8080
        String fileResult = readFileAsString(getBasedir()
                + "/target/testdata/runners/conf/jetty-spring.xml");
        assertTrue("Missing or incorrect web port number",
                fileResult.contains("SelectChannelConnector\" p:port=\"9000\""));

    }

    private void javaAppResults(int numRunScripts) throws Exception {
        // Now analyze the directories for proper contents
        File folderFile = new File(getBasedir()
                + "/target/testdata/runners/bin");
        File[] contents = folderFile.listFiles();
        assertEquals(numRunScripts, contents.length);
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
    }

    private void jettyResults(boolean multi, String testPrefix)
            throws Exception {
        // Now analyze the directories for proper contents
        File folderFile = new File(getBasedir()
                + "/target/testdata/runners/bin");
        File[] contents = folderFile.listFiles();
        if (!multi) {
            // Single jetty test, only jetty runscript
            assertEquals(1, contents.length);
        } else {
            // Multi runscript assumes that we're only using 2.
            assertEquals(2, contents.length);
        }

        boolean foundJettyscript = false;
        for (File runscript : contents) {
            if (runscript.getName().equals("jtytestsvc.sh"))
                foundJettyscript = true;
        }
        assertTrue("Expecting to find jetty run script", foundJettyscript);
        String fileResult = readFileAsString(getBasedir()
                + "/target/testdata/runners/bin/jtytestsvc.sh");
        assertTrue("JMX Port config not found",
                fileResult.contains("-Dcom.sun.management.jmxremote.port=1093"));

        folderFile = new File(getBasedir() + "/target/testdata/runners/init.d");
        contents = folderFile.listFiles();
        assertEquals(1, contents.length);
        assertEquals("jtytestsvc", contents[0].getName());

        // Check to make sure service script is for jetty mode
        fileResult = readFileAsString(getBasedir()
                + "/target/testdata/runners/init.d/jtytestsvc");
        // assertTrue("JETTY reference not found",
        // fileResult.contains("JETTY"));

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

        folderFile = new File(getBasedir() + "/target/testdata/runners/conf");
        contents = folderFile.listFiles();
        if (!multi) {
            assertEquals(5, contents.length);
        } else {
            assertEquals(7, contents.length);
        }
        fileResult = readFileAsString(getBasedir()
                + "/target/testdata/runners/conf/jetty-spring.xml");
        assertTrue(
                "Missing or incorrect test-service-facade context name",
                fileResult
                        .contains("p:contextPath=\"/jtytestsvc\" p:extractWAR="));
        assertTrue("Missing or incorrect test-service-facade war name",
                fileResult.contains("p:war=\"war/jtytestsvc.war\""));
        File rpmFile = new File(getBasedir() + "/target/testdata/" + testPrefix
                + "-1.0-1309218173.noarch.rpm");
        
        // Check for war file
        checkRPMFile(rpmFile, "jtytestsvc", "/usr/local/iovation/" + testPrefix
                + "/war/");
        
        // Check for the properties file 
        checkRPMFile(rpmFile, "jtytestsvc.properties", "/usr/local/iovation/" + testPrefix + "/conf/");
    }

}
