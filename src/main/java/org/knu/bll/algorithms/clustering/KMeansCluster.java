package org.knu.bll.algorithms.clustering;

import org.knu.bll.ProgressListener;
import org.knu.bll.helpers.ImageHelper;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The KMeansCluster class implements the Cluster interface to apply the K-Means clustering algorithm to an image.
 * This class segments an image into K clusters based on the color similarity of pixels. The K-Means algorithm
 * iteratively assigns pixels to the nearest centroid and updates the centroids until convergence.
 */
public class KMeansCluster implements Cluster {
    private int k;
    private List<int[]> centroids;
    private int[][] clusters;
    private final Random random = new Random();

    /**
     * Applies the K-Means clustering algorithm to the given image.
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
    @Override
    public BufferedImage applyCluster(BufferedImage image, ProgressListener listener) {
        if (image == null) {
            throw new NullPointerException("Image cannot be null");
        }
        if (listener == null) {
            throw new NullPointerException("Listener cannot be null");
        }

        int width = image.getWidth();
        int height = image.getHeight();
        clusters = new int[height][width];
        centroids = new ArrayList<>();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        listener.onProgressStart(3);

        initializeCentroids(image, k);
        listener.onProgressUpdate();

        checkAndUpdateClusters(image);
        listener.onProgressUpdate();

        segmentImage(height, width, bufferedImage);
        listener.onProgressUpdate();

        return bufferedImage;
    }

    /**
     * Initializes the centroids by randomly selecting K pixels from the image.
     *
     * @param image The original image from which to select initial centroids.
     * @param k The number of clusters.
     */
    private void initializeCentroids(BufferedImage image, int k) {
        for (int i = 0; i < k; i++) {
            int x = random.nextInt(image.getWidth());
            int y = random.nextInt(image.getHeight());
            int rgb = image.getRGB(x, y);

            int[] centroid = {ImageHelper.getRedByte(rgb), ImageHelper.getGreenByte(rgb), ImageHelper.getBlueByte(rgb)};
            centroids.add(centroid);
        }
    }

    /**
     * Iteratively assigns pixels to the nearest centroid and updates the centroids until convergence.
     *
     * @param image The original image to be clustered.
     */
    private void checkAndUpdateClusters(BufferedImage image) {
        boolean converged = false;
        while (!converged) {
            clusters = assignClusters(image, centroids);
            List<int[]> newCentroids = updateCentroids(image, clusters, k);
            converged = checkConvergence(centroids, newCentroids);
            centroids = newCentroids;
        }
    }

    /**
     * Assigns each pixel to the nearest centroid.
     *
     * @param image The original image to be clustered.
     * @param centroids The list of current centroids.
     * @return A 2D array representing the cluster assignment for each pixel.
     */
    private int[][] assignClusters(BufferedImage image, List<int[]> centroids) {
        int[][] clusters = new int[image.getHeight()][image.getWidth()];

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);

                int closestCentroidIndex = -1;
                double minDistance = Double.MAX_VALUE;

                for (int i = 0; i < centroids.size(); i++) {
                    int[] centroid = centroids.get(i);
                    double distance = euclideanDistance(
                            ImageHelper.getRedByte(rgb),
                            ImageHelper.getGreenByte(rgb),
                            ImageHelper.getBlueByte(rgb),
                            centroid[0], centroid[1], centroid[2]);

                    if (distance < minDistance) {
                        minDistance = distance;
                        closestCentroidIndex = i;
                    }
                }

                clusters[y][x] = closestCentroidIndex;
            }
        }

        return clusters;
    }

    /**
     * Updates the centroids based on the current cluster assignments.
     *
     * @param image The original image to be clustered.
     * @param clusters The 2D array representing the cluster assignment for each pixel.
     * @param k The number of clusters.
     * @return A list of new centroids.
     */
    private List<int[]> updateCentroids(BufferedImage image, int[][] clusters, int k) {
        List<int[]> newCentroids = new ArrayList<>();
        int[][] sums = new int[k][3];
        int[] counts = new int[k];

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int cluster = clusters[y][x];
                int rgb = image.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                sums[cluster][0] += red;
                sums[cluster][1] += green;
                sums[cluster][2] += blue;
                counts[cluster]++;
            }
        }

        for (int i = 0; i < k; i++) {
            int[] centroid = new int[3];
            if (counts[i] > 0) {
                centroid[0] = sums[i][0] / counts[i];
                centroid[1] = sums[i][1] / counts[i];
                centroid[2] = sums[i][2] / counts[i];
            }
            newCentroids.add(centroid);
        }

        return newCentroids;
    }

    /**
     * Segments the image by assigning each pixel the color of its corresponding centroid.
     *
     * @param height The height of the image.
     * @param width The width of the image.
     * @param segmentedImage The image to store the segmented result.
     */
    private void segmentImage(int height, int width, BufferedImage segmentedImage) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int cluster = clusters[y][x];
                int[] centroid = centroids.get(cluster);
                int rgb = (centroid[0] << 16) | (centroid[1] << 8) | centroid[2];
                segmentedImage.setRGB(x, y, rgb);
            }
        }
    }

    /**
     * Checks if the centroids have converged.
     *
     * @param oldCentroids The list of old centroids.
     * @param newCentroids The list of new centroids.
     * @return True if the centroids have converged, false otherwise.
     */
    private boolean checkConvergence(List<int[]> oldCentroids, List<int[]> newCentroids) {
        for (int i = 0; i < oldCentroids.size(); i++) {
            if (!arraysEqual(oldCentroids.get(i), newCentroids.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if two arrays are equal.
     *
     * @param arr1 The first array.
     * @param arr2 The second array.
     * @return True if the arrays are equal, false otherwise.
     */
    private boolean arraysEqual(int[] arr1, int[] arr2) {
        if (arr1.length != arr2.length) {
            return false;
        }
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] != arr2[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculates the Euclidean distance between two points in 3D space.
     *
     * @param r1 The red component of the first point.
     * @param g1 The green component of the first point.
     * @param b1 The blue component of the first point.
     * @param r2 The red component of the second point.
     * @param g2 The green component of the second point.
     * @param b2 The blue component of the second point.
     * @return The Euclidean distance between the two points.
     */
    private double euclideanDistance(int r1, int g1, int b1, int r2, int g2, int b2) {
        return Math.sqrt(Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2) + Math.pow(b1 - b2, 2));
    }

    /**
     * Returns a string representation of the K-Means clustering algorithm.
     *
     * @return The string "K-Means".
     */
    @Override
    public String toString() {
        return "K-Means";
    }

    /**
     * Sets the number of clusters (K).
     *
     * @param k The number of clusters.
     */
    public void setK(int k) {
        this.k = k;
    }
}
