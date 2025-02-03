package org.knu.bll.algorithms.clustering;

import org.knu.bll.ProgressListener;
import org.knu.bll.helpers.ImageHelper;

import java.awt.image.BufferedImage;

/**
 * The OtsuThresholding class implements the Cluster interface to apply Otsu's thresholding method to an image.
 * This class converts an image to grayscale and then applies Otsu's thresholding to binarize the image.
 * Otsu's method is an adaptive thresholding technique that automatically determines the optimal threshold value
 * by maximizing the variance between two classes of pixels (foreground and background).
 *
 * @author Your Name
 * @version 1.0
 * @since 2023-10-01
 */
public class OtsuThresholding implements Cluster {

    /**
     * Applies Otsu's thresholding to the given image.
     *
     * @param image The original image to which the thresholding will be applied.
     *              This image should not be null.
     * @param listener A ProgressListener to receive progress updates during the thresholding
     *                 operation. This listener should not be null.
     * @return A new BufferedImage that represents the binarized version of the original image.
     *         If the input image is null, the method should throw a NullPointerException.
     * @throws NullPointerException if the image is null.
     * @throws NullPointerException if the listener is null.
     */
    @Override
    public BufferedImage applyCluster(BufferedImage image, ProgressListener listener) {
        if (image == null) throw new NullPointerException("Image cannot be null");
        if (listener == null) throw new NullPointerException("Listener cannot be null");

        listener.onProgressStart(5);

        BufferedImage bufferedImage = ImageHelper.copyImage(image);
        ImageHelper.grayscale(bufferedImage);
        listener.onProgressUpdate();


        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        // Calculate histogram
        int[] histogram = ImageHelper.calculateCountIntensities(bufferedImage, 256)[3];
        listener.onProgressUpdate();

        // Calculate probabilities
        int totalPixels = width * height;
        double[] probabilities = calculateProbabilities(histogram, totalPixels);
        listener.onProgressUpdate();

        // Calculate threshold value
        int threshold = calculateThreshold(probabilities);
        listener.onProgressUpdate();

        // Apply threshold value
        bufferedImage =  applyThreshold(bufferedImage, width, height, threshold);
        listener.onProgressUpdate();

        return bufferedImage;
    }


    /**
     * Applies the calculated threshold value to the grayscale image to create a binary image.
     *
     * @param bufferedImage The grayscale image to which the thresholding will be applied.
     * @param width The width of the image.
     * @param height The height of the image.
     * @param threshold The threshold value to be applied.
     * @return A new BufferedImage that represents the binarized version of the grayscale image.
     */
    private static BufferedImage applyThreshold(BufferedImage bufferedImage, int width, int height, int threshold) {
        BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int gray = (bufferedImage.getRGB(x, y) >> 16) & 0xFF;
                int binary = (gray >= threshold) ? 0xFFFFFF : 0x000000;
                binaryImage.setRGB(x, y, binary);
            }
        }
        return binaryImage;
    }

    /**
     * Calculates the optimal threshold value using Otsu's method.
     *
     * @param probabilities The probabilities of each intensity level.
     * @return The optimal threshold value.
     */
    private int calculateThreshold( double[] probabilities) {
        double sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += i * probabilities[i];
        }

        double sumB = 0;
        double wB = 0;
        double wF = 0;
        double maxVariance = 0;
        int threshold = 0;

        for (int i = 0; i < 256; i++) {
            wB += probabilities[i];
            wF = 1 - wB;
            if (wB == 0 || wF == 0) {
                continue;
            }
            sumB += i * probabilities[i];
            double mB = sumB / wB;
            double mF = (sum - sumB) / wF;
            double variance = wB * wF * Math.pow(mB - mF, 2);
            if (variance > maxVariance) {
                maxVariance = variance;
                threshold = i;
            }
        }
        return threshold;
    }

    /**
     * Calculates the probabilities of each intensity level in the histogram.
     *
     * @param histogram The histogram of the image.
     * @param totalPixels The total number of pixels in the image.
     * @return An array of probabilities for each intensity level.
     */
    private double[] calculateProbabilities(int[] histogram, int totalPixels) {
        double[] probabilities = new double[256];
        for (int i = 0; i < 256; i++) {
            probabilities[i] = (double) histogram[i] / totalPixels;
        }
        return probabilities;
    }

    /**
     * Returns a string representation of the Otsu thresholding algorithm.
     *
     * @return The string "Otsu".
     */
    @Override
    public String toString() {
        return "Otsu";
    }
}
