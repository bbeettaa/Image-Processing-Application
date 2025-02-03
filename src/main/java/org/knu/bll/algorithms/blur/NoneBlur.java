package org.knu.bll.algorithms.blur;

import org.knu.bll.ProgressListener;

import java.awt.image.BufferedImage;

/**
 * The NoneBlur class implements the BlurFilter interface to use this class like a stub in Kenny filter.
 */
public class NoneBlur implements BlurFilter {
    /**
     * Applies a blur filter to the given image.
     *
     * @param image      The original image to which the blur filter will be applied.
     *                   This image should not be null.
     * @param kernelSize The size of the blur kernel. This value determines the extent
     *                   of the blur effect. A larger kernel size results in a more pronounced blur.
     * @param listener   A ProgressListener to receive progress updates during the blur
     *                   operation.
     * @return A new BufferedImage that represents the blurred version of the original image.
     */
    @Override
    public BufferedImage applyFilter(BufferedImage image, int kernelSize, ProgressListener listener) {
        return image;
    }

    @Override
    public String toString() {
        return "None";
    }
}
