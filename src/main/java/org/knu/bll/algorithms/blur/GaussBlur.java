package org.knu.bll.algorithms.blur;

import org.knu.bll.ProgressListener;
import org.knu.bll.helpers.ImageHelper;

import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * The GaussBlur class implements the BlurFilter interface to apply a Gaussian blur effect to an image.
 * This class uses a Gaussian kernel to blur the image, where each pixel's color is weighted by the kernel values.
 */
public class GaussBlur implements BlurFilter {
    private double sigma;

    /**
     * Constructs a GaussBlur object with a default sigma value of 1.
     */
    public GaussBlur() {
        this.sigma = 1;
    }

    /**
     * Applies a Gaussian blur filter to the given image.
     *
     * @param image The original image to which the blur filter will be applied.
     *              This image should not be null.
     * @param kernelSize The size of the blur kernel. This value determines the extent
     *                   of the blur effect. A larger kernel size results in a more pronounced blur.
     *                   The kernel size should be a positive odd integer.
     * @param listener A ProgressListener to receive progress updates during the blur
     *                 operation.
     * @return A new BufferedImage that represents the blurred version of the original image.
     *         If the input image is null, the method should return null.
     * @throws IllegalArgumentException if the kernelSize is not a positive odd integer.
     * @throws NullPointerException if the image is null.
     */
    @Override
    public BufferedImage applyFilter(BufferedImage image, int kernelSize, ProgressListener listener) {
        validateFilterSize(kernelSize);
        listener.onProgressStart(image.getWidth());
        double[] kernel = createGaussianKernel(kernelSize, sigma);
        return applyGaussianBlur(image, kernel, kernelSize, listener);
    }

    /**
     * Applies a Gaussian blur to the given image using the specified kernel.
     *
     * @param image The original image to which the blur filter will be applied.
     * @param kernel The Gaussian kernel to be used for blurring.
     * @param kernelSize The size of the blur kernel.
     * @param listener A ProgressListener to receive progress updates during the blur
     *                 operation. This can be null if progress reporting is not required.
     * @return A new BufferedImage that represents the blurred version of the original image.
     */
    private BufferedImage applyGaussianBlur(BufferedImage image, double[] kernel, int kernelSize, ProgressListener listener) {
        int radius = kernelSize / 2;
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage blurredImage = ImageHelper.copyImage(image, BufferedImage.TYPE_INT_ARGB);

        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
        int[] blurredPixels = applyGaussianBlurToArray(pixels, width, height, kernel, radius);
        blurredImage.setRGB(0, 0, width, height, blurredPixels, 0, width);

        listener.onProgressUpdate();
        return blurredImage;
    }

    /**
     * Applies a Gaussian blur to an array of pixels using the specified kernel.
     *
     * @param pixels The array of pixels to which the blur filter will be applied.
     * @param width The width of the image.
     * @param height The height of the image.
     * @param kernel The Gaussian kernel to be used for blurring.
     * @param radius The radius of the blur kernel.
     * @return An array of blurred pixels.
     */
    private int[] applyGaussianBlurToArray(int[] pixels, int width, int height, double[] kernel, int radius) {
        int[] blurredPixels = new int[pixels.length];
        Arrays.fill(blurredPixels, 0);

        for (int x = 0; x < width; x++) {
            applyGaussianKernelToArray(pixels, blurredPixels, width, height, kernel, radius, x);
        }
        return blurredPixels;
    }

    /**
     * Applies the Gaussian kernel to a specific column of pixels.
     *
     * @param pixels The array of pixels to which the blur filter will be applied.
     * @param blurredPixels The array to store the blurred pixels.
     * @param width The width of the image.
     * @param height The height of the image.
     * @param kernel The Gaussian kernel to be used for blurring.
     * @param radius The radius of the blur kernel.
     * @param x The current column index.
     */
    private void applyGaussianKernelToArray(int[] pixels, int[] blurredPixels, int width, int height,
                                            double[] kernel, int radius, int x) {
        for (int y = 0; y < height; y++) {
            double[] colorChannelsArr = new double[4];
            int kernelIndex = 0;

            for (int kx = -radius; kx <= radius; kx++) {
                for (int ky = -radius; ky <= radius; ky++) {
                    int px = x + kx;
                    int py = y + ky;

                    if (px >= 0 && px < width && py >= 0 && py < height)
                        getAndIncrementColorChannels(colorChannelsArr, pixels[py * width + px], kernel[kernelIndex]);
                    kernelIndex++;
                }
            }
            blurredPixels[y * width + x] = createColorFromChannels(colorChannelsArr);
        }
    }

    /**
     * Creates an ARGB color value from the given color channels.
     *
     * @param colorChannelsArr The array containing the color channels (alpha, red, green, blue).
     * @return An integer representing the ARGB color value.
     */
    private static int createColorFromChannels(double[] colorChannelsArr) {
        int alpha = Math.min(255, Math.max(0, (int) colorChannelsArr[0]));
        int red = Math.min(255, Math.max(0, (int) colorChannelsArr[1]));
        int green = Math.min(255, Math.max(0, (int) colorChannelsArr[2]));
        int blue = Math.min(255, Math.max(0, (int) colorChannelsArr[3]));
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    /**
     * Increments the color channels array with the weighted color values from the given RGB value.
     *
     * @param colorChannelsArr The array containing the color channels (alpha, red, green, blue).
     * @param rgb The RGB value to be added to the color channels.
     * @param kernel The weight of the kernel value.
     */
    private static void getAndIncrementColorChannels(double[] colorChannelsArr, int rgb, double kernel) {
        colorChannelsArr[0] += ((rgb >> 24) & 0xff) * kernel;
        colorChannelsArr[1] += ((rgb >> 16) & 0xff) * kernel;
        colorChannelsArr[2] += ((rgb >> 8) & 0xff) * kernel;
        colorChannelsArr[3] += (rgb & 0xff) * kernel;
    }

    /**
     * Creates a Gaussian kernel of the specified size and sigma value.
     *
     * @param size The size of the kernel.
     * @param sigma The standard deviation of the Gaussian distribution.
     * @return A double array representing the Gaussian kernel.
     */
    private double[] createGaussianKernel(int size, double sigma) {
        double[] kernel = new double[size * size];
        int radius = size / 2;
        double sum = 0.0;

        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                double value = Math.exp(-(x * x + y * y) / (2 * sigma * sigma));
                kernel[(y + radius) * size + (x + radius)] = value;
                sum += value;
            }
        }

        for (int i = 0; i < kernel.length; i++) {
            kernel[i] /= sum;
        }
        return kernel;
    }

    /**
     * Validates that the filter size is a positive odd integer.
     *
     * @param kernelSize The size of the filter kernel.
     * @throws IllegalArgumentException if the kernelSize is not a positive odd integer.
     */
    private void validateFilterSize(int kernelSize) {
        if (kernelSize <= 0 || kernelSize % 2 == 0) {
            throw new IllegalArgumentException("Kernel size must be a positive odd number");
        }
    }

    /**
     * Sets the sigma value for the Gaussian blur.
     *
     * @param sigma The standard deviation of the Gaussian distribution.
     */
    public void setSigma(double sigma) {
        this.sigma = sigma;
    }

    /**
     * Returns a string representation of the Gaussian blur filter.
     *
     * @return The string "Gaussian blur".
     */
    @Override
    public String toString() {
        return "Gaussian blur";
    }
}
