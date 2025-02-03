
package org.knu.ui.tools;

import org.knu.bll.algorithms.ImageStatisticsCalculator;
import org.knu.bll.helpers.ImageHelper;
import org.knu.ui.swing.WorkingPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

public class StatisticTool implements Tool {
    private boolean isActive = false;
    private boolean isFrame = false;
    private final WorkingPanel workingPanel;

    private JLabel imageLabel;
    private JSlider binSlider;

    private final JCheckBox redChannelCheckbox;
    private final JCheckBox greenChannelCheckbox;
    private final JCheckBox blueChanelCheckbox;
    private final JCheckBox grayChannelCheckbox;
    private final ImageStatisticsCalculator statisticsCalculator;

    private final Color[] histogramColors;

    private final JLabel imageSize = new JLabel();
    private final JLabel meanIntensity = new JLabel();
    private final JLabel variance = new JLabel();
    private final JLabel stdDeviation = new JLabel();
    private final JLabel entropy = new JLabel();
    private final JLabel energy = new JLabel();
    private final JLabel contrast = new JLabel();

    private  JFrame parentFrame;

    @Override
    public String getName() {
        return "Statistic";
    }

    @Override
    public String getDescription() {
        return "Statistic";
    }

    @Override
    public Icon getIcon() {
        try (InputStream inputStream = getClass().getResourceAsStream("/icons/statistic.png")) {
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
        isActive = true;
        updateInfo();
    }

    public void updateInfo() {
        updateHistogram();
        updateStatistic();
    }

    @Override
    public void deactivate() {
        isActive = false;
    }

    @Override
    public void onImageFocusChanged() {
        updateInfo();
    }

    public StatisticTool(ImageStatisticsCalculator statisticsCalculator, WorkingPanel workingPanel) {
        this.histogramColors = new Color[]{Color.RED, Color.GREEN, Color.BLUE, Color.GRAY};
        this.workingPanel = workingPanel;
        this.statisticsCalculator = statisticsCalculator;

        redChannelCheckbox = new JCheckBox("Red", true);
        greenChannelCheckbox = new JCheckBox("Green", true);
        blueChanelCheckbox = new JCheckBox("Blue", true);
        grayChannelCheckbox = new JCheckBox("Gray", true);
    }


    public JComponent createUI() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.add(controlPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(containerPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(340, 750));

        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        imageLabel = new JLabel();
        imagePanel.add(imageLabel);
        controlPanel.add(imagePanel);
        controlPanel.add(Box.createVerticalStrut(20));

        binSlider = new JSlider(10, 256, 256);
        binSlider.setMaximumSize(new Dimension(260, 40));
        binSlider.setMajorTickSpacing(50);
        binSlider.setPaintTicks(true);
        binSlider.setPaintLabels(true);
        binSlider.addChangeListener(e -> updateHistogram());
        controlPanel.add(binSlider);
        controlPanel.add(Box.createVerticalStrut(20));

        JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        checkBoxPanel.setBorder(BorderFactory.createTitledBorder("Color Channel"));
        checkBoxPanel.setMaximumSize(new Dimension(300, 40));

        checkBoxPanel.add(redChannelCheckbox);
        checkBoxPanel.add(greenChannelCheckbox);
        checkBoxPanel.add(blueChanelCheckbox);
        checkBoxPanel.add(grayChannelCheckbox);

        redChannelCheckbox.addActionListener(e -> updateHistogram());
        greenChannelCheckbox.addActionListener(e -> updateHistogram());
        blueChanelCheckbox.addActionListener(e -> updateHistogram());
        grayChannelCheckbox.addActionListener(e -> updateHistogram());

        controlPanel.add(checkBoxPanel);
        controlPanel.add(Box.createVerticalStrut(5));

        JPanel statisticBoxPanel = new JPanel(new GridBagLayout());
        statisticBoxPanel.setBorder(BorderFactory.createTitledBorder("Statistic"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        addLabeledComponent(statisticBoxPanel, "Image Size:", imageSize, gbc);
        gbc.gridy++;
        addLabeledComponent(statisticBoxPanel, "Mean Intensity:", meanIntensity, gbc);
        gbc.gridy++;
        addLabeledComponent(statisticBoxPanel, "Variance:", variance, gbc);
        gbc.gridy++;
        addLabeledComponent(statisticBoxPanel, "Standard Deviation:", stdDeviation, gbc);
        gbc.gridy++;
        addLabeledComponent(statisticBoxPanel, "Entropy:", entropy, gbc);
        gbc.gridy++;
        addLabeledComponent(statisticBoxPanel, "Energy:", energy, gbc);
        gbc.gridy++;
        addLabeledComponent(statisticBoxPanel, "Contrast:", contrast, gbc);

        controlPanel.add(statisticBoxPanel);
        controlPanel.add(Box.createVerticalStrut(5));

//        JPanel updateButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        JButton updateButton = new JButton("Update All");
//        updateButton.addActionListener(e -> {
//            updateHistogram();
//            updateStatistic();
//        });
//        updateButtonPanel.add(updateButton);
//        controlPanel.add(updateButtonPanel);
//        controlPanel.add(Box.createVerticalStrut(20));


        controlPanel.add(Box.createVerticalStrut(20));

        return scrollPane;
    }


    private void addLabeledComponent(JPanel panel, String labelText, JComponent component, GridBagConstraints gbc) {
        JLabel label = new JLabel(labelText);
        gbc.gridx = 0;
        panel.add(label, gbc);
        gbc.gridx = 1;
        panel.add(component, gbc);
    }


    private void updateHistogram() {
        if (isActive || isFrame) {
            SwingWorker<int[][], Void> worker = new SwingWorker<>() {
                @Override
                protected int[][] doInBackground() throws Exception {
                    int numBins = binSlider.getValue();
                    return ImageHelper.calculateCountIntensities(workingPanel.getCurrentImagePanel().getBufferedImage(), numBins);
                }

                @Override
                protected void done() {
                    try {
                        int[][] histogram = get();

                        int numBins = binSlider.getValue();
                        BufferedImage histogramImage = drawHistogram(histogram, numBins);
                        imageLabel.setIcon(new ImageIcon(histogramImage));
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    } finally {
                        workingPanel.setCursor(Cursor.getDefaultCursor());
                        workingPanel.hideProgressBar();
                    }
                }
            };
            worker.execute();
        }
    }


    private void updateStatistic() {
        SwingWorker<ImageStatisticsCalculator.ImageStatistics, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageStatisticsCalculator.ImageStatistics doInBackground() throws Exception {
                try {
                    return statisticsCalculator.calculateStatistics(workingPanel.getCurrentImagePanel().getBufferedImage());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    ImageStatisticsCalculator.ImageStatistics calc = get();

                    imageSize.setText(String.format("%spx  *  %spx", calc.getImageWidth(), calc.getImageHeight()));
                    meanIntensity.setText(String.valueOf(calc.getMeanIntensity()));
                    variance.setText(String.valueOf(calc.getVariance()));
                    stdDeviation.setText(String.valueOf(calc.getStdDeviation()));
                    entropy.setText(String.valueOf(calc.getEntropy()));
                    energy.setText(String.valueOf(calc.getEnergy()));
                    contrast.setText(String.valueOf(calc.getContrast()));
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                } finally {
                    workingPanel.setCursor(Cursor.getDefaultCursor());
                    workingPanel.hideProgressBar();
                }
            }
        };

        worker.execute();
    }


    public BufferedImage drawHistogram(int[][] histogram, int numBins) {
        boolean[] channelsToEqualize = new boolean[]{
                redChannelCheckbox.isSelected(),
                greenChannelCheckbox.isSelected(),
                blueChanelCheckbox.isSelected(),
                grayChannelCheckbox.isSelected()
        };
        int maxCount = 0;

        // Находим максимальное количество пикселей в гистограмме
        for (int[] colors : histogram) {
            for (int count : colors) {
                maxCount = Math.max(maxCount, count);
            }
        }

        // Создаем изображение с фиксированной шириной и высотой
        int width = 300; // Фиксированная ширина
        int height = 200; // Фиксированная высота
        BufferedImage histogramImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = histogramImage.createGraphics();

        // Заливаем фон белым цветом
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Устанавливаем ширину столбца
        int barWidth = width / (numBins - 1); // Учитываем, что последний столбец должен быть точно у края

        // Рисуем кривые для каждого канала
        for (int c = 0; c < histogram.length; c++) {
            if (!channelsToEqualize[c]) continue; // Пропускаем канал, если он отключен

            g2d.setColor(histogramColors[c]);

            // Создаем массивы для точек кривой
            int[] xPoints = new int[numBins];
            int[] yPoints = new int[numBins];

            for (int i = 0; i < numBins; i++) {
                xPoints[i] = i * (width - 1) / (numBins - 1); // Равномерное распределение с учетом последней точки
                int barHeight = maxCount > 0 ? (histogram[c][i] * (height - 30)) / maxCount : 0;
                yPoints[i] = height - barHeight - 30; // Отступ от нижней границы
            }

            // Рисуем кривую
            g2d.drawPolyline(xPoints, yPoints, numBins);
        }

        // Рисуем горизонтальную линейку
        int rulerHeight = 20; // Высота линейки
        int rulerY = height - rulerHeight; // y-координата линейки
        g2d.setColor(Color.BLACK);
        g2d.drawLine(0, rulerY, width, rulerY);

        // Рисуем деления на линейке
        int numTicks = 10; // Количество делений
        for (int i = 0; i <= numTicks; i++) {
            int x = (i * width) / numTicks;
            g2d.drawLine(x, rulerY, x, rulerY - 5); // Рисуем деление
            g2d.drawString(String.valueOf(i * (256 / numTicks)), x - 5, rulerY + 15); // Рисуем метку
        }


        g2d.dispose();
        return histogramImage;
    }


    public void setFrame(boolean frame) {
        isFrame = frame;
    }


    public WorkingPanel getImageWorkingPanel() {
        return workingPanel;
    }


}
