package org.knu.ui.swing;

import org.knu.ui.tools.Tool;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class WorkingPanel extends JPanel {
    private final JTabbedPane tabbedPane;
    private SingleImagePanel activePanel;
    private final JProgressBar progressBar;
    private String title;
    private JFrame parentJframe;
    private Tool[] tools;

    public WorkingPanel() {
        setLayout(new BorderLayout());

        tabbedPane = createTabbedPane();
        progressBar = createProgressBar();

        add(tabbedPane, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);

    }

    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && tabbedPane.getSelectedIndex() >= 0) {
                    setActivePanel(getSelectedPanel());
                }
            }
        });
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() != -1) {
                setActivePanel(getSelectedPanel());
            }
        });

        return tabbedPane;
    }

    public void setActivePanel(SingleImagePanel SelectedPanel) {
        activePanel = SelectedPanel;
        parentJframe.setTitle(activePanel.getName());
        notifyTools();
    }

    public void setParentJFrame(JFrame parentJFrame) {
        this.parentJframe = parentJFrame;
    }

    private JProgressBar createProgressBar() {
        JProgressBar progressBar = new JProgressBar();
        progressBar.setVisible(false);
        return progressBar;
    }

    private SingleImagePanel getSelectedPanel() {
        return (SingleImagePanel) tabbedPane.getSelectedComponent();
    }


    public void addNewTabImage(BufferedImage image, String fileName, String absolutePath) {
        SingleImagePanel imagePanel = new SingleImagePanel(this, image, fileName, absolutePath);
        imagePanel.setFocusable(true);
        tabbedPane.addTab(fileName, imagePanel);
    }

    public SingleImagePanel getCurrentImagePanel() {
        return activePanel;
    }

    public void zoomCurrentImage(double scale, Point cursorPosition) {
        if (activePanel != null) {
            activePanel.zoomImage(scale, cursorPosition);
        }
    }

    public JFrame detachCurrentTab() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex == -1) {
            return null;
        }

        String title = tabbedPane.getTitleAt(selectedIndex);
        SingleImagePanel panel = (SingleImagePanel) tabbedPane.getComponentAt(selectedIndex);

        tabbedPane.remove(selectedIndex);
        return createFloatingWindow(panel, title);
    }

    private JFrame createFloatingWindow(SingleImagePanel panel, String name) {
        JFrame floatingFrame = new JFrame(name);
        floatingFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        floatingFrame.add(panel);

        Dimension parentSize = SwingUtilities.getWindowAncestor(this).getSize();
        floatingFrame.setMinimumSize(new Dimension(500, 500));
        floatingFrame.setLocationRelativeTo(null);

        floatingFrame.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                setActivePanel(panel);
            }
        });

        floatingFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                reattachTab(panel, name);
                if (panel.getDetachedFrame() != null) {
                    panel.setDetachedFrame(null);
                }
            }
        });

        parentJframe.setTitle(activePanel.getName());
        floatingFrame.setVisible(true);
        activePanel = panel;
        panel.setDetachedFrame(floatingFrame);
        return floatingFrame;
    }

    public void reattachTab(SingleImagePanel panel, String title) {
        tabbedPane.addTab(title, panel);
    }

    public void removeActivePanel() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex != -1) {
            tabbedPane.removeTabAt(selectedIndex);
        }
    }

    public void showProgressBar() {
        progressBar.setVisible(true);
    }

    public void hideProgressBar() {
        progressBar.setVisible(false);
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }


    public void saveState() {
        getCurrentImagePanel().saveState();
        notifyTools();
    }

    public void undo() {
        getCurrentImagePanel().undo();
        notifyTools();
    }

    public void redo() {
        getCurrentImagePanel().redo();
        notifyTools();
    }


    public void setNotificationTool(Tool... tools) {
        this.tools = tools;
    }

    public void notifyTools() {
        for (Tool t : tools) {
            t.onImageFocusChanged();
        }
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }
}
