package org.knu.ui.swing;

import org.knu.bll.memento.ImageCaretaker;
import org.knu.bll.memento.ImageMemento;
import org.knu.bll.memento.ImageOriginator;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;


public class SingleImagePanel extends JPanel {
    private final JLabel imageLabel;
    private final JScrollPane imageScrollPane;
    private double currentScale = 1.0;
    private BufferedImage bufferedImage;
    private JFrame detachedFrame;

    private final ImageOriginator originator;
    private final ImageCaretaker caretaker;
    private final String absolutePath;

    public SingleImagePanel(WorkingPanel workingPanel, BufferedImage bufferedImage, String name, String absolutePath) {
        originator = new ImageOriginator();
        caretaker = new ImageCaretaker();

        setLayout(new BorderLayout());
        this.bufferedImage = bufferedImage;
        this.setName(name);
        this.absolutePath = absolutePath;

        imageLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon icon = (ImageIcon) getIcon();
                if (icon != null) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setColor(getBackground());
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    if (currentScale < 0.05) {
                        currentScale = 0.05;
                    }
                    g2d.scale(currentScale, currentScale);
                    g2d.drawImage(icon.getImage(), 0, 0, this);
                    g2d.dispose();
                }
            }
        };

        ImageIcon image = new ImageIcon(bufferedImage);
        imageLabel.setIcon(image);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        imageLabel.setPreferredSize(new Dimension(image.getIconWidth(), image.getIconHeight()));

        imageScrollPane = new JScrollPane(imageLabel);
        imageScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        imageScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(imageScrollPane, BorderLayout.CENTER);

    }

    public void zoomImage(double scale, Point cursorPosition) {
        ImageIcon imageIcon = (ImageIcon) imageLabel.getIcon();
        if (imageIcon != null) {
            int imageWidth = (int) (imageIcon.getIconWidth() * currentScale);
            int imageHeight = (int) (imageIcon.getIconHeight() * currentScale);

            JViewport viewport = imageScrollPane.getViewport();
            Point viewPosition = viewport.getViewPosition();
            int cursorX = cursorPosition.x - viewport.getX();
            int cursorY = cursorPosition.y - viewport.getY();

            double relativeCursorX = (cursorX + viewPosition.getX()) / imageWidth;
            double relativeCursorY = (cursorY + viewPosition.getY()) / imageHeight;

            currentScale *= scale;

            int newImageWidth = (int) (imageIcon.getIconWidth() * currentScale);
            int newImageHeight = (int) (imageIcon.getIconHeight() * currentScale);

            imageLabel.setPreferredSize(new Dimension(newImageWidth, newImageHeight));
            imageLabel.revalidate();

            double newViewX = (relativeCursorX * newImageWidth) - cursorX;
            double newViewY = (relativeCursorY * newImageHeight) - cursorY;

            newViewX = Math.max(0, Math.min(newViewX, newImageWidth - viewport.getWidth()));
            newViewY = Math.max(0, Math.min(newViewY, newImageHeight - viewport.getHeight()));

            viewport.setViewPosition(new Point((int) newViewX, (int) newViewY));
            imageLabel.repaint();
        }
    }

    public void updateImage(ImageIcon image) {
        imageLabel.setIcon(image);
        imageLabel.setText("");
        imageLabel.revalidate();
        imageLabel.repaint();
    }

    public JLabel getImageLabel() {
        return imageLabel;
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    public void setBufferedImage(BufferedImage image) {
        this.bufferedImage = image;
    }

    public JFrame getDetachedFrame() {
        return detachedFrame;
    }

    public void setDetachedFrame(JFrame detachedFrame) {
        this.detachedFrame = detachedFrame;
    }

    public void setCurrentScale(double currentScale) {
        this.currentScale = currentScale;
        this.repaint();
    }


    public void saveState() {
        BufferedImage currentImage = getBufferedImage();
        if (currentImage != null) {
            originator.setState(currentImage);
            caretaker.saveState(originator.save());
        }
    }

    public void undo() {
        if (caretaker.canUndo()) {
            originator.setState(getBufferedImage());
            caretaker.saveToRedo(originator.save());
            ImageMemento memento = caretaker.undo();
            if (memento != null) {
                originator.restore(memento);
                updateImage(originator.getState());
            }
        }
    }

    public void redo() {
        if (caretaker.canRedo()) {
            originator.setState(getBufferedImage());
            caretaker.saveToUndo(originator.save());
            ImageMemento memento = caretaker.redo();
            if (memento != null) {
                originator.restore(memento);
                updateImage(originator.getState());
            }
        }
    }


    private void updateImage(BufferedImage image) {
        setBufferedImage(image);
        updateImage(new ImageIcon(image));
        repaint();
    }

    public String getAbsolutePath() {
        return absolutePath;
    }
}
