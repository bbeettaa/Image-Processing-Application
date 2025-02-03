package org.knu.bll.algorithms.colors;

import org.knu.bll.helpers.ImageHelper;

import java.awt.image.BufferedImage;

/**
 * The GrayColorFilter class implements the ColorFilter interface to apply a grayscale filter to an image.
 * This class converts an image to grayscale by averaging the red, green, and blue color components of each pixel.
 */
public class GrayColorFilter implements ColorFilter {

    /**
     * Applies a grayscale filter to the given image.
     *
     * @param image The original image to which the grayscale filter will be applied.
     *              This image should not be null.
     * @return A new BufferedImage that represents the grayscale version of the original image.
     *         If the input image is null, the method should throw a NullPointerException.
     * @throws NullPointerException if the image is null.
     */
    @Override
    public BufferedImage applyColorFilter(BufferedImage image) {
        if (image == null) throw new NullPointerException("Image cannot be null");
        BufferedImage output = ImageHelper.copyImage(image, BufferedImage.TYPE_INT_ARGB);
        ImageHelper.grayscale(output);
        return output;
    }

    /**
     * Returns a string representation of the grayscale filter.
     *
     * @return The string "Gray".
     */
    @Override
    public String toString() {
        return "Gray";
    }
}
