package org.knu.bll.algorithms.histograms;

import org.knu.bll.ProgressListener;
import org.knu.bll.helpers.ImageHelper;

import java.awt.image.BufferedImage;

/**
 * The HistogramEqualizerProcessor class implements histogram equalization to enhance the contrast of an image.
 * This class equalizes the histograms of the specified color channels to achieve a balanced gray level distribution.
 */
public class HistogramEqualizerProcessor {

    /**
     * Applies histogram equalization to the given image.
     *
     * @param original           The original image to which histogram equalization will be applied.
     *                           This image should not be null.
     * @param channelsToEqualize A boolean array indicating which color channels to equalize.
     * @param listener           A ProgressListener to receive progress updates during the histogram equalization
     *                           process. This listener should not be null.
     * @return A new BufferedImage that represents the histogram-equalized version of the original image.
     * If the input image is null, the method should throw a NullPointerException.
     * @throws NullPointerException if the original image is null.
     * @throws NullPointerException if the listener is null.
     */
    public BufferedImage histogramEqualization(BufferedImage original, boolean[] channelsToEqualize, ProgressListener listener) {
        int width = original.getWidth();
        int height = original.getHeight();
        int totalPixels = width * height;
        listener.onProgressStart(5);

        int[][] histograms = ImageHelper.calculateCountIntensities(original, 256);
        listener.onProgressUpdate();

        double[][] normalizedHistograms = normalizeHistogram(channelsToEqualize, histograms, totalPixels);
        listener.onProgressUpdate();

        double[][] cdfs = calculateCDF(channelsToEqualize, normalizedHistograms);
        listener.onProgressUpdate();

        int[][] newValues = getNewIntensities(channelsToEqualize, cdfs);
        listener.onProgressUpdate();

        BufferedImage equalizedImage = applyHistogram(original, channelsToEqualize, width, height, newValues);
        listener.onProgressUpdate();

        return equalizedImage;
    }

    /**
     * Applies the histogram equalization to the original image using the new intensity values.
     *
     * @param original           The original image.
     * @param channelsToEqualize A boolean array indicating which color channels to equalize.
     * @param width              The width of the image.
     * @param height             The height of the image.
     * @param newValues          The new intensity values for each color channel.
     * @return A new BufferedImage that represents the histogram-equalized version of the original image.
     */
    private BufferedImage applyHistogram(BufferedImage original, boolean[] channelsToEqualize, int width, int height, int[][] newValues) {
        BufferedImage equalizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = original.getRGB(x, y);

                int red = ImageHelper.getRedByte(rgb);
                int green = ImageHelper.getGreenByte(rgb);
                int blue = ImageHelper.getBlueByte(rgb);

                int newRed = channelsToEqualize[0] ? newValues[0][red] : red;
                int newGreen = channelsToEqualize[1] ? newValues[1][green] : green;
                int newBlue = channelsToEqualize[2] ? newValues[2][blue] : blue;

                int newRGB = (newRed << 16) | (newGreen << 8) | newBlue;
                equalizedImage.setRGB(x, y, newRGB);
            }
        }
        return equalizedImage;
    }

    /**
     * Computes the new intensity values for each color channel based on the CDFs.
     *
     * @param channelsToEqualize A boolean array indicating which color channels to equalize.
     * @param cdfs               The CDFs for each color channel.
     * @return A 2D array representing the new intensity values for each color channel.
     */
    private int[][] getNewIntensities(boolean[] channelsToEqualize, double[][] cdfs) {
        int[][] newValues = new int[4][256];
        for (int i = 0; i < 4; i++) {
            if (channelsToEqualize[i])
                newValues[i] = computeNewIntensityValues(cdfs[i]);
        }
        return newValues;
    }

    /**
     * Calculates the Cumulative Distribution Functions (CDFs) for the normalized histograms.
     *
     * @param channelsToEqualize   A boolean array indicating which color channels to equalize.
     * @param normalizedHistograms The normalized histograms for each color channel.
     * @return A 2D array representing the CDFs for each color channel.
     */
    private  double[][] calculateCDF(boolean[] channelsToEqualize, double[][] normalizedHistograms) {
        double[][] cdfs = new double[4][256];
        for (int i = 0; i < 4; i++) {
            if (channelsToEqualize[i])
                cdfs[i] = computeCDF(normalizedHistograms[i]);
        }
        return cdfs;
    }

    /**
     * Normalizes the histograms for the specified color channels.
     *
     * @param channelsToEqualize A boolean array indicating which color channels to equalize.
     * @param histograms         The histograms for each color channel.
     * @param totalPixels        The total number of pixels in the image.
     * @return A 2D array representing the normalized histograms for each color channel.
     */
    private double[][] normalizeHistogram(boolean[] channelsToEqualize, int[][] histograms, int totalPixels) {
        double[][] normalizedHistograms = new double[4][256];
        for (int i = 0; i < 4; i++) {
            if (channelsToEqualize[i])
                normalizedHistograms[i] = normalizeHistogram(histograms[i], totalPixels);
        }
        return normalizedHistograms;
    }

    /**
     * Normalizes a histogram by dividing each bin by the total number of pixels.
     *
     * @param histogram   The histogram to be normalized.
     * @param totalPixels The total number of pixels in the image.
     * @return A normalized histogram.
     */
    private double[] normalizeHistogram(int[] histogram, int totalPixels) {
        double[] normalizedHistogram = new double[histogram.length];
        for (int i = 0; i < histogram.length; i++) {
            normalizedHistogram[i] = (double) histogram[i] / totalPixels;
        }
        return normalizedHistogram;
    }

    /**
     * Computes the new intensity values based on the CDF.
     *
     * @param cdf The CDF for a color channel.
     * @return An array representing the new intensity values for the color channel.
     */
    private int[] computeNewIntensityValues(double[] cdf) {
        int[] newValues = new int[cdf.length];
        for (int i = 0; i < cdf.length; i++) {
            newValues[i] = (int) Math.round(cdf[i] * 255);
            newValues[i] = Math.min(newValues[i], 255);
            newValues[i] = Math.max(newValues[i], 0);
        }
        return newValues;
    }


    /**
     * Computes the Cumulative Distribution Function (CDF) for a given normalized histogram.
     *
     * @param normalizedHistogram The normalized histogram to compute the CDF for.
     * @return A double array representing the CDF.
     */
    private double[] computeCDF(double[] normalizedHistogram) {
        double[] cdf = new double[normalizedHistogram.length];
        cdf[0] = normalizedHistogram[0];
        for (int i = 1; i < normalizedHistogram.length; i++) {
            cdf[i] = cdf[i - 1] + normalizedHistogram[i];
        }
        return cdf;
    }
}
