package org.knu.ui.tools;

import javax.swing.*;

public class SeparatorTool implements Tool {

    @Override
    public String getName() {
        return "separator";
    }

    @Override
    public String getDescription() {
        return "separator";
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public JPanel createSettingsPanel() {
        return null;
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
