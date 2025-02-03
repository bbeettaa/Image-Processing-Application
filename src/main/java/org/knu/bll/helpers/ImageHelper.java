package org.knu.bll.helpers;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * The ImageHelper class provides utility methods for image manipulation, including converting images to grayscale,
 * copying images, and calculating histograms and Cumulative Distribution Functions (CDFs).
 */
public class ImageHelper {

    /**
     * Converts the given image to grayscale.
     *
     * @param coloredImage The image to be converted to grayscale.
     */
    public static void grayscale(BufferedImage coloredImage) {
        int width = coloredImage.getWidth();
        int height = coloredImage.getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int px = coloredImage.getRGB(x, y);
                Color color = new Color(px, true);
                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();
                int gray = (int) (0.299 * red + 0.587 * green + 0.114 * blue);
                coloredImage.setRGB(x, y, new Color(gray, gray, gray).getRGB());
            }
        }
    }

    /**
     * Converts the given image to grayscale using a more efficient method.
     *
     * @param coloredImage The image to be converted to grayscale.
     */
    public static void convertToGrayscale(BufferedImage coloredImage) {
        int width = coloredImage.getWidth();
        int height = coloredImage.getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int px = coloredImage.getRGB(x, y);
                coloredImage.setRGB(x, y, getGrayColorRGB(px));
            }
        }
    }

    /**
     * Converts an RGB value to a grayscale value.
     *
     * @param px The RGB value to be converted to grayscale.
     * @return The grayscale value as an RGB integer.
     */
    public static int getGrayColorRGB(int px) {
        int red = (0xff & (px >> 16));
        int green = (0xff & (px >> 8));
        int blue = (0xff & px);
        int gray = (int) (0.299 * red + 0.587 * green + 0.114 * blue);
        return (0xff000000 | gray << 16 | gray << 8 | gray);
    }

    /**
     * Converts a byte value to an RGB value.
     *
     * @param b The byte value to be converted to RGB.
     * @return The RGB value as an integer.
     */
    public static int byteToRGB(int b) {
        int red = ((b << 16));
        int green = ((b << 8));
        int blue = (b);
        return (red | green | blue);
    }

    /**
     * Converts an RGB value to a grayscale byte value.
     *
     * @param px The RGB value to be converted to grayscale.
     * @return The grayscale byte value.
     */
    public static int getGrayByte(int px) {
        int red = (0xff & (px >> 16));
        int green = (0xff & (px >> 8));
        int blue = (0xff & px);
        int gray = (int) (0.299 * red + 0.587 * green + 0.114 * blue);
        return gray;
    }

    /**
     * Calculates the average gradient value of an RGB value.
     *
     * @param rgb The RGB value to calculate the gradient for.
     * @return The average gradient value.
     */
    public static int getGradient(int rgb) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        return (red + green + blue) / 3;
    }

    /**
     * Extracts the red component from an RGB value.
     *
     * @param rgb The RGB value to extract the red component from.
     * @return The red component as a byte value.
     */
    public static int getRedByte(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    /**
     * Extracts the green component from an RGB value.
     *
     * @param rgb The RGB value to extract the green component from.
     * @return The green component as a byte value.
     */
    public static int getGreenByte(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    /**
     * Extracts the blue component from an RGB value.
     *
     * @param rgb The RGB value to extract the blue component from.
     * @return The blue component as a byte value.
     */
    public static int getBlueByte(int rgb) {
        return (rgb) & 0xFF;
    }

    /**
     * Creates a copy of the given image.
     *
     * @param image The image to be copied.
     * @return A new BufferedImage that is a copy of the original image.
     */
    public static BufferedImage copyImage(BufferedImage image) {
        BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                output.setRGB(x, y, image.getRGB(x, y));
            }
        }
        return output;
    }

    /**
     * Creates a copy of the given image with the specified image type.
     *
     * @param image The image to be copied.
     * @param imageType The type of the new image.
     * @return A new BufferedImage that is a copy of the original image with the specified image type.
     */
    public static BufferedImage copyImage(BufferedImage image, int imageType) {
        BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), imageType);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                output.setRGB(x, y, image.getRGB(x, y));
            }
        }
        return output;
    }

    /**
     * Calculates the histograms for the red, green, blue, and gray components of the given image.
     *
     * @param image The image to calculate the histograms for.
     * @param numBins The number of bins in the histograms.
     * @return A 2D array representing the histograms for the red, green, blue, and gray components.
     */
    public static int[][] calculateCountIntensities(BufferedImage image, int numBins) {
        int[][] histogram = new int[4][numBins];
        int width = image.getWidth();
        int height = image.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = image.getRGB(x, y);
                int red = countColorsByBin(getRedByte(rgb), numBins);
                int green = countColorsByBin(getGreenByte(rgb), numBins);
                int blue = countColorsByBin(getBlueByte(rgb), numBins);
                int gray = countColorsByBin(getGradient(rgb), numBins);

                histogram[0][red]++;
                histogram[1][green]++;
                histogram[2][blue]++;
                histogram[3][gray]++;
            }
        }

        return histogram;
    }

    /**
     * Determines the bin index for a given color component value.
     *
     * @param rgb The color component value.
     * @param numBins The number of bins in the histogram.
     * @return The bin index for the color component value.
     */
    private static int countColorsByBin(int rgb, int numBins) {
        return (rgb * numBins) / 256;
    }


}
