package org.knu.ui.tools;

import org.knu.bll.ProgressListener;
import org.knu.bll.algorithms.edges.EdgeDetectionOperator;
import org.knu.ui.swing.WorkingPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class EdgeDetectorOperatorTool implements Tool {
    private final WorkingPanel workingPanel;
    private final JComboBox<EdgeDetectionOperator> edgeDetectionOperatorJComboBox;

    public EdgeDetectorOperatorTool(EdgeDetectionOperator[] edgeDetectionOperators, WorkingPanel workingPanel) {
        this.workingPanel = workingPanel;
        edgeDetectionOperatorJComboBox = new JComboBox<>(edgeDetectionOperators);
    }


    @Override
    public String getName() {
        return "Edge detection tool";
    }

    @Override
    public String getDescription() {
        return "Identifying edges defined as curves in image";
    }

    @Override
    public Icon getIcon() {
        try (InputStream inputStream = getClass().getResourceAsStream("/icons/cube_sobel.png")) {
            Image image = ImageIO.read(inputStream);
            return new ImageIcon(image);
        } catch (IOException ex) {
            throw new RuntimeException("Error loading icon", ex);
        }
    }

    @Override
    public JPanel createSettingsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel descriptionLabel = new JLabel(getDescription());
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(descriptionLabel);
        panel.add(Box.createVerticalStrut(15));

        JButton applyButton = createApplyButton();
        panel.add(applyButton);
        panel.add(Box.createVerticalStrut(40));

        JPanel blurMethodPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        blurMethodPanel.add(new JLabel("Blur Method: "));
        blurMethodPanel.add(edgeDetectionOperatorJComboBox);
        blurMethodPanel.add(Box.createVerticalStrut(20));
        panel.add(blurMethodPanel);

        return panel;
    }

    private JButton createApplyButton() {
        JButton applyButton = new JButton("Apply");
        applyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        applyButton.setFocusPainted(false);
        applyButton.setPreferredSize(new Dimension(150, 35));
        applyButton.addActionListener(createListener());
        return applyButton;
    }

    private ActionListener createListener() {
        return e -> {
            workingPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            SwingWorker<BufferedImage, Void> worker = createSwingWorker();
            worker.execute();
        };
    }

    private SwingWorker<BufferedImage, Void> createSwingWorker() {
        return new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                BufferedImage inputImage = workingPanel.getCurrentImagePanel().getBufferedImage();
                EdgeDetectionOperator detector = (EdgeDetectionOperator) edgeDetectionOperatorJComboBox.getSelectedItem();
                return detector.applyOperator(inputImage, new ProgressListener() {
                    @Override
                    public void onProgressStart(int maximum) {
                        workingPanel.showProgressBar();
                        workingPanel.getProgressBar().setMaximum(maximum);
                        workingPanel.getProgressBar().setValue(0);
                    }

                    @Override
                    public void onProgressUpdate() {
                        workingPanel.getProgressBar().setValue(
                                workingPanel.getProgressBar().getValue() + 1
                        );
                    }
                });
            }

            @Override
            protected void done() {
                try {
                    BufferedImage outputImage = get();
                    if (outputImage != null) {
                        workingPanel.saveState();
                        workingPanel.getCurrentImagePanel().setBufferedImage(outputImage);
                        workingPanel.getCurrentImagePanel().updateImage(new ImageIcon(outputImage));
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(workingPanel,
                            "Error applying: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    workingPanel.setCursor(Cursor.getDefaultCursor());
                    workingPanel.hideProgressBar();
                }
            }
        };
    }

    @Override
    public void activate() {

    }

    @Override
    public void deactivate() {

    }

    @Override
    public void onImageFocusChanged() {

    }


}
