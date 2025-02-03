package org.knu.bll.algorithms.clustering;

import org.knu.bll.ProgressListener;

import java.awt.image.BufferedImage;

/**
 * The Cluster interface defines a contract for applying clustering algorithms to an image.
 * Implementations of this interface should provide the logic for clustering pixels in an image
 * and optionally reporting progress through a listener.
 *
 * @author Your Name
 * @version 1.0
 * @since 2023-10-01
 */
public interface Cluster {

    /**
     * Applies a clustering algorithm to the given image.
     *
     * @param image The original image to which the clustering algorithm will be applied.
     *              This image should not be null.
     * @param listener A ProgressListener to receive progress updates during the clustering
     *                 operation. This listener should not be null.
     * @return A new BufferedImage that represents the clustered version of the original image.
     *         If the input image is null, the method should throw a NullPointerException.
     * @throws NullPointerException if the image is null.
     * @throws NullPointerException if the listener is null.
     */
    BufferedImage applyCluster(BufferedImage image, ProgressListener listener);
}
