package org.knu.bll.algorithms.histograms;

import org.knu.bll.ProgressListener;
import org.knu.bll.helpers.ImageHelper;

import java.awt.image.BufferedImage;

/**
 * The CLAHEProcessor class implements the Contrast Limited Adaptive Histogram Equalization (CLAHE) algorithm
 * to enhance the contrast of an image. This class divides the image into tiles, applies histogram equalization
 * to each tile, and then uses bilinear interpolation to combine the results.
 */
public class CLAHEProcessor {

    /**
     * Applies the CLAHE algorithm to the given image.
     *
     * @param original  The original image to which the CLAHE algorithm will be applied.
     *                  This image should not be null.
     * @param tileSize  The size of the tiles into which the image will be divided.
     * @param clipLimit The clip limit to apply to the histograms.
     * @param cdfBlur   The blur power to apply to the CDFs.
     * @param l         A ProgressListener to receive progress updates during the CLAHE process.
     *                  This listener should not be null.
     * @return A new BufferedImage that represents the contrast-enhanced version of the original image.
     * If the input image is null, the method should throw a NullPointerException.
     * @throws NullPointerException if the original image is null.
     * @throws NullPointerException if the listener is null.
     */
    public BufferedImage applyCLAHE(BufferedImage original, int tileSize, double clipLimit, int cdfBlur, ProgressListener l) {
        int width = original.getWidth();
        int height = original.getHeight();
        int numTilesX = (int) Math.ceil((double) width / tileSize);
        int numTilesY = (int) Math.ceil((double) height / tileSize);
        l.onProgressStart(4);

        int[][] histograms = computeHistograms(original, numTilesX, numTilesY, tileSize);
        l.onProgressUpdate();

        clipHistograms(histograms, clipLimit);
        l.onProgressUpdate();

        int[][] cdfs = computeCDFs(histograms, cdfBlur, tileSize);
        l.onProgressUpdate();

        BufferedImage result = new BufferedImage(width, height, original.getType());
        applyBilinearInterpolation(original, result, cdfs, tileSize, numTilesX, numTilesY);
        l.onProgressUpdate();

        return result;
    }

    /**
     * Computes the histograms for each tile in the image.
     *
     * @param image     The original image.
     * @param numTilesX The number of tiles in the x-direction.
     * @param numTilesY The number of tiles in the y-direction.
     * @param tileSize  The size of the tiles.
     * @return A 2D array representing the histograms for each tile.
     */
    private int[][] computeHistograms(BufferedImage image, int numTilesX, int numTilesY, int tileSize) {
        int[][] histograms = new int[numTilesX * numTilesY][256];
        for (int ty = 0; ty < numTilesY; ty++) {
            for (int tx = 0; tx < numTilesX; tx++) {
                int[] histogram = new int[256];
                for (int y = ty * tileSize; y < Math.min((ty + 1) * tileSize, image.getHeight()); y++) {
                    for (int x = tx * tileSize; x < Math.min((tx + 1) * tileSize, image.getWidth()); x++) {
                        int pixel = ImageHelper.getGrayByte(image.getRGB(x, y));
                        histogram[pixel]++;
                    }
                }
                histograms[ty * numTilesX + tx] = histogram;
            }
        }
        return histograms;
    }

    /**
     * Clips the histograms to the specified clip limit.
     *
     * @param histograms The histograms to be clipped.
     * @param clipLimit  The clip limit to apply to the histograms.
     */
    private void clipHistograms(int[][] histograms, double clipLimit) {
        int numHistograms = histograms.length;
        int bins = histograms[0].length;

        for (int h = 0; h < numHistograms; h++) {
            int excess = 0;

            // Determine excess values
            for (int i = 0; i < bins; i++) {
                if (histograms[h][i] > clipLimit) {
                    excess += histograms[h][i] - (int) clipLimit;
                    histograms[h][i] = (int) clipLimit;
                }
            }

            // Distribute excess values uniformly
            int increment = excess / bins;
            int remainder = excess % bins;

            for (int i = 0; i < bins; i++) {
                histograms[h][i] += increment;
            }

            // Distribute remaining excess values
            for (int i = 0; i < remainder; i++) {
                histograms[h][i] += 1;
            }
        }
    }

    /**
     * Computes the Cumulative Distribution Functions (CDFs) for the histograms.
     *
     * @param histograms The histograms for which to compute the CDFs.
     * @param blurPower  The blur power to apply to the CDFs.
     * @param size       The size of the tiles.
     * @return A 2D array representing the CDFs for each histogram.
     */
    private int[][] computeCDFs(int[][] histograms, int blurPower, int size) {
        int[][] cdfs = new int[histograms.length][256];
        for (int i = 0; i < histograms.length; i++) {
            int[] histogram = histograms[i];
            int[] cdf = new int[256];
            cdf[0] = histogram[0];
            for (int j = 1; j < 256; j++) {
                cdf[j] = cdf[j - 1] + histogram[j];
            }

            int cdfMax = cdf[255];
            for (int j = 1; j < 256; j++) {
                cdf[j] = (int) (cdf[j] * 255.0 / cdfMax);
            }

            cdfs[i] = movingAverage(cdf, blurPower);
        }
        return cdfs;
    }

    /**
     * Applies a moving average to the CDF to smooth it.
     *
     * @param cdf The CDF to be smoothed.
     * @param mod The blur power to apply to the CDF.
     * @return A smoothed CDF.
     */
    private int[] movingAverage(int[] cdf, int mod) {
        int[] smoothedCDF = new int[cdf.length];
        int halfWindow = mod / 2;

        for (int i = 0; i < cdf.length; i++) {
            int sum = 0, count = 0;
            for (int j = Math.max(0, i - halfWindow); j <= Math.min(cdf.length - 1, i + halfWindow); j++) {
                sum += cdf[j];
                count++;
            }
            smoothedCDF[i] = sum / count;
        }
        return smoothedCDF;
    }

    /**
     * Applies bilinear interpolation to combine the results of the CDFs.
     *
     * @param original  The original image.
     * @param result    The image to store the contrast-enhanced result.
     * @param cdfs      The CDFs for each tile.
     * @param tileSize  The size of the tiles.
     * @param numTilesX The number of tiles in the x-direction.
     * @param numTilesY The number of tiles in the y-direction.
     */
    private void applyBilinearInterpolation(BufferedImage original, BufferedImage result, int[][] cdfs, int tileSize, int numTilesX, int numTilesY) {
        for (int y = 0; y < result.getHeight(); y++) {
            for (int x = 0; x < result.getWidth(); x++) {
                int tx = Math.min(x / tileSize, numTilesX - 1);
                int ty = Math.min(y / tileSize, numTilesY - 1);
                int pixel = ImageHelper.getGrayByte(original.getRGB(x, y));
                int newPixel = bilinearInterpolate(cdfs, tx, ty, numTilesX, numTilesY, pixel, x, y, tileSize);
                result.setRGB(x, y, ImageHelper.byteToRGB(newPixel));
            }
        }
    }

    /**
     * Performs bilinear interpolation to determine the new pixel value.
     *
     * @param cdfs      The CDFs for each tile.
     * @param tx        The x-coordinate of the tile.
     * @param ty        The y-coordinate of the tile.
     * @param numTilesX The number of tiles in the x-direction.
     * @param numTilesY The number of tiles in the y-direction.
     * @param pixel     The original pixel value.
     * @param x         The x-coordinate of the pixel.
     * @param y         The y-coordinate of the pixel.
     * @param tileSize  The size of the tiles.
     * @return The new pixel value after bilinear interpolation.
     */
    private int bilinearInterpolate(int[][] cdfs, int tx, int ty, int numTilesX, int numTilesY, int pixel, int x, int y, int tileSize) {
        int cdfTL = cdfs[ty * numTilesX + tx][pixel];
        int cdfTR = (tx + 1 < numTilesX) ? cdfs[ty * numTilesX + (tx + 1)][pixel] : cdfTL;
        int cdfBL = (ty + 1 < numTilesY) ? cdfs[(ty + 1) * numTilesX + tx][pixel] : cdfTL;
        int cdfBR = (tx + 1 < numTilesX && ty + 1 < numTilesY) ? cdfs[(ty + 1) * numTilesX + (tx + 1)][pixel] : cdfTL;

        double dx = (x % tileSize) / (double) tileSize;
        double dy = (y % tileSize) / (double) tileSize;

        return (int) ((cdfTL * (1 - dx) * (1 - dy)) + (cdfTR * dx * (1 - dy)) + (cdfBL * (1 - dx) * dy) + (cdfBR * dx * dy));
    }
}
