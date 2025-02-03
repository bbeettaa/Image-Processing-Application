package org.knu.ui.tools;

import org.knu.bll.ProgressListener;
import org.knu.bll.algorithms.CannyFilter;
import org.knu.bll.algorithms.blur.BlurFilter;
import org.knu.bll.algorithms.blur.GaussBlur;
import org.knu.bll.algorithms.blur.NoneBlur;
import org.knu.bll.algorithms.edges.EdgeDetectionOperator;
import org.knu.ui.swing.WorkingPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class CannyFilterTool implements Tool {
    private final ActionListener actionListener;
    private final JSpinner lowThreshold;
    private final JSpinner highThreshold;
    private final WorkingPanel workingPanel;
    private final CannyFilter cannyFilter;
    private final JSpinner kernel;
    private final JSpinner sigma;

    private final JComboBox<BlurFilter> blurFiltersComboBox;
    private final JComboBox<EdgeDetectionOperator> edgeDetectionComboBox;

    public CannyFilterTool(CannyFilter filter, BlurFilter[] blurFilters, EdgeDetectionOperator[] edgeDetect, WorkingPanel workingPanel) {
        this.cannyFilter = filter;
        this.workingPanel = workingPanel;

        blurFiltersComboBox = new JComboBox<>(blurFilters);
        edgeDetectionComboBox = new JComboBox<>(edgeDetect);
        this.lowThreshold = new JSpinner(new SpinnerNumberModel(50, 0, 255, 1));
        this.highThreshold = new JSpinner(new SpinnerNumberModel(80, 0, 255, 1));
        this.kernel = new JSpinner(new SpinnerNumberModel(5, 1, 55, 2));
        this.sigma = new JSpinner(new SpinnerNumberModel(1, 0, 150.0, 0.1));
        this.actionListener = e -> applyCanny();
    }

    @Override
    public String getName() {
        return "Canny Edge Detector";
    }

    @Override
    public String getDescription() {
        return "Edge detection";
    }

    @Override
    public Icon getIcon() {
        try (InputStream inputStream = getClass().getResourceAsStream("/icons/cube.png")) {
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
        blurMethodPanel.add(new JLabel("Blur Method: "));
        blurMethodPanel.add(blurFiltersComboBox);
        panel.add(blurMethodPanel);

        JPanel edgeDetectMethodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        edgeDetectMethodPanel.add(new JLabel("Edge detection Method: "));
        edgeDetectMethodPanel.add(edgeDetectionComboBox);
        panel.add(edgeDetectMethodPanel);
        panel.add(Box.createVerticalStrut(20));

        JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        textPanel.add(new JLabel("Low threshold (0 - 255): "));
        lowThreshold.setPreferredSize(new Dimension(60, 30));
        textPanel.add(lowThreshold);
        panel.add(textPanel);

        JPanel textPanelHighThreshold = new JPanel(new FlowLayout(FlowLayout.LEFT));
        textPanelHighThreshold.add(new JLabel("High threshold (0 - 255): "));
        highThreshold.setPreferredSize(new Dimension(60, 30));
        textPanelHighThreshold.add(highThreshold);
        panel.add(textPanelHighThreshold);
        panel.add(Box.createVerticalStrut(20));

        JPanel kernelText = new JPanel(new FlowLayout(FlowLayout.LEFT));
        kernelText.add(new JLabel("Blur kernel size: "));
        kernel.setPreferredSize(new Dimension(60, 30));
        kernelText.add(kernel);
        panel.add(kernelText);

        JPanel sigmaText = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sigmaText.add(new JLabel("Blur sigma: "));
        sigma.setPreferredSize(new Dimension(60, 30));
        sigmaText.add(sigma);
        panel.add(sigmaText);
        panel.add(Box.createVerticalStrut(15));

        blurFiltersComboBox.addActionListener(e -> {
            if (blurFiltersComboBox.getSelectedItem() instanceof GaussBlur) {
                sigma.setVisible(true);
                sigmaText.setVisible(true);
                kernel.setVisible(true);
                kernelText.setVisible(true);
            }else if(blurFiltersComboBox.getSelectedItem() instanceof NoneBlur) {
                kernel.setVisible(false);
                kernelText.setVisible(false);

                sigma.setVisible(false);
                sigmaText.setVisible(false);
            } else {
                kernel.setVisible(true);
                kernelText.setVisible(true);
                sigma.setVisible(false);
                sigmaText.setVisible(false);
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

    private void applyCanny() {
        workingPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<BufferedImage, Void> worker = createSwingWorker();
        worker.execute();
    }

    private SwingWorker<BufferedImage, Void> createSwingWorker() {
        return new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                try {
                    int kernelSize = (int) kernel.getValue();
                    double sigmaValue = (double) sigma.getValue();
                    int lth = (int) lowThreshold.getValue();
                    int hth = (int) highThreshold.getValue();
                    BlurFilter blur = (BlurFilter) blurFiltersComboBox.getSelectedItem();
                    EdgeDetectionOperator edge = (EdgeDetectionOperator) edgeDetectionComboBox.getSelectedItem();

                    if (blur instanceof GaussBlur)
                        ((GaussBlur) blur).setSigma(sigmaValue);

                    BufferedImage inputImage = workingPanel.getCurrentImagePanel().getBufferedImage();

                    return cannyFilter.applyFilter(inputImage, lth, hth, blur, kernelSize,edge , new ProgressListener() {
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
