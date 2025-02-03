package org.knu.bll.algorithms.colors;

import java.awt.image.BufferedImage;

/**
 * The ColorFilter interface defines a contract for applying color filters to an image.
 * Implementations of this interface should provide the logic for modifying the colors of an image
 * according to a specific filtering algorithm.
 */
public interface ColorFilter {

    /**
     * Applies a color filter to the given image.
     *
     * @param image The original image to which the color filter will be applied.
     *              This image should not be null.
     * @return A new BufferedImage that represents the filtered version of the original image.
     *         If the input image is null, the method should throw a NullPointerException.
     * @throws NullPointerException if the image is null.
     */
    BufferedImage applyColorFilter(BufferedImage image);
}
