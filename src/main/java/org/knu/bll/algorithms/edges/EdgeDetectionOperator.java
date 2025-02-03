package org.knu.bll.algorithms.edges;

import org.knu.bll.ProgressListener;

import java.awt.image.BufferedImage;

/**
 * The EdgeDetectionOperator interface defines a contract for applying edge detection algorithms to an image.
 * Implementations of this interface should provide the logic for detecting edges in an image and optionally
 * reporting progress through a listener.
 */
public interface EdgeDetectionOperator {

    /**
     * Applies an edge detection operator to the given image.
     *
     * @param image The original image to which the edge detection operator will be applied.
     *              This image should not be null.
     * @param listener A ProgressListener to receive progress updates during the edge detection
     *                 operation. This listener should not be null.
     * @return A new BufferedImage that represents the gradient magnitudes of the original image.
     *         If the input image is null, the method should throw a NullPointerException.
     * @throws NullPointerException if the image is null.
     * @throws NullPointerException if the listener is null.
     */
    BufferedImage applyOperator(BufferedImage image, ProgressListener listener);

    /**
     * Returns the gradient directions calculated during the edge detection process.
     *
     * @return A 2D array representing the gradient directions for each pixel.
     */
    double[][] getGradientDirections();

}
