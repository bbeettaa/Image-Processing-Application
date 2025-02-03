package org.knu.ui.tools;

import org.knu.bll.ProgressListener;
import org.knu.bll.algorithms.histograms.HistogramEqualization;
import org.knu.ui.swing.WorkingPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class HistogramEqualizerTool implements Tool {
    private final WorkingPanel workingPanel;
    private final HistogramEqualization equalizer;

    private final JCheckBox redChannelCheckbox;
    private final JCheckBox greenChannelCheckbox;
    private final JCheckBox blueChanelCheckbox;
    private final JCheckBox grayChannelCheckbox;

    private final  JSpinner blockSize;
    private final  JSpinner clipLimit;
    private final  JSpinner cdfBlur;

    public HistogramEqualizerTool(HistogramEqualization equalizer,
                                  WorkingPanel workingPanel) {
        this.equalizer = equalizer;
        this.workingPanel = workingPanel;

         blockSize = new JSpinner(new SpinnerNumberModel(24, 0, 255, 1));
        clipLimit = new JSpinner(new SpinnerNumberModel(4, 0, 50, 1));
        cdfBlur = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));

        redChannelCheckbox = new JCheckBox("Red", true);
        greenChannelCheckbox = new JCheckBox("Green", true);
        blueChanelCheckbox = new JCheckBox("Blue", true);
        grayChannelCheckbox = new JCheckBox("Gray", true);
    }

    @Override
    public String getName() {
        return "Histogram Equalizer";
    }

    @Override
    public String getDescription() {
        return "Histogram Equalizer";
    }

    @Override
    public Icon getIcon() {
        try (InputStream inputStream = getClass().getResourceAsStream("/icons/histogram.png")) {
            Image image = ImageIO.read(inputStream);
            return new ImageIcon(image);
        } catch (IOException ex) {
            throw new RuntimeException("Error loading icon", ex);
        }
    }

    @Override
    public JComponent createSettingsPanel() {
        JComponent panel = createUI();
        return panel;
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

    public JComponent createUI() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

        JPanel checkBoxPanel = new JPanel(new GridBagLayout());
        checkBoxPanel.setBorder(BorderFactory.createTitledBorder("Histogram Equalization"));
        checkBoxPanel.setMaximumSize(new Dimension(300, 40));

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 0;
        gbc2.gridy = 0;
        gbc2.anchor = GridBagConstraints.WEST;
        gbc2.insets = new Insets(5, 5, 5, 5);

        addLabeledComponent(checkBoxPanel, "Red Channel:", redChannelCheckbox, gbc2);
        gbc2.gridy++;
        addLabeledComponent(checkBoxPanel, "Green Channel:", greenChannelCheckbox, gbc2);
        gbc2.gridy++;
        addLabeledComponent(checkBoxPanel, "Blue Channel:", blueChanelCheckbox, gbc2);

        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> applyHistogram());
        gbc2.gridy++;
        addLabeledComponent(checkBoxPanel, "Apply:", applyButton, gbc2);

        controlPanel.add(checkBoxPanel);

        controlPanel.add(Box.createVerticalStrut(30));

        JPanel CLAHEPanel = new JPanel(new GridBagLayout());
        CLAHEPanel.setBorder(BorderFactory.createTitledBorder("CLAHE"));
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.anchor = GridBagConstraints.WEST;
        gbc1.insets = new Insets(5, 5, 5, 5);

        JButton applyCLAHEButton = new JButton("Apply CLAHE");
        applyCLAHEButton.addActionListener(e -> applyCLAHEHistogram());

        addLabeledComponent(CLAHEPanel, "Apply:", applyCLAHEButton, gbc1);
        gbc1.gridy++;
        addLabeledComponent(CLAHEPanel, "Block Size:", blockSize, gbc1);
        gbc1.gridy++;
        addLabeledComponent(CLAHEPanel, "Clip Limit:", clipLimit, gbc1);
        gbc1.gridy++;
        addLabeledComponent(CLAHEPanel, "Cdf Blur:", cdfBlur, gbc1);

        controlPanel.add(CLAHEPanel);
        controlPanel.add(Box.createVerticalStrut(20));

        return controlPanel;
    }

    private void addLabeledComponent(JPanel panel, String labelText, JComponent component, GridBagConstraints gbc) {
        JLabel label = new JLabel(labelText);
        gbc.gridx = 0;
        panel.add(label, gbc);
        gbc.gridx = 1;
        panel.add(component, gbc);
    }

    private void applyHistogram() {
        workingPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<BufferedImage, Void> worker = createSwingWorker_applyHistogram();
        worker.execute();
    }

    private SwingWorker<BufferedImage, Void> createSwingWorker_applyHistogram() {
        return new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                try {
                    boolean[] channelsToEqualize = new boolean[]{
                            redChannelCheckbox.isSelected(),
                            greenChannelCheckbox.isSelected(),
                            blueChanelCheckbox.isSelected(),
                            grayChannelCheckbox.isSelected()
                    };

                    BufferedImage inputImage = workingPanel.getCurrentImagePanel().getBufferedImage();

                    return equalizer.histogramEqualization(inputImage, channelsToEqualize, new ProgressListener() {
                        @Override
                        public void onProgressStart(int maximum) {
                        }

                        @Override
                        public void onProgressUpdate() {
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

    private void applyCLAHEHistogram() {
        workingPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<BufferedImage, Void> worker = createSwingWorker_applyCLAHEHistogram();
        worker.execute();
    }

    private SwingWorker<BufferedImage, Void> createSwingWorker_applyCLAHEHistogram() {
        return new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                try {
                    int lt = (int) blockSize.getValue();
                    int ht = (int) clipLimit.getValue();
                    int cdfBlurValue = (int) cdfBlur.getValue();

                    BufferedImage inputImage = workingPanel.getCurrentImagePanel().getBufferedImage();
                    return equalizer.applyCLAHE(inputImage, lt, ht, cdfBlurValue, new ProgressListener() {
                        @Override
                        public void onProgressStart(int maximum) {
                        }

                        @Override
                        public void onProgressUpdate() {
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

}
