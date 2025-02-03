package org.knu.ui.tools;

import org.knu.bll.algorithms.colors.ColorFilter;
import org.knu.bll.algorithms.colors.GrayColorFilter;
import org.knu.ui.swing.WorkingPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class FiltersTool implements Tool {
    private final WorkingPanel workingPanel;
    private final ColorFilter[] colorFilters;
    private final JComboBox<ColorFilter> comboBox;

    public FiltersTool(WorkingPanel workingPanel) {
        this.workingPanel = workingPanel;
        colorFilters = new ColorFilter[]{new GrayColorFilter()};
        this.comboBox = new JComboBox<>(colorFilters);
    }


    @Override
    public String getName() {
        return "Filter Tool";
    }

    @Override
    public String getDescription() {
        return "Apply color filter";
    }

    @Override
    public Icon getIcon() {
        try (InputStream inputStream = getClass().getResourceAsStream("/icons/filter.png")) {
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

        panel.add(comboBox);

        panel.add(Box.createVerticalStrut(15));
        JButton applyButton = createApplyButton();
        panel.add(applyButton);
        panel.add(Box.createVerticalStrut(40));

        panel.add(Box.createVerticalStrut(15));

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
            BufferedImage outputImage = ((ColorFilter) comboBox.getSelectedItem()).applyColorFilter(workingPanel.getCurrentImagePanel().getBufferedImage());
            workingPanel.saveState();
            workingPanel.getCurrentImagePanel().setBufferedImage(outputImage);
            workingPanel.getCurrentImagePanel().updateImage(new ImageIcon(outputImage));
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
