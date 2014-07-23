package org.lumanmed.classguard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.lumanmed.classguard.security.IEncryption;

/**
 * @author Willard Wang
 *
 */
public class Guard {
    private static final String DEFAULT_CONFIG_FILE = "config.properties";
    private static final String KEY_WORKING_DIRECTORY = "workingDirectory";
    private static final String KEY_ENCRYPTION_SUFFIX = "encryptionSuffix";
    private static final String KEY_REMOVE_ORIGINAL_CLASS = "removeOriginalClass";
    private static final String KEY_ENCRYPTION_KEYS = "encryptionKeys";
    private static final String KEY_ENCRYPTION_CLASS = "encryptionClass";
    private static final String KEY_TARGET_CLASS_PARTTERN = "targetClassPattern";

    private String workingDirectory = ".";
    private String encryptionSuffix = ".enc";
    private boolean removeOriginalClass = true;

    private IEncryption encryption;
    private FilenameFilter targetClassFilter;

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
                }};
        }
    }

    public void run() {
        File root = new File(workingDirectory);
        runInDirectory(root);
    }

    private void runInDirectory(File root) {
        if (root.isDirectory() && root.canRead()) {
            root.list(targetClassFilter);
            // TODO
        } else {
            System.out.println(String.format("Ignored %s.",
                    root.getAbsolutePath()));
        }
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
