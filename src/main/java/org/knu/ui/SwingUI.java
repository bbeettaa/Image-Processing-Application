package org.knu.ui;

import org.knu.bll.FileService;
import org.knu.ui.swing.WorkingPanel;
import org.knu.ui.swing.SingleImagePanel;
import org.knu.ui.tools.SeparatorTool;
import org.knu.ui.tools.Tool;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileView;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class SwingUI extends JFrame {
    private final FileService fileService;
    private final WorkingPanel workingPanel;
    private JPanel settingsPanel;

    private final List<Tool> tools;
    private final List<JMenu> menus;
    private Tool currentTool;

    private boolean isSettingsPanelVisible = true;

    public SwingUI(WorkingPanel workingPanel, FileService fileService, List<Tool> tools, List<JMenu> menus) {
        this.fileService = fileService;
        this.workingPanel = workingPanel;
        this.menus = menus;
        this.workingPanel.setParentJFrame(this);
        this.tools = tools;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        initializeUI(this.getWidth() - 300);
        setVisible(true);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
                    if (e.isShiftDown()) {
                        workingPanel.redo();
                    } else {
                        workingPanel.undo();
                    }
                }
            }
            return false; // Return false to allow the event to be processed by other listeners
        });
    }

    private void initializeUI(int divider) {
        settingsPanel = createSettingsPanel();
        JToolBar toolBar = createToolBar();
        JSplitPane mainSplitPane = createMainSplitPane(divider);

        add(toolBar, BorderLayout.WEST);
        add(mainSplitPane, BorderLayout.CENTER);

        setJMenuBar(createMenuBar());
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(150, getHeight()));
        panel.setSize(new Dimension(250, getHeight()));
        panel.setBorder(BorderFactory.createTitledBorder("Tool settings"));
        return panel;
    }

    private JToolBar createToolBar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));

        toolbar.add(Box.createVerticalStrut(10));

        for (Tool tool : tools) {
            if (tool instanceof SeparatorTool) {
                toolbar.add(Box.createVerticalStrut(5));
                JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
                separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                toolbar.add(separator);
                toolbar.add(Box.createVerticalStrut(10));
            } else {
                JButton toolButton;
                if (tool.getIcon() != null) toolButton = new JButton(tool.getIcon());
                else toolButton = new JButton(tool.getName());
                toolButton.setToolTipText(tool.getName());
                toolButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                toolButton.addActionListener(e -> selectTool(tool));
                toolbar.add(toolButton);
                toolbar.add(Box.createVerticalStrut(5));
            }
        }

        return toolbar;
    }

    private JSplitPane createMainSplitPane(int divider) {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, workingPanel, settingsPanel);
        splitPane.setDividerLocation(divider);
        splitPane.setResizeWeight(1.0);
        splitPane.setContinuousLayout(true);
        return splitPane;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createViewMenu());
        menuBar.add(createEditMenu());

        for (var menu : menus)
            menuBar.add(menu);

        return menuBar;
    }

    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("File");

        JMenuItem saveFile = new JMenuItem("Save File");
        saveFile.addActionListener(e -> saveFile());
        fileMenu.add(saveFile);

        JMenuItem loadFile = new JMenuItem("Load Images");
        loadFile.addActionListener(e -> loadImages());
        fileMenu.add(loadFile);

        fileMenu.addSeparator();

        JMenuItem closeFile = new JMenuItem("Close File");
        closeFile.addActionListener(e -> closeFile());
        fileMenu.add(closeFile);
        JMenuItem closeFileAll = new JMenuItem("Close All Files");
        closeFileAll.addActionListener(e -> closeAllFiles());
        fileMenu.add(closeFileAll);

        fileMenu.addSeparator();

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> System.exit(0));
        fileMenu.add(exit);

        return fileMenu;
    }

    private JMenu createViewMenu() {
        JMenu viewMenu = new JMenu("View");

        JCheckBoxMenuItem toggleSettings = new JCheckBoxMenuItem("Show Settings", isSettingsPanelVisible);
        toggleSettings.addActionListener(e -> toggleSettingsPanel(toggleSettings.isSelected()));
        viewMenu.add(toggleSettings);

        JMenuItem floatingCurrentPanel = new JMenuItem("Detach Current Window");
        floatingCurrentPanel.addActionListener(e -> {
            workingPanel.detachCurrentTab().setLocationRelativeTo(this);
        });
        viewMenu.add(floatingCurrentPanel);

        return viewMenu;
    }

    private JMenu createEditMenu() {
        JMenu editMenu = new JMenu("Edit");

        JMenuItem undoButton = new JMenuItem("Undo");
        undoButton.addActionListener(e -> workingPanel.undo());
        editMenu.add(undoButton);

        JMenuItem redoButton = new JMenuItem("Redo");
        redoButton.addActionListener(e -> workingPanel.redo());
        editMenu.add(redoButton);

        return editMenu;
    }

    private void loadImages() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "png", "jpg", "jpeg"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            // Show progress indicator
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            JOptionPane progressPane = new JOptionPane(progressBar, JOptionPane.PLAIN_MESSAGE);
            JDialog dialog = progressPane.createDialog(this, "Loading Images");
            progressPane.setVisible(true);

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    for (File file : fileChooser.getSelectedFiles()) {
                        BufferedImage image = fileService.loadImage(file.getPath());
                        workingPanel.addNewTabImage(image, file.getName(), file.getAbsolutePath());
                    }
                    return null;
                }

                @Override
                protected void done() {
                    dialog.setVisible(false);
                }
            };
            worker.execute();
        }
    }



    private void saveFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (workingPanel.getCurrentImagePanel() != null)
            fileChooser.setSelectedFile(new File(workingPanel.getCurrentImagePanel().getAbsolutePath()));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "png", "jpg", "jpeg"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String filePath = ensureImageExtension(fileChooser.getSelectedFile().getAbsolutePath());
            try {
                fileService.saveImage(new File(filePath), workingPanel.getCurrentImagePanel().getBufferedImage());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to save image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private String ensureImageExtension(String filePath) {
        if (!filePath.toLowerCase().matches(".*\\.(png|jpg|jpeg)$")) {
            return filePath + ".png"; // Default save as PNG
        }
        return filePath;
    }

    private void closeFile() {
        SingleImagePanel activePanel = workingPanel.getCurrentImagePanel();
        if (activePanel != null &&
                JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure?",
                        "Close File",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            workingPanel.removeActivePanel();
        }
    }

    private void closeAllFiles() {
        SingleImagePanel activePanel = workingPanel.getCurrentImagePanel();
        if (activePanel != null &&
                JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure?",
                        "Close All Files",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            workingPanel.getTabbedPane().removeAll();
        }
    }

    private void toggleSettingsPanel(boolean isVisible) {
        isSettingsPanelVisible = isVisible;
        settingsPanel.setVisible(isVisible);
    }

    private void selectTool(Tool tool) {
        if (currentTool != null) {
            currentTool.deactivate();
        }
        settingsPanel.setBorder(BorderFactory.createTitledBorder(tool.getName()));
        currentTool = tool;
        settingsPanel.removeAll();
        settingsPanel.add(tool.createSettingsPanel());
        settingsPanel.revalidate();
        settingsPanel.repaint();
        currentTool.activate();
    }

    class ImagePreview extends JComponent {
        private JFileChooser fileChooser;
        private JLabel previewLabel;

        public ImagePreview(JFileChooser fileChooser) {
            this.fileChooser = fileChooser;
            previewLabel = new JLabel();
            previewLabel.setPreferredSize(new Dimension(100, 100));
            previewLabel.setBorder(BorderFactory.createEtchedBorder());
            previewLabel.setHorizontalAlignment(JLabel.CENTER);
            add(previewLabel);
            fileChooser.addPropertyChangeListener(evt -> {
                if (evt.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
                    File file = fileChooser.getSelectedFile();
                    if (file != null) {
                        ImageIcon icon = new ImageIcon(file.getPath());
                        if (icon.getIconWidth() > 100) {
                            icon = new ImageIcon(icon.getImage().getScaledInstance(100, -1, Image.SCALE_DEFAULT));
                        }
                        previewLabel.setIcon(icon);
                    }
                }
            });
        }
    }

    class ImageFileView extends FileView {
        @Override
        public String getName(File f) {
            return null; // use default file name
        }

        @Override
        public String getDescription(File f) {
            return null; // use default file description
        }

        @Override
        public Boolean isTraversable(File f) {
            return null; // use default directory traversal
        }

        @Override
        public String getTypeDescription(File f) {
            String extension = getExtension(f);
            String type = null;
            if (extension != null) {
                if (extension.equals("jpeg") || extension.equals("jpg")) {
                    type = "JPEG Image";
                } else if (extension.equals("gif")) {
                    type = "GIF Image";
                } else if (extension.equals("tiff") || extension.equals("tif")) {
                    type = "TIFF Image";
                } else if (extension.equals("png")) {
                    type = "PNG Image";
                }
            }
            return type;
        }

        @Override
        public Icon getIcon(File f) {
            String extension = getExtension(f);
            if (extension != null) {
                if (extension.equals("jpeg") || extension.equals("jpg")) {
                    return new ImageIcon("path/to/jpegIcon.png");
                } else if (extension.equals("gif")) {
                    return new ImageIcon("path/to/gifIcon.png");
                } else if (extension.equals("tiff") || extension.equals("tif")) {
                    return new ImageIcon("path/to/tiffIcon.png");
                } else if (extension.equals("png")) {
                    return new ImageIcon("path/to/pngIcon.png");
                }
            }
            return null;
        }

        private String getExtension(File f) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');
            if (i > 0 && i < s.length() - 1) {
                ext = s.substring(i + 1).toLowerCase();
            }
            return ext;
        }
    }
}
