package org.knu.bll.memento;

import java.awt.image.BufferedImage;

/**
 * The ImageOriginator class represents the originator in the Memento pattern, which can save and restore its state.
 */
public class ImageOriginator {
    private BufferedImage currentImage;

    /**
     * Sets the current state of the image.
     *
     * @param image The image to set as the current state.
     */
    public void setState(BufferedImage image) {
        this.currentImage = image;
    }

    /**
     * Returns the current state of the image.
     *
     * @return The current state of the image.
     */
    public BufferedImage getState() {
        return currentImage;
    }

    /**
     * Saves the current state of the image as a memento.
     *
     * @return A memento object representing the current state of the image.
     */
    public ImageMemento save() {
        return new ImageMemento(currentImage);
    }

    /**
     * Restores the state of the image from a memento.
     *
     * @param memento The memento object representing the state to restore.
     */
    public void restore(ImageMemento memento) {
        this.currentImage = memento.getState();
    }
}
