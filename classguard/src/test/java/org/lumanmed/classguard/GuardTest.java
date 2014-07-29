/**
 * 
 */
package org.lumanmed.classguard;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lumanmed.classguard.security.AESEncryption;

/**
 * @author Willard Wang
 *
 */
public class GuardTest {
    static final String key = "K1ojI90%Nc5A#xuE";
    static final String iv = "t#vD2TnDaREbp%3P";
    Properties properties;

    @Before
    public void setUp() throws Exception {
        properties = new Properties();
        properties.setProperty(Guard.KEY_ENCRYPTION_CLASS,
                AESEncryption.class.getCanonicalName());
        properties.setProperty(Guard.KEY_ENCRYPTION_KEYS,
                key+","+iv);
        properties.setProperty(Guard.KEY_TARGET_CLASS_PARTTERN, "com.test");
    }

    @After
    public void tearDown() throws Exception {
        properties = null;
    }

    /**
     * Test method for {@link org.lumanmed.classguard.Guard#run()}.
     */
    @Test
    public void testRun() {
        //fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link org.lumanmed.classguard.Guard#processFile(java.io.File)}.
     */
    @Test
    public void testProcessFile() {
        Guard guard = new Guard(properties);
        Random random = new Random();
        int length = random.nextInt(1024);
        byte[] data = new byte[length];
        random.nextBytes(data);

        File testFile = new File("com/test/Testing.class");
        File resultFile = new File("com/test/Testing.class"
                + guard.getEncryptionSuffix());
        try {
            System.out.println(testFile.getAbsolutePath());
            if (testFile.exists()) {
                testFile.delete();
            } else if (!testFile.getParentFile().exists()) {
                assertTrue(testFile.getParentFile().mkdirs());
            }
            assertTrue(testFile.createNewFile());
            
            OutputStream os = new FileOutputStream(testFile);
            os.write(data);
            os.close();

            if (resultFile.exists()) {
                resultFile.delete();
            }

            assertFalse(resultFile.exists());

            guard.processFile(testFile);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        assertTrue(resultFile.exists());

        byte[] result = null;
        try {
            InputStream is = new FileInputStream(resultFile);
            assertEquals(length, is.available());
            result = new byte[length];
            is.read(result);
            is.close();
            
            deleteDir(new File("com"));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        AESEncryption enc = new AESEncryption(key, iv);
        byte[] actual = enc.decrypt(result);
        assertEquals(length, actual.length);
        for (int i=0; i<length; i++) {
            assertEquals(data[i], actual[i]);
        }
    }

    /**
     * Delete a directory recursively.
     * 
     * @param dir to delete
     * @return boolean Returns "true" if all deletions were successful.
     *                 If a deletion fails, the method stops attempting to
     *                 delete and returns "false".
     */
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
}
