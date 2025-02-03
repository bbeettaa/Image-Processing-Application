package org.knu.bll;

/**
 * The ProgressListener interface defines a contract for receiving progress updates during a long-running operation.
 */
public interface ProgressListener {

    /**
     * Called when a progress operation starts.
     *
     * @param maximum The maximum value of the progress.
     */
    void onProgressStart(int maximum);

    /**
     * Called when the progress is updated.
     */
    void onProgressUpdate();
}
