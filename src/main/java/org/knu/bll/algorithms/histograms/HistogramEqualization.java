package org.knu.bll.algorithms.histograms;

import org.knu.bll.ProgressListener;

import java.awt.image.BufferedImage;

/**
 * The HistogramEqualization class provides methods to apply histogram equalization and CLAHE to an image.
 * This class acts as a facade to the HistogramEqualizerProcessor and CLAHEProcessor classes.
 *
 * @author Your Name
 * @version 1.0
 * @since 2023-10-01
 */
public class HistogramEqualization {

    /**
     * Applies histogram equalization to the given image.
     *
     * @param original The original image to which histogram equalization will be applied.
     *                 This image should not be null.
     * @param channelsToEqualize A boolean array indicating which color channels to equalize.
     * @param listener A ProgressListener to receive progress updates during the histogram equalization
     *                process. This listener should not be null.
     * @return A new BufferedImage that represents the histogram-equalized version of the original image.
     *         If the input image is null, the method should throw a NullPointerException.
     * @throws NullPointerException if the original image is null.
     * @throws NullPointerException if the listener is null.
     */
    public BufferedImage histogramEqualization(BufferedImage original, boolean[] channelsToEqualize, ProgressListener listener) {
        if (original == null) throw new NullPointerException("Original image cannot be null");
        if (listener == null) throw new NullPointerException("Listener cannot be null");

        return new HistogramEqualizerProcessor().histogramEqualization(original, channelsToEqualize, listener);
    }

    /**
     * Applies the CLAHE algorithm to the given image.
     *
     * @param original The original image to which the CLAHE algorithm will be applied.
     *                 This image should not be null.
     * @param tileSize The size of the tiles into which the image will be divided.
     * @param clipLimit The clip limit to apply to the histograms.
     * @param cdfBlur The blur power to apply to the CDFs.
     * @param l A ProgressListener to receive progress updates during the CLAHE process.
     *          This listener should not be null.
     * @return A new BufferedImage that represents the contrast-enhanced version of the original image.
     *         If the input image is null, the method should throw a NullPointerException.
     * @throws NullPointerException if the original image is null.
     * @throws NullPointerException if the listener is null.
     */
    public BufferedImage applyCLAHE(BufferedImage original, int tileSize, double clipLimit, int cdfBlur, ProgressListener l) {
        if (original == null) throw new NullPointerException("Original image cannot be null");
        if (l == null) throw new NullPointerException("Listener cannot be null");

        return new CLAHEProcessor().applyCLAHE(original, tileSize, clipLimit, cdfBlur, l);
    }
}
