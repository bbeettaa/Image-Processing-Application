package org.knu.ui.tools;

import org.knu.bll.ProgressListener;
import org.knu.bll.algorithms.clustering.Cluster;
import org.knu.bll.algorithms.clustering.KMeansCluster;
import org.knu.ui.swing.WorkingPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class ClusteringTool implements Tool {
    private final ActionListener actionListener;
    private final JSpinner kMeansSpinner;
    private final WorkingPanel workingPanel;

    private final JComboBox<Cluster> clusterJComboBox;

    public ClusteringTool(Cluster[] blurFilters, WorkingPanel workingPanel) {
        this.workingPanel = workingPanel;
        clusterJComboBox = new JComboBox<>(blurFilters);

        this.kMeansSpinner = new JSpinner(new SpinnerNumberModel(3,1,55,1));
        this.actionListener = e -> applyBlur();
    }

    @Override
    public String getName() {
        return "Cluster tool";
    }

    @Override
    public String getDescription() {
        return "Apply cluster filter to image";
    }

    @Override
    public Icon getIcon() {
        try (InputStream inputStream = getClass().getResourceAsStream("/icons/cluster.png")) {
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

        JPanel blurMethodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        blurMethodPanel.add(new JLabel("Cluster Method: "));
        blurMethodPanel.add(clusterJComboBox);
        blurMethodPanel.add(Box.createVerticalStrut(20));
        panel.add(blurMethodPanel);

        JPanel kMeansTextPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        kMeansTextPanel.add(new JLabel("K value: "));
        kMeansSpinner.setPreferredSize(new Dimension(60, 30));
        kMeansTextPanel.add(kMeansSpinner);
        panel.add(kMeansTextPanel);
//
//        JPanel sigmaText = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        sigmaText.add(new JLabel("Sigma: "));
//        sigma.setPreferredSize(new Dimension(60, 30));
//        sigmaText.add(sigma);
//        panel.add(sigmaText);

        panel.add(Box.createVerticalStrut(15));

        clusterJComboBox.addActionListener(e -> {
            if (clusterJComboBox.getSelectedItem() instanceof KMeansCluster) {
                kMeansTextPanel.setVisible(true);
            } else {
                kMeansTextPanel.setVisible(false);
            }
        });

        return panel;
    }

    private JButton createApplyButton() {
        JButton applyButton = new JButton("Apply");
        applyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        applyButton.setFocusPainted(false);
        applyButton.setPreferredSize(new Dimension(150, 35));
        applyButton.addActionListener(actionListener);
        return applyButton;
    }

    private void applyBlur() {
        workingPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<BufferedImage, Void> worker = createSwingWorker();
        worker.execute();
    }

    private SwingWorker<BufferedImage, Void> createSwingWorker() {
        return new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                try {
                    BufferedImage inputImage = workingPanel.getCurrentImagePanel().getBufferedImage();

                    Cluster cluster = (Cluster) clusterJComboBox.getSelectedItem();
                    if (cluster instanceof KMeansCluster) {
                        int kValue = (int) kMeansSpinner.getValue();
                        ((KMeansCluster) cluster).setK(kValue);
                    }

                    return cluster.applyCluster(inputImage, new ProgressListener() {
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
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
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
                            "Error applying filter: " + ex.getMessage(),
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
        // No default action
    }

    @Override
    public void deactivate() {
        // Cleanup resources if necessary
    }

    @Override
    public void onImageFocusChanged() {

    }
}
