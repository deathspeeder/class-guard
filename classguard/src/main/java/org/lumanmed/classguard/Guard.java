package org.lumanmed.classguard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.lumanmed.classguard.security.IEncryption;

/**
 * @author Willard Wang
 *
 */
public class Guard {
    private static final Logger logger = Logger.getLogger(Guard.class);
    
    public static final String DEFAULT_CONFIG_FILE = "config.properties";
    public static final String KEY_WORKING_DIRECTORY = "workingDirectory";
    public static final String KEY_ENCRYPTION_SUFFIX = "encryptionSuffix";
    public static final String KEY_REMOVE_ORIGINAL_CLASS = "removeOriginalClass";
    public static final String KEY_ENCRYPTION_KEYS = "encryptionKeys";
    public static final String KEY_ENCRYPTION_CLASS = "encryptionClass";
    public static final String KEY_TARGET_CLASS_PARTTERN = "targetClassPattern";

    private String workingDirectory = ".";
    private String encryptionSuffix = ".enc";
    private boolean removeOriginalClass = true;

    private IEncryption encryption;
    private FilenameFilter targetClassFilter;
    
    public String getEncryptionSuffix() {
        return encryptionSuffix;
    }

    public Guard(Properties properties) {
        if (properties.containsKey(KEY_WORKING_DIRECTORY)) {
            workingDirectory = properties.getProperty(KEY_WORKING_DIRECTORY);
        }
        if (properties.containsKey(KEY_ENCRYPTION_SUFFIX)) {
            encryptionSuffix = properties.getProperty(KEY_ENCRYPTION_SUFFIX);
        }
        if (properties.containsKey(KEY_REMOVE_ORIGINAL_CLASS)) {
            removeOriginalClass = Boolean.parseBoolean(properties
                    .getProperty(KEY_REMOVE_ORIGINAL_CLASS));
        }
        String[] encryptionKeys;
        if (properties.containsKey(KEY_ENCRYPTION_KEYS)) {
            encryptionKeys = properties.getProperty(KEY_ENCRYPTION_KEYS).split(
                    ",");
        } else {
            throw new IllegalArgumentException("Need to specify "
                    + KEY_ENCRYPTION_KEYS);
        }

        if (properties.containsKey(KEY_ENCRYPTION_CLASS)) {
            try {
                Class<?> clazz = Class.forName(properties
                        .getProperty(KEY_ENCRYPTION_CLASS));
                Object object = clazz.newInstance();
                if (!(object instanceof IEncryption)) {
                    throw new IllegalArgumentException(String.format(
                            "Class %s does not implements %s.",
                            clazz.getCanonicalName(),
                            IEncryption.class.getCanonicalName()));
                }
                encryption = (IEncryption) object;
                encryption.init(encryptionKeys);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            } catch (InstantiationException e) {
                throw new IllegalArgumentException(e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            throw new IllegalArgumentException("Need to specify "
                    + KEY_ENCRYPTION_CLASS);
        }

        if (properties.containsKey(KEY_TARGET_CLASS_PARTTERN)) {
            targetClassFilter = new TargetClassNameFilter(
                    properties.getProperty(KEY_TARGET_CLASS_PARTTERN));
        } else {
            targetClassFilter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return true;
                }
            };
        }
    }

    public void run() throws IOException {
        File root = new File(workingDirectory);
        runInDirectory(root);
    }

    protected void runInDirectory(File root) throws IOException {
        String[] names = root.list(targetClassFilter);
        for (String name : names) {
            File file = new File(name);
            if (file.isDirectory() && file.canRead()) {
                runInDirectory(file);
            } else if (file.isFile() && file.canRead()) {
                processFile(file);
            } else {
                logger.warn(String.format("Ignored %s.",
                        root.getAbsolutePath()));
            }
        }
    }

    protected void processFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        byte[] data = new byte[is.available()];
        is.read(data);
        is.close();
        
        if (removeOriginalClass) {
            file.delete();
        }
        
        OutputStream os = new FileOutputStream(file.getAbsolutePath() + encryptionSuffix);
        os.write(encryption.encrypt(data));
        os.close();
    }

    public static void main(String[] args) throws IOException {

        InputStream is = null;
        if (args.length > 0) {
            is = new FileInputStream(args[0]);
        } else {
            is = new FileInputStream(DEFAULT_CONFIG_FILE);
        }

        Properties properties = new Properties();
        properties.load(is);
        is.close();

        Guard guard = new Guard(properties);
        guard.run();
    }
}
