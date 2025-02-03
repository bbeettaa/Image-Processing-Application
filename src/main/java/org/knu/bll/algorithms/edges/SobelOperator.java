package org.knu.bll.algorithms.edges;

import org.knu.bll.ProgressListener;
import org.knu.bll.helpers.ImageHelper;

import java.awt.image.BufferedImage;

/**
 * The SobelOperator class implements the EdgeDetectionOperator interface to apply the Sobel edge detection
 * algorithm to an image. This class uses the Sobel operator to detect edges by calculating the gradient of
 * image intensity.
 *
 * @author Your Name
 * @version 1.0
 * @since 2023-10-01
 */
public class SobelOperator implements EdgeDetectionOperator {

    private final int[][] GX;
    private final int[][] GY;
    private double[][] gradientDirections;

    /**
     * Constructs a SobelOperator with predefined Sobel masks for gradient calculation.
     */
    public SobelOperator() {
        GX = new int[][]{
                {1, 0, -1},
                {2, 0, -2},
                {1, 0, -1}
        };
        GY = new int[][]{
                {1, 2, 1},
                {0, 0, 0},
                {-1, -2, -1}
        };
    }

    /**
     * Applies the Sobel edge detection operator to the given image.
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
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

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
     * Processes a single pixel to calculate its gradient using the Sobel operator.
     *
     * @param image       The original image.
     * @param outputImage The image to store the edge-detected result.
     * @param x           The x-coordinate of the pixel.
     * @param y           The y-coordinate of the pixel.
     */
    public void processPixel(BufferedImage image, BufferedImage outputImage, int x, int y) {
        int px = 0, py = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int pixel = ImageHelper.getGradient(image.getRGB(x + i, y + j));
                px += GX[i + 1][j + 1] * pixel;
                py += GY[i + 1][j + 1] * pixel;
            }
        }

        allocateBorder(outputImage, x, y, px, py);
        double angle = Math.atan2(px, py);
        gradientDirections[x][y] = angle;
    }

    /**
     * Normalizes the gradient angle to the nearest standard angle (0°, 45°, 90°, 135°).
     *
     * @param angle The gradient angle in radians.
     * @return The normalized gradient angle in degrees.
     */
    private double normalizeAngle(double angle) {
        angle = Math.toDegrees(angle); // Convert to degrees
        if (angle < 0) {
            angle += 180;
        }

        if (angle < 22.5 || angle >= 157.5) {
            return 0.0;
        } else if (angle >= 22.5 && angle < 67.5) {
            return 45.0;
        } else if (angle >= 67.5 && angle < 112.5) {
            return 90.0;
        } else {
            return 135.0;
        }
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
        int magnitude = (int) Math.min(255, Math.hypot(px, py));
        outputImage.setRGB(x, y, ImageHelper.byteToRGB(magnitude));
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
     * Returns a string representation of the Sobel edge detection operator.
     *
     * @return The string "Sobel".
     */
    @Override
    public String toString() {
        return "Sobel";
    }
}
