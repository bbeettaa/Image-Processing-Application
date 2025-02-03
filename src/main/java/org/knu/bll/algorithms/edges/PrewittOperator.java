package org.knu.bll.algorithms.edges;

import org.knu.bll.ProgressListener;
import org.knu.bll.helpers.ImageHelper;

import java.awt.image.BufferedImage;

/**
 * The PrewittOperator class implements the EdgeDetectionOperator interface to apply the Prewitt edge detection
 * algorithm to an image. This class uses the Prewitt operator to detect edges by calculating the gradient of
 * image intensity.
 *
 * @author Your Name
 * @version 1.0
 * @since 2023-10-01
 */
public class PrewittOperator implements EdgeDetectionOperator {
    private final int[][] GX;
    private final int[][] GY;
    private double[][] gradientDirections;

    /**
     * Constructs a PrewittOperator with predefined Prewitt masks for gradient calculation.
     */
    public PrewittOperator() {
        GX = new int[][]{
                {1, 0, -1},
                {1, 0, -1},
                {1, 0, -1}
        };
        GY = new int[][]{
                {1, 1, 1},
                {0, 0, 0},
                {-1, -1, -1}
        };
    }

    /**
     * Applies the Prewitt edge detection operator to the given image.
     *
     * @param image    The original image to which the edge detection operator will be applied.
     *                 This image should not be null.
     * @param listener A ProgressListener to receive progress updates during the edge detection
     *                 operation. This listener should not be null.
     * @return A new BufferedImage that represents the edge-detected (gradient magnitude) version of the original image.
     * If the input image is null, the method should throw a NullPointerException.
     * @throws NullPointerException if the image is null.
     * @throws NullPointerException if the listener is null.
     */
    @Override
    public BufferedImage applyOperator(BufferedImage image, ProgressListener listener) {
        if (image == null) throw new NullPointerException("Image cannot be null");
        if (listener == null) throw new NullPointerException("Listener cannot be null");


        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        gradientDirections = new double[width][height];

        listener.onProgressStart(width);
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                processPixel(image, outputImage, x, y);
            }
            listener.onProgressUpdate();
        }
        return outputImage;
    }

    /**
     * Processes a single pixel to calculate its gradient using the Prewitt operator.
     *
     * @param image       The original image.
     * @param outputImage The image to store the edge-detected result.
     * @param x           The x-coordinate of the pixel.
     * @param y           The y-coordinate of the pixel.
     */
    private void processPixel(BufferedImage image, BufferedImage outputImage, int x, int y) {
        int px = 0;
        int py = 0;

        // Apply masks to get gradients
        for (int nx = -1; nx <= 1; nx++) {
            for (int ny = -1; ny <= 1; ny++) {
                int brightness = ImageHelper.getGradient(image.getRGB(x + nx, y + ny));
                px += brightness * GX[nx + 1][ny + 1];
                py += brightness * GY[nx + 1][ny + 1];
            }
        }

        allocateBorder(outputImage, x, y, px, py);
        gradientDirections[x][y] = Math.atan2(px, py);
    }

    /**
     * Allocates the border pixel with the calculated gradient magnitude.
     *
     * @param outputImage The image to store the edge-detected result.
     * @param x           The x-coordinate of the pixel.
     * @param y           The y-coordinate of the pixel.
     * @param px          The gradient in the x-direction.
     * @param py          The gradient in the y-direction.
     */
    private void allocateBorder(BufferedImage outputImage, int x, int y, int px, int py) {
        int gradient = (int) Math.min(255, Math.hypot(px, py));
        int newPixel = (gradient << 16) | (gradient << 8) | gradient;
        outputImage.setRGB(x, y, newPixel);
    }

    /**
     * Returns the gradient directions calculated during the edge detection process.
     *
     * @return A 2D array representing the gradient directions for each pixel.
     */
    @Override
    public double[][] getGradientDirections() {
        return gradientDirections;
    }


    /**
     * Returns a string representation of the Prewitt edge detection operator.
     *
     * @return The string "Prewitt".
     */
    @Override
    public String toString() {
        return "Prewitt";
    }
}
