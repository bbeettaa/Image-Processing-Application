package org.knu.ui.tools;

import org.knu.ui.swing.WorkingPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.io.InputStream;

public class ZoomTool implements Tool {
    WorkingPanel workingPanel;
    MouseWheelListener mwl;
    MouseAdapter ma;

    public ZoomTool(WorkingPanel workingPanel) {
        this.workingPanel = workingPanel;
        this.ma = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                 workingPanel.zoomCurrentImage((e.getButton() == 1) ? 1.2 : (e.getButton() == 3) ? 0.8 : 1.0, e.getPoint());
            }
        };
    }

    @Override
    public void activate() {
        if(workingPanel.getCurrentImagePanel()==null)
            return;
        workingPanel.setFocusable(true);
        workingPanel.getCurrentImagePanel().getImageLabel().addMouseListener(ma);
    }



    @Override
    public String getName() {
        return "Zoom tool";
    }

    @Override
    public String getDescription() {
        return "Increase or decrease the size of the image";
    }

    @Override
    public Icon getIcon() {
        try {
            InputStream inputStream = getClass().getResourceAsStream("/icons/magnifying-glass.png");
            Image  image = ImageIO.read(inputStream);
            return new ImageIcon(image);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public JPanel createSettingsPanel() {
        JPanel panel = new JPanel();

        JPanel updateButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton updateButton = new JButton("Reset scale");
        updateButton.addActionListener(e -> workingPanel.getCurrentImagePanel().setCurrentScale(1));
        updateButtonPanel.add(updateButton);
        panel.add(updateButtonPanel);
        panel.add(Box.createVerticalStrut(20));


        return panel;
    }

    @Override
    public void deactivate() {
        if(workingPanel.getCurrentImagePanel()==null)
            return;
        workingPanel.getCurrentImagePanel().getImageLabel().removeMouseListener(ma);
        workingPanel.setCursor(Cursor.getDefaultCursor());
    }

    @Override
    public void onImageFocusChanged() {

    }
}
