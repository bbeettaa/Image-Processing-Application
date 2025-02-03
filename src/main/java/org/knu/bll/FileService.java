package org.knu.bll;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * The FileService class provides methods to load and save images from and to files.
 */
public class FileService {

    /**
     * Loads an image from the specified file path.
     *
     * @param path The file path of the image to load.
     * @return A BufferedImage representing the loaded image.
     * @throws RuntimeException if the file does not exist or an error occurs while loading the image.
     */
    public BufferedImage loadImage(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                throw new RuntimeException("File does not exist: " + file.getAbsolutePath());
            }
            return ImageIO.read(file);
        } catch (IOException e) {
            throw new RuntimeException("Error loading image from " + path, e);
        }
    }

    /**
     * Saves an image to the specified file.
     *
     * @param selectedFile The file to save the image to.
     * @param image The image to save.
     * @throws RuntimeException if the file cannot be created or written to, or if an error occurs while saving the image.
     */
    public void saveImage(File selectedFile, BufferedImage image) {
        String format = getFileExtension(selectedFile).toUpperCase();
        if (image == null || format.isBlank()) {
            throw new IllegalArgumentException("Invalid arguments: file, image, or format cannot be null or empty.");
        }
        if (!ImageIO.getImageWritersByFormatName(format).hasNext()) {
            throw new IllegalArgumentException("Unsupported image format: " + format);
        }
        try {
            if (!selectedFile.exists()) {
                if (!selectedFile.createNewFile()) {
                    throw new IOException("Failed to create file: " + selectedFile.getAbsolutePath());
                }
            }
            if (!selectedFile.canWrite()) {
                throw new IOException("File is not writable: " + selectedFile.getAbsolutePath());
            }
            ImageIO.write(image, format, selectedFile);
        } catch (IOException e) {
            throw new RuntimeException("Error saving image to file: " + selectedFile.getAbsolutePath(), e);
        }
    }

    /**
     * Gets the file extension of the specified file.
     *
     * @param file The file to get the extension for.
     * @return The file extension in lowercase, or an empty string if the file has no extension.
     */
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return (lastDot == -1) ? "" : name.substring(lastDot + 1).toLowerCase();
    }
}
