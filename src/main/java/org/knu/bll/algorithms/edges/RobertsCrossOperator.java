package org.knu.bll.algorithms.edges;

import org.knu.bll.ProgressListener;
import org.knu.bll.helpers.ImageHelper;

import java.awt.image.BufferedImage;

/**
 * The RobertsCrossOperator class implements the EdgeDetectionOperator interface to apply the Roberts Cross edge
 * detection algorithm to an image. This class uses the Roberts Cross operator to detect edges by calculating the
 * gradient of image intensity.
 *
 * @author Your Name
 * @version 1.0
 * @since 2023-10-01
 */
public class RobertsCrossOperator implements EdgeDetectionOperator {

    private final int[][] GX = {
            {1, 0},
            {0, -1}
    };
    private final int[][] GY = {
            {0, 1},
            {-1, 0}
    };
    private double[][] gradientDirection;

    /**
     * Applies the Roberts Cross edge detection operator to the given image.
     *
     * @param image The original image to which the edge detection operator will be applied.
     *              This image should not be null.
     * @param listener A ProgressListener to receive progress updates during the edge detection
     *                 operation. This listener should not be null.
     * @return A new BufferedImage that represents the edge-detected (gradient magnitude) version of the original image.
     *         If the input image is null, the method should throw a NullPointerException.
     * @throws NullPointerException if the image is null.
     * @throws NullPointerException if the listener is null.
     */
    @Override
    public BufferedImage applyOperator(BufferedImage image, ProgressListener listener) {
        if (image == null) throw new NullPointerException("Image cannot be null");
        if (listener == null)  throw new NullPointerException("Listener cannot be null");


        int width = image.getWidth();
        int height = image.getHeight();
        gradientDirection = new double[width][height];

        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        listener.onProgressStart(width);
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                double px = 0;
                double py = 0;
                // Apply masks to get gradients
                for (int nx = 0; nx <= 1; nx++) {
                    for (int ny = 0; ny <= 1; ny++) {
                        int brightness = ImageHelper.getGradient(image.getRGB(x + nx - 1, y + ny - 1));
                        px += brightness * GX[nx][ny];
                        py += brightness * GY[nx][ny];
                    }
                }

                int gradient = Math.min(255, (int) Math.sqrt(px * px + py * py));
                outputImage.setRGB(x, y, (gradient << 16) | (gradient << 8) | gradient);

                gradientDirection[x][y] = Math.atan(py / px) - ((3 * Math.PI) / 4);
            }
            listener.onProgressUpdate();
        }

        return outputImage;
    }

    /**
     * Returns the gradient directions calculated during the edge detection process.
     *
     * @return A 2D array representing the gradient directions for each pixel.
     */
    @Override
    public double[][] getGradientDirections() {
        return gradientDirection;
    }


    /**
     * Returns a string representation of the Roberts Cross edge detection operator.
     *
     * @return The string "Roberts".
     */
    @Override
    public String toString() {
        return "Roberts";
    }
}
