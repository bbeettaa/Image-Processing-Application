package org.knu.bll.memento;

import java.awt.image.BufferedImage;

/**
 * The ImageMemento class represents a memento object that stores the state of an image.
 */
public class ImageMemento {
    private final BufferedImage state;

    /**
     * Constructs a new ImageMemento object with the given image state.
     *
     * @param state The state of the image to be stored.
     */
    public ImageMemento(BufferedImage state) {
        this.state = state;
    }

    /**
     * Returns the state of the image stored in this memento.
     *
     * @return The state of the image.
     */
    public BufferedImage getState() {
        return state;
    }
}
