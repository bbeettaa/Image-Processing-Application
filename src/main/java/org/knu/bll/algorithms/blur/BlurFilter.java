package org.knu.bll.algorithms.blur;

import org.knu.bll.ProgressListener;

import java.awt.image.BufferedImage;

/**
 * The BlurFilter interface defines a contract for applying a blur effect to an image.
 * Implementations of this interface should provide the logic for blurring an image
 * using a specified kernel size and optionally reporting progress through a listener.
 */
public interface BlurFilter {

    /**
     * Applies a blur filter to the given image.
     *
     * @param image The original image to which the blur filter will be applied.
     *              This image should not be null.
     * @param kernelSize The size of the blur kernel. This value determines the extent
     *                   of the blur effect. A larger kernel size results in a more pronounced blur.
     * @param listener A ProgressListener to receive progress updates during the blur
     *                 operation.
     * @return A new BufferedImage that represents the blurred version of the original image.
     */
    BufferedImage applyFilter(BufferedImage image, int kernelSize, ProgressListener listener);
}
