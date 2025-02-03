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

/**
 * The MedianBlur class implements the BlurFilter interface to apply a median blur effect to an image.
 * This class uses a median filter to reduce noise in the image by replacing each pixel's value with the median
 * value of its neighboring pixels within a specified kernel size. The median filter is particularly effective
 * in preserving edges while reducing noise.
 */
public class MedianBlur implements BlurFilter {
    private final ExecutorService executorService;
    private final Lock lock;

    /**
     * Constructs a MedianBlur object with the specified ExecutorService.
     *
     * @param executorService The ExecutorService to manage the concurrent execution of filter tasks.
     */
    public MedianBlur(ExecutorService executorService) {
        this.executorService = executorService;
        this.lock = new ReentrantLock();
    }

    /**
     * Applies a median blur filter to the given image.
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
        BufferedImage outputImage = ImageHelper.copyImage(image, BufferedImage.TYPE_INT_RGB);
        try {
            List<Callable<Void>> tasks = createFilterTasks(image, outputImage, kernelSize, listener);
            listener.onProgressStart(tasks.size());
            executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return outputImage;
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
     * Creates a list of filter tasks to be executed concurrently.
     *
     * @param image The original image to which the blur filter will be applied.
     * @param outputImage The image to store the blurred result.
     * @param size The size of the blur kernel.
     * @param listener A ProgressListener to receive progress updates during the blur
     *                 operation. This can be null if progress reporting is not required.
     * @return A list of Callable tasks to be executed concurrently.
     */
    private List<Callable<Void>> createFilterTasks(BufferedImage image, BufferedImage outputImage, int size, ProgressListener listener) {
        List<Callable<Void>> tasks = new ArrayList<>();
        int width = image.getWidth();
        int offset = size / 2;

        for (int x = offset; x < width - offset; x++) {
            tasks.add(createFilterTask(image, outputImage, size, offset, x, listener));
        }

        return tasks;
    }

    /**
     * Creates a filter task to be executed concurrently.
     *
     * @param image The original image to which the blur filter will be applied.
     * @param outputImage The image to store the blurred result.
     * @param size The size of the blur kernel.
     * @param offset The offset from the edge of the image to the start of the kernel.
     * @param x The current column index.
     * @param listener A ProgressListener to receive progress updates during the blur
     *                 operation. This can be null if progress reporting is not required.
     * @return A Callable task to be executed concurrently.
     */
    private Callable<Void> createFilterTask(BufferedImage image, BufferedImage outputImage, int size, int offset, int x, ProgressListener listener) {
        return () -> {
            int height = image.getHeight();
            for (int y = offset; y < height - offset; y++) {
                int[] window = extractPixelWindow(image, size, offset, x, y);
                int medianPixel = calculateMedian(window);

                lock.lock();
                try {
                    outputImage.setRGB(x, y, medianPixel);
                } finally {
                    lock.unlock();
                }
            }
            listener.onProgressUpdate();
            return null;
        };
    }

    /**
     * Extracts a window of pixels around the specified coordinates.
     *
     * @param image The original image to which the blur filter will be applied.
     * @param size The size of the blur kernel.
     * @param offset The offset from the edge of the image to the start of the kernel.
     * @param x The current column index.
     * @param y The current row index.
     * @return An array of pixel values within the window.
     */
    private int[] extractPixelWindow(BufferedImage image, int size, int offset, int x, int y) {
        int[] window = new int[size * size];
        int index = 0;

        for (int dx = 0; dx < size; dx++) {
            for (int dy = 0; dy < size; dy++) {
                int nx = x + dx - offset;
                int ny = y + dy - offset;
                window[index++] = image.getRGB(nx, ny);
            }
        }

        return window;
    }

    /**
     * Calculates the median value of the given array of pixel values.
     *
     * @param window An array of pixel values.
     * @return The median pixel value.
     */
    private int calculateMedian(int[] window) {
        Arrays.sort(window);
        return window[window.length / 2];
    }

    /**
     * Returns a string representation of the Median blur filter.
     *
     * @return The string "Median blur".
     */
    @Override
    public String toString() {
        return "Median blur";
    }
}
