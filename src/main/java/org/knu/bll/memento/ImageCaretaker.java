package org.knu.bll.memento;

import java.util.Stack;

/**
 * The ImageCaretaker class manages the undo and redo operations for image states using the Memento pattern.
 */
public class ImageCaretaker {
    private final Stack<ImageMemento> undoStack = new Stack<>();
    private final Stack<ImageMemento> redoStack = new Stack<>();

    /**
     * Saves the current state of the image for undo operations.
     *
     * @param memento The memento object representing the current state of the image.
     */
    public void saveState(ImageMemento memento) {
        undoStack.push(memento);
        redoStack.clear();
    }

    /**
     * Saves the current state of the image for redo operations.
     *
     * @param memento The memento object representing the current state of the image.
     */
    public void saveToRedo(ImageMemento memento) {
        redoStack.push(memento);
    }

    /**
     * Saves the current state of the image for undo operations.
     *
     * @param memento The memento object representing the current state of the image.
     */
    public void saveToUndo(ImageMemento memento) {
        undoStack.push(memento);
    }

    /**
     * Performs an undo operation by restoring the previous state of the image.
     *
     * @return The memento object representing the previous state of the image, or null if the undo stack is empty.
     */
    public ImageMemento undo() {
        if (!undoStack.isEmpty()) {
            ImageMemento memento = undoStack.pop();
            return memento;
        }
        return null;
    }

    /**
     * Performs a redo operation by restoring the next state of the image.
     *
     * @return The memento object representing the next state of the image, or null if the redo stack is empty.
     */
    public ImageMemento redo() {
        if (!redoStack.isEmpty()) {
            ImageMemento memento = redoStack.pop();
            return memento;
        }
        return null;
    }

    /**
     * Checks if an undo operation can be performed.
     *
     * @return True if an undo operation can be performed, false otherwise.
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Checks if a redo operation can be performed.
     *
     * @return True if a redo operation can be performed, false otherwise.
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}
