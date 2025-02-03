package org.knu.bll.algorithms;

import org.knu.bll.ProgressListener;
import org.knu.bll.algorithms.edges.EdgeDetectionOperator;
import org.knu.bll.algorithms.blur.BlurFilter;
import org.knu.bll.helpers.ImageHelper;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * The CannyFilter class implements the Canny edge detection algorithm to detect edges in an image.
 * This class applies a series of steps including blur, gradient calculation, non-maximum suppression,
 * double thresholding and edge tracking by hysteresis.
 */
public class CannyFilter {

    /**
     * Applies the Canny edge detection algorithm to the given image.
     *
     * @param image The original image to which the Canny edge detection algorithm will be applied.
     *              This image should not be null.
     * @param lowThreshold The low threshold value for edge detection.
     * @param highThreshold The high threshold value for edge detection.
     * @param blurFilter The blur filter to apply Gaussian blur to the image.
     * @param kernelSize The size of the blur kernel.
     * @param edgeDetectionOperator The edge detection operator to calculate gradients.
     * @param listener A ProgressListener to receive progress updates during the Canny edge detection
     *                process. This listener should not be null.
     * @return A new BufferedImage that represents the edge-detected version of the original image.
     *         If the input image is null, the method should throw a NullPointerException.
     * @throws InterruptedException if the thread is interrupted during the process.
     * @throws NullPointerException if the image is null.
     * @throws NullPointerException if the listener is null.
     */
    public BufferedImage applyFilter(BufferedImage image, int lowThreshold, int highThreshold, BlurFilter blurFilter, int kernelSize,
                                     EdgeDetectionOperator edgeDetectionOperator, ProgressListener listener) throws InterruptedException {
        validateLowThreshold(lowThreshold);
        listener.onProgressStart(6);
        ProgressListener mockListener = new ProgressListener() {
            @Override
            public void onProgressStart(int maximum) {

            }

            @Override
            public void onProgressUpdate() {

            }
        };

        BufferedImage bufferedImage = ImageHelper.copyImage(image, BufferedImage.TYPE_INT_RGB);

        ImageHelper.convertToGrayscale(bufferedImage);
        listener.onProgressUpdate();

        if(blurFilter != null)
        bufferedImage = blurFilter.applyFilter(bufferedImage, kernelSize, mockListener);
        listener.onProgressUpdate();

        bufferedImage = edgeDetectionOperator.applyOperator(bufferedImage, mockListener);
        listener.onProgressUpdate();

        nonmaxSuppression(bufferedImage, edgeDetectionOperator.getGradientDirections());
        listener.onProgressUpdate();

        doubleThresholding(bufferedImage, highThreshold, lowThreshold);
        listener.onProgressUpdate();

        hysteresis(bufferedImage, 255, lowThreshold);
        listener.onProgressUpdate();

        return bufferedImage;
    }

    /**
     * Applies non-maximum suppression to the given image using the gradient directions.
     *
     * @param suppressedImage The image to apply non-maximum suppression to.
     * @param directions The gradient directions for each pixel.
     */
    private void nonmaxSuppression(BufferedImage suppressedImage, double[][] directions) {
        int width = suppressedImage.getWidth();
        int height = suppressedImage.getHeight();

        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                int currentMagnitude = ImageHelper.getGradient(suppressedImage.getRGB(x, y));
                double angle = Math.toDegrees(directions[x][y]) % 180;
                angle = (angle + 180) % 180; // Normalize angle to [0, 180]

                int neighbor1 = 0;
                int neighbor2 = 0;

                // Determine neighbor directions
                if ((angle >= 0 && angle < 22.5) || (angle >= 157.5 && angle <= 180)) {
                    neighbor1 = ImageHelper.getGradient(suppressedImage.getRGB(x - 1, y));
                    neighbor2 = ImageHelper.getGradient(suppressedImage.getRGB(x + 1, y));
                } else if (angle >= 22.5 && angle < 67.5) {
                    neighbor1 = ImageHelper.getGradient(suppressedImage.getRGB(x - 1, y - 1));
                    neighbor2 = ImageHelper.getGradient(suppressedImage.getRGB(x + 1, y + 1));
                } else if (angle >= 67.5 && angle < 112.5) {
                    neighbor1 = ImageHelper.getGradient(suppressedImage.getRGB(x, y - 1));
                    neighbor2 = ImageHelper.getGradient(suppressedImage.getRGB(x, y + 1));
                } else if (angle >= 112.5 && angle < 157.5) {
                    neighbor1 = ImageHelper.getGradient(suppressedImage.getRGB(x - 1, y + 1));
                    neighbor2 = ImageHelper.getGradient(suppressedImage.getRGB(x + 1, y - 1));
                }

                // Suppress non-maximum points
                if (currentMagnitude < neighbor1 || currentMagnitude < neighbor2) {
                    suppressedImage.setRGB(x, y, Color.BLACK.getRGB());
                }
            }
        }
    }

    /**
     * Applies double thresholding to the given image.
     *
     * @param image The image to apply double thresholding to.
     * @param highThreshold The high threshold value.
     * @param lowThreshold The low threshold value.
     */
    private void doubleThresholding(BufferedImage image, double highThreshold, double lowThreshold) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Iterate over each pixel in the image
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = ImageHelper.getGradient(image.getRGB(x, y));

                if (pixel >= highThreshold) {
                    // Strong edge
                    image.setRGB(x, y, Color.WHITE.getRGB());
                } else if (pixel >= lowThreshold) {
                    // Weak edge
                    image.setRGB(x, y, Color.RED.getRGB());
                } else {
                    // Suppressed pixel
                    image.setRGB(x, y, Color.BLACK.getRGB());
                }
            }
        }
    }

    /**
     * Applies hysteresis thresholding to the given image.
     *
     * @param output The image to apply hysteresis thresholding to.
     * @param highThreshold The high threshold value.
     * @param lowThreshold The low threshold value.
     */
    private void hysteresis(BufferedImage output, int highThreshold, int lowThreshold) {
        int width = output.getWidth();
        int height = output.getHeight();

        // Iterate over each pixel in the image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Get the brightness value of the current pixel
                int brightness = ImageHelper.getGradient(output.getRGB(x, y));

                // If the pixel is weak and has no strong neighbors, make it black
                if (brightness < lowThreshold) {
                    output.setRGB(x, y, Color.BLACK.getRGB());
                }
                // If the pixel is strong, keep it white
                else if (brightness >= highThreshold) {
                    output.setRGB(x, y, Color.WHITE.getRGB());
                }
                // If the pixel is weak and has a strong neighbor, make it white
                else {
                    if (isConnectedToStrongPixel(output, x, y, width, height, highThreshold)) {
                        output.setRGB(x, y, Color.WHITE.getRGB());
                    } else {
                        output.setRGB(x, y, Color.BLACK.getRGB());
                    }
                }
            }
        }
    }

    /**
     * Checks if a weak pixel is connected to a strong pixel.
     *
     * @param image The image to check.
     * @param x The x-coordinate of the pixel.
     * @param y The y-coordinate of the pixel.
     * @param width The width of the image.
     * @param height The height of the image.
     * @param highThreshold The high threshold value.
     * @return True if the weak pixel is connected to a strong pixel, false otherwise.
     */
    private boolean isConnectedToStrongPixel(BufferedImage image, int x, int y, int width, int height, int highThreshold) {
        // Check 8 neighbors for the pixel (x, y)
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int nx = x + dx;
                int ny = y + dy;

                // Skip out-of-bounds neighbors
                if (nx >= 0 && ny >= 0 && nx < width && ny < height) {
                    int neighborBrightness = ImageHelper.getGradient(image.getRGB(nx, ny));
                    // If the neighbor is strong, return true
                    if (neighborBrightness >= highThreshold) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Validates the low threshold value.
     *
     * @param range The low threshold value.
     * @throws IllegalArgumentException if the low threshold value is not between 0 and 255.
     */
    private void validateLowThreshold(int range) {
        if (range < 0 || range >= 255) {
            throw new IllegalArgumentException("LowThreshold must be between 0 and 255");
        }
    }
}
