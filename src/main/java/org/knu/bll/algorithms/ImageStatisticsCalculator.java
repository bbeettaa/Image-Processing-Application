package org.knu.bll.algorithms;

import org.knu.bll.helpers.ImageHelper;

import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * The ImageStatisticsCalculator class calculates various statistical measures for an image, including mean intensity,
 * variance, standard deviation, entropy, energy, and contrast.
 */
public class ImageStatisticsCalculator {

    /**
     * Calculates the statistics for the given image.
     *
     * @param image The image to calculate statistics for. This image should not be null.
     * @return An ImageStatistics object containing the calculated statistics.
     * @throws NullPointerException if the image is null.
     */
    public ImageStatistics calculateStatistics(BufferedImage image) {
        if (image == null) {
            throw new NullPointerException("Image cannot be null");
        }

        int width = image.getWidth();
        int height = image.getHeight();
        int totalPixels = width * height;

        ImageStatistics imageStatistics = new ImageStatistics();

        imageStatistics.setImageWidth(width);
        imageStatistics.setImageHeight(height);

        int[] intensity = new int[totalPixels];
        image.getRGB(0, 0, width, height, intensity, 0, width);
        intensity = Arrays.stream(intensity).map(ImageHelper::getGradient).toArray();

        double mean = calculateMean(intensity);
        imageStatistics.setMeanIntensity(mean);

        double variance = calculateVariance(intensity, mean);
        imageStatistics.setVariance(variance);

        double stdDev = Math.sqrt(variance);
        imageStatistics.setStdDeviation(stdDev);

        double entropy = calculateEntropy(intensity);
        imageStatistics.setEntropy(entropy);

        double energy = calculateEnergy(intensity);
        imageStatistics.setEnergy(energy);

        int contrast = calculateContrast(intensity);
        imageStatistics.setContrast(contrast);

        return imageStatistics;
    }

    /**
     * Calculates the mean intensity of the given values.
     *
     * @param values The array of intensity values.
     * @return The mean intensity.
     */
    private static double calculateMean(int[] values) {
        return (double) Arrays.stream(values).sum() / values.length;
    }

    /**
     * Calculates the variance of the given values.
     *
     * @param values The array of intensity values.
     * @param mean The mean intensity.
     * @return The variance.
     */
    private static double calculateVariance(int[] values, double mean) {
        return Arrays.stream(values).mapToDouble(e -> Math.pow(e - mean, 2)).sum() / values.length;
    }

    /**
     * Calculates the entropy of the given values.
     *
     * @param values The array of intensity values.
     * @return The entropy.
     */
    private static double calculateEntropy(int[] values) {
        int[] histogram = new int[256];
        for (int v : values)
            histogram[v]++;

        return Arrays
                .stream(histogram)
                .filter(e -> e > 0)
                .mapToDouble(e -> ((double) e / values.length) * (Math.log((double) e / values.length) / Math.log(2)))
                .sum() * -1;
    }

    /**
     * Calculates the energy of the given values.
     *
     * @param values The array of intensity values.
     * @return The energy.
     */
    private static double calculateEnergy(int[] values) {
        return Arrays.stream(values).mapToDouble(e -> Math.pow(e / 255.0, 2)).sum() / values.length;
    }

    /**
     * Calculates the contrast of the given values.
     *
     * @param values The array of intensity values.
     * @return The contrast.
     */
    private static int calculateContrast(int[] values) {
        return Arrays.stream(values).max().orElse(0) - Arrays.stream(values).min().orElse(250);
    }

    /**
     * The ImageStatistics class represents the statistical measures of an image.
     */
    static public class ImageStatistics {
        private int imageWidth;
        private int imageHeight;
        private double meanIntensity;
        private double variance;
        private double stdDeviation;
        private double entropy;
        private double energy;
        private double contrast;

        public int getImageWidth() {
            return imageWidth;
        }

        public void setImageWidth(int imageWidth) {
            this.imageWidth = imageWidth;
        }

        public int getImageHeight() {
            return imageHeight;
        }

        public void setImageHeight(int imageHeight) {
            this.imageHeight = imageHeight;
        }

        public double getMeanIntensity() {
            return meanIntensity;
        }

        public void setMeanIntensity(double meanIntensity) {
            this.meanIntensity = meanIntensity;
        }

        public double getVariance() {
            return variance;
        }

        public void setVariance(double variance) {
            this.variance = variance;
        }

        public double getStdDeviation() {
            return stdDeviation;
        }

        public void setStdDeviation(double stdDeviation) {
            this.stdDeviation = stdDeviation;
        }

        public double getEntropy() {
            return entropy;
        }

        public void setEntropy(double entropy) {
            this.entropy = entropy;
        }

        public double getEnergy() {
            return energy;
        }

        public void setEnergy(double energy) {
            this.energy = energy;
        }

        public double getContrast() {
            return contrast;
        }

        public void setContrast(double contrast) {
            this.contrast = contrast;
        }
    }
}
