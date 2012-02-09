package org.javadrop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.channels.Channels;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.freecompany.redline.ReadableChannelWrapper;
import org.freecompany.redline.Scanner;
import org.freecompany.redline.header.AbstractHeader.Entry;
import org.freecompany.redline.header.Format;
import org.freecompany.redline.header.Header;

public abstract class JavaDropBaseTest extends AbstractMojoTestCase {

    /**
     * Test directory defaults to current directory. Intended to be set by the
     * maven pom.
     */
    protected File scriptOutputDir;

    public JavaDropBaseTest() {
        super();
    }

    @Override
    public void setUp() throws Exception {
        // Required for mojo lookup to work
        super.setUp();

        scriptOutputDir = new File(getBasedir() + "/target/testdata");
        scriptOutputDir.mkdirs();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        recursiveRmDir(scriptOutputDir);
    }

    /**
     * Note that this doesn't check for links back to parents so it is pretty
     * brain-dead. However, it is designed to clean up the testdata which is a
     * known quantity.
     * 
     * @param directory
     *            Directory to be deleted
     */
    protected void recursiveRmDir(File directory) {
        File[] contents = directory.listFiles();
        for (File fileContent : contents) {
            if (fileContent.isDirectory()) {
                recursiveRmDir(fileContent);
            } else {
                fileContent.delete();
            }
        }

        directory.delete();
    }

    /**
     * This is a little horrid as it sucks a file into a String. However, for
     * the purposes of testing it seems fine so long as it's not abused. (makes
     * testing file contents easier)
     * 
     * @param filePath
     *            File to load in
     * @return String of THE ENTIRE FILE (so be careful)
     * @throws java.io.IOException
     */
    protected String readFileAsString(String filePath)
            throws java.io.IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            fileData.append(buf, 0, numRead);
        }
        reader.close();
        return fileData.toString();
    }

    @SuppressWarnings("unchecked")
    protected void checkRPMFile(File rpmFile, String basename, String dirname)
            throws FileNotFoundException, Exception {
        Scanner scanner = new Scanner();
        Format format = scanner.run(new ReadableChannelWrapper(Channels
                .newChannel(new FileInputStream(rpmFile))));

        Entry<String[]> stringEntries = (Entry<String[]>) format.getHeader()
                .getEntry(Header.HeaderTag.BASENAMES);

        String[] bnames = stringEntries.getValues();
        if (!arrayContains(bnames, basename)) {
            fail("Basename not found in RPM: " + basename);
        }

        stringEntries = (Entry<String[]>) format.getHeader().getEntry(
                Header.HeaderTag.DIRNAMES);
        String[] dnames = stringEntries.getValues();
        if (!arrayContains(dnames, dirname)) {
            fail("Dirname not found in RPM: " + dirname);
        }
    }

    @SuppressWarnings("unchecked")
    protected void verifyRPMFileMissing(File rpmFile, String basename,
            String dirname) throws FileNotFoundException, Exception {
        boolean foundFile = false;
        boolean foundDir = false;
        Scanner scanner = new Scanner();
        Format format = scanner.run(new ReadableChannelWrapper(Channels
                .newChannel(new FileInputStream(rpmFile))));

        Entry<String[]> stringEntries = (Entry<String[]>) format.getHeader()
                .getEntry(Header.HeaderTag.BASENAMES);

        String[] bnames = stringEntries.getValues();
        if (arrayContains(bnames, basename)) {
            foundFile = true;
        }

        stringEntries = (Entry<String[]>) format.getHeader().getEntry(
                Header.HeaderTag.DIRNAMES);
        String[] dnames = stringEntries.getValues();
        if (arrayContains(dnames, dirname)) {
            foundDir = true;
            fail("Dirname not found in RPM: " + dirname);
        }
        assertFalse("File exists but it shouldn't: " + basename, foundFile);
        assertFalse("Directory exists but it shouldn't: " + dirname, foundDir);
    }

    private boolean arrayContains(String[] sarray, String toFind) {
        for (String astr : sarray) {
            if (astr.equalsIgnoreCase(toFind))
                return true;
        }
        return false;
    }

}