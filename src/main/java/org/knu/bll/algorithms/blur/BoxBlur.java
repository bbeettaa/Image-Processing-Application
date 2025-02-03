package org.knu.bll.algorithms.blur;

import org.knu.bll.ProgressListener;
import org.knu.bll.helpers.ImageHelper;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import java.awt.image.BufferedImage;

/**
 * The BoxBlur class implements the BlurFilter interface to apply a box blur effect to an image.
 * This class uses a simple averaging technique to blur the image, where each pixel's color is
 * averaged with its neighboring pixels within a specified kernel size.
 */
public class BoxBlur implements BlurFilter {

    /**
     * Applies a box blur filter to the given image.
     *
     * @param image The original image to which the blur filter will be applied.
     *              This image should not be null.
     * @param kernelSize The size of the blur kernel. This value determines the extent
     *                   of the blur effect. A larger kernel size results in a more pronounced blur.
     *                   The kernel size should be a positive odd integer.
     * @param listener A ProgressListener to receive progress updates during the blur
     *                 operation.
     * @return A new BufferedImage that represents the blurred version of the original image.
     * @throws IllegalArgumentException if the kernelSize is not a positive odd integer.
     * @throws NullPointerException if the image is null.
     */
    @Override
    public BufferedImage applyFilter(BufferedImage image, int kernelSize, ProgressListener listener) {
        validateFilterSize(kernelSize);

        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage blurredImage = ImageHelper.copyImage(image, BufferedImage.TYPE_INT_RGB);
        listener.onProgressStart(image.getWidth());

        int radius = kernelSize / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int sumRed = 0, sumGreen = 0, sumBlue = 0;

                for (int ky = -radius; ky <= radius; ky++) {
                    for (int kx = -radius; kx <= radius; kx++) {
                        int px = Math.min(Math.max(x + kx, 0), width - 1);
                        int py = Math.min(Math.max(y + ky, 0), height - 1);
                        int rgb = image.getRGB(px, py);

                        sumRed += ImageHelper.getRedByte(rgb);
                        sumGreen += ImageHelper.getGreenByte(rgb);
                        sumBlue += ImageHelper.getBlueByte(rgb);
                    }
                }
                blurredImage.setRGB(x, y, getAverageRgb(kernelSize, sumRed, sumGreen, sumBlue));
            }
            listener.onProgressUpdate();
        }

        return blurredImage;
    }

    /**
     * Calculates the average RGB value for a given kernel size and sum of color components.
     *
     * @param kernelSize The size of the blur kernel.
     * @param sumRed The sum of the red color components within the kernel.
     * @param sumGreen The sum of the green color components within the kernel.
     * @param sumBlue The sum of the blue color components within the kernel.
     * @return An integer representing the average RGB color value.
     */
    private int getAverageRgb(int kernelSize, int sumRed, int sumGreen, int sumBlue) {
        int count = kernelSize * kernelSize;

        int avgRed = sumRed / count;
        int avgGreen = sumGreen / count;
        int avgBlue = sumBlue / count;
        return (avgRed << 16) | (avgGreen << 8) | avgBlue;
    }

    /**
     * Validates that the filter size is a positive odd integer.
     *
     * @param size The size of the filter kernel.
     * @throws IllegalArgumentException if the size is not a positive odd integer.
     */
    private void validateFilterSize(int size) {
        if (size <= 0 || size % 2 == 0) {
            throw new IllegalArgumentException("Filter size must be a positive odd number");
        }
    }

    /**
     * Returns a string name for the filter.
     *
     * @return The string "Box blur".
     */
    @Override
    public String toString() {
        return "Box blur";
    }
}
