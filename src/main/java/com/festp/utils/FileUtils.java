package com.festp.utils;

import com.festp.Logger;
import com.festp.Main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.String.format;

// Original: https://github.com/AuthMe/AuthMeReloaded/blob/d2a36b95d6f10694c5d08cd1abee42da3c6b3df4/src/main/java/fr/xephi/authme/util/FileUtils.java
/**
 * File utilities.
 */
public final class FileUtils
{
    // Utility class
    private FileUtils() {
    }

    /**
     * Copy a resource file (from the JAR) to the given file if it doesn't exist.
     *
     * @param destinationFile The file to check and copy to (outside of JAR)
     * @param resourcePath    Local path to the resource file (path to file within JAR)
     *
     * @return False if the file does not exist and could not be copied, true otherwise
     */
    public static boolean copyFileFromResource(File destinationFile, String resourcePath) {
        if (destinationFile.exists()) {
            return true;
        } else if (!createDirectory(destinationFile.getParentFile())) {
        	Logger.warning("Cannot create parent directories for '" + destinationFile + "'");
            return false;
        }

        try (InputStream is = getResourceFromJar(resourcePath)) {
            if (is == null) {
            	Logger.warning(format("Cannot copy resource '%s' to file '%s': cannot load resource",
                    resourcePath, destinationFile.getPath()));
            } else {
                java.nio.file.Files.copy(is, destinationFile.toPath());
                return true;
            }
        } catch (IOException e) {
        	Logger.severe(format("Cannot copy resource '%s' to file '%s': ",
                resourcePath, destinationFile.getPath()) + e.getMessage());
        }
        return false;
    }

    /**
     * Creates the given directory.
     *
     * @param dir the directory to create
     * @return true upon success, false otherwise
     */
    public static boolean createDirectory(File dir) {
        if (!dir.exists() && !dir.mkdirs()) {
            Logger.warning("Could not create directory '" + dir + "'");
            return false;
        }
        return dir.isDirectory();
    }

    /**
     * Returns a JAR file as stream. Returns null if it doesn't exist.
     *
     * @param path the local path (starting from resources project, e.g. "config.yml" for 'resources/config.yml')
     * @return the stream if the file exists, or false otherwise
     */
    public static InputStream getResourceFromJar(String path) {
        // ClassLoader#getResourceAsStream does not deal with the '\' path separator: replace to '/'
        final String normalizedPath = path.replace("\\", "/");
        return Main.class.getClassLoader().getResourceAsStream(normalizedPath);
    }

    /**
     * Delete a given directory and all its content.
     *
     * @param directory The directory to remove
     */
    public static void purgeDirectory(File directory) {
        if (!directory.isDirectory()) {
            return;
        }
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File target : files) {
            if (target.isDirectory()) {
                purgeDirectory(target);
            }
            delete(target);
        }
    }

    /**
     * Delete the given file or directory and log a message if it was unsuccessful.
     * Method is null safe and does nothing when null is passed.
     *
     * @param file the file to delete
     */
    public static void delete(File file) {
        if (file != null) {
            boolean result = file.delete();
            if (!result) {
            	Logger.warning("Could not delete file '" + file + "'");
            }
        }
    }

    /**
     * Creates the given file or throws an exception.
     *
     * @param file the file to create
     */
    public static void create(File file) {
        try {
            boolean result = file.createNewFile();
            if (!result) {
                throw new IllegalStateException("Could not create file '" + file + "'");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error while creating file '" + file + "'", e);
        }
    }

    /**
     * Construct a file path from the given elements, i.e. separate the given elements by the file separator.
     *
     * @param elements The elements to create a path with
     *
     * @return The created path
     */
    public static String makePath(String... elements) {
        return String.join(File.separator, elements);
    }
}