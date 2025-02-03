package org.knu.ui.tools;

import javax.swing.*;
import java.awt.*;

public interface Tool {
    String getName();

    String getDescription();

    Icon getIcon();

    Component createSettingsPanel();

    void activate();

    void deactivate();

    void onImageFocusChanged();
}