/*
 * Copyright 2020 Cosinus Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */package org.cosinus.swing.color;

/**
 * System colors enum
 */
public enum SystemColor {
    BUTTON_BACKGROUND("Button.background"),
    BUTTON_DARK_SHADOW("Button.darkShadow"),
    BUTTON_DISABLED_TEXT("Button.disabledText"),
    BUTTON_FOREGROUND("Button.foreground"),
    BUTTON_HIGHLIGHT("Button.highlight"),
    BUTTON_LIGHT("Button.light"),
    BUTTON_SELECT("Button.select"),
    BUTTON_SHADOW("Button.shadow"),
    CHECKBOX_BACKGROUND("CheckBox.background"),
    CHECKBOX_DISABLED_TEXT("CheckBox.disabledText"),
    CHECKBOX_FOREGROUND("CheckBox.foreground"),
    CHECKBOX_SELECT("CheckBox.select"),
    CHECKBOX_MENUITEM_ACCELERATOR_FOREGROUND("CheckBoxMenuItem.acceleratorForeground"),
    CHECKBOX_MENUITEM_ACCELERATOR_SELECTION_FOREGROUND("CheckBoxMenuItem.acceleratorSelectionForeground"),
    CHECKBOX_MENUITEM_BACKGROUND("CheckBoxMenuItem.background"),
    CHECKBOX_MENUITEM_DISABLED_BACKGROUND("CheckBoxMenuItem.disabledBackground"),
    CHECKBOX_MENUITEM_DISABLED_FOREGROUND("CheckBoxMenuItem.disabledForeground"),
    CHECKBOX_MENUITEM_FOREGROUND("CheckBoxMenuItem.foreground"),
    CHECKBOX_MENUITEM_SELECTION_BACKGROUND("CheckBoxMenuItem.selectionBackground"),
    CHECKBOX_MENUITEM_SELECTION_FOREGROUND("CheckBoxMenuItem.selectionForeground"),
    COLOR_CHOOSER_BACKGROUND("ColorChooser.background"),
    COLOR_CHOOSER_FOREGROUND("ColorChooser.foreground"),
    COLOR_CHOOSER_SWATCHES_DEFAULT_RECENT_COLOR("ColorChooser.swatchesDefaultRecentColor"),
    COMBOBOX_BACKGROUND("ComboBox.background"),
    COMBOBOX_BUTTON_BACKGROUND("ComboBox.buttonBackground"),
    COMBOBOX_BUTTON_DARK_SHADOW("ComboBox.buttonDarkShadow"),
    COMBOBOX_BUTTON_HIGHLIGHT("ComboBox.buttonHighlight"),
    COMBOBOX_BUTTON_SHADOW("ComboBox.buttonShadow"),
    COMBOBOX_DISABLED_BACKGROUND("ComboBox.disabledBackground"),
    COMBOBOX_DISABLED_FOREGROUND("ComboBox.disabledForeground"),
    COMBOBOX_FOREGROUND("ComboBox.foreground"),
    COMBOBOX_SELECTION_BACKGROUND("ComboBox.selectionBackground"),
    COMBOBOX_SELECTION_FOREGROUND("ComboBox.selectionForeground"),
    DESKTOP_BACKGROUND("Desktop.background"),
    EDITOR_PANE_BACKGROUND("EditorPane.background"),
    EDITOR_PANE_CARET_FOREGROUND("EditorPane.caretForeground"),
    EDITOR_PANE_FOREGROUND("EditorPane.foreground"),
    EDITOR_PANE_INACTIVE_BACKGROUND("EditorPane.inactiveBackground"),
    EDITOR_PANE_INACTIVE_FOREGROUND("EditorPane.inactiveForeground"),
    EDITOR_PANE_SELECTION_BACKGROUND("EditorPane.selectionBackground"),
    EDITOR_PANE_SELECTION_FOREGROUND("EditorPane.selectionForeground"),
    FOCUS_COLOR("Focus.color"),
    FORMATTED_TEXT_FIELD_BACKGROUND("FormattedTextField.background"),
    FORMATTED_TEXT_FIELD_CARET_FOREGROUND("FormattedTextField.caretForeground"),
    FORMATTED_TEXT_FIELD_FOREGROUND("FormattedTextField.foreground"),
    FORMATTED_TEXT_FIELD_INACTIVE_BACKGROUND("FormattedTextField.inactiveBackground"),
    FORMATTED_TEXT_FIELD_INACTIVE_FOREGROUND("FormattedTextField.inactiveForeground"),
    FORMATTED_TEXT_FIELD_SELECTION_BACKGROUND("FormattedTextField.selectionBackground"),
    FORMATTED_TEXT_FIELD_SELECTION_FOREGROUND("FormattedTextField.selectionForeground"),
    INTERNAL_FRAME_ACTIVE_TITLE_BACKGROUND("InternalFrame.activeTitleBackground"),
    INTERNAL_FRAME_ACTIVE_TITLE_FOREGROUND("InternalFrame.activeTitleForeground"),
    INTERNAL_FRAME_BACKGROUND("InternalFrame.background"),
    INTERNAL_FRAME_BORDER_COLOR("InternalFrame.borderColor"),
    INTERNAL_FRAME_BORDER_DARK_SHADOW("InternalFrame.borderDarkShadow"),
    INTERNAL_FRAME_BORDER_HIGHLIGHT("InternalFrame.borderHighlight"),
    INTERNAL_FRAME_BORDER_LIGHT("InternalFrame.borderLight"),
    INTERNAL_FRAME_BORDER_SHADOW("InternalFrame.borderShadow"),
    INTERNAL_FRAME_INACTIVE_TITLE_BACKGROUND("InternalFrame.inactiveTitleBackground"),
    INTERNAL_FRAME_INACTIVE_TITLE_FOREGROUND("InternalFrame.inactiveTitleForeground"),
    INTERNAL_FRAME_OPTION_DIALOG_BACKGROUND("InternalFrame.optionDialogBackground"),
    INTERNAL_FRAME_PALETTE_BACKGROUND("InternalFrame.paletteBackground"),
    LABEL_BACKGROUND("Label.background"),
    LABEL_DISABLED_FOREGROUND("Label.disabledForeground"),
    LABEL_DISABLED_SHADOW("Label.disabledShadow"),
    LABEL_FOREGROUND("Label.foreground"),
    LIST_BACKGROUND("List.background"),
    LIST_FOREGROUND("List.foreground"),
    LIST_SELECTION_BACKGROUND("List.selectionBackground"),
    LIST_SELECTION_FOREGROUND("List.selectionForeground"),
    MENU_ACCELERATOR_FOREGROUND("Menu.acceleratorForeground"),
    MENU_ACCELERATOR_SELECTION_FOREGROUND("Menu.acceleratorSelectionForeground"),
    MENU_BACKGROUND("Menu.background"),
    MENU_DISABLED_BACKGROUND("Menu.disabledBackground"),
    MENU_DISABLED_FOREGROUND("Menu.disabledForeground"),
    MENU_FOREGROUND("Menu.foreground"),
    MENU_SELECTION_BACKGROUND("Menu.selectionBackground"),
    MENU_SELECTION_FOREGROUND("Menu.selectionForeground"),
    MENUBAR_BACKGROUND("MenuBar.background"),
    MENUBAR_DISABLED_BACKGROUND("MenuBar.disabledBackground"),
    MENUBAR_DISABLED_FOREGROUND("MenuBar.disabledForeground"),
    MENUBAR_FOREGROUND("MenuBar.foreground"),
    MENUBAR_HIGHLIGHT("MenuBar.highlight"),
    MENUBAR_SELECTION_BACKGROUND("MenuBar.selectionBackground"),
    MENUBAR_SELECTION_FOREGROUND("MenuBar.selectionForeground"),
    MENUBAR_SHADOW("MenuBar.shadow"),
    MENUITEM_ACCELERATOR_FOREGROUND("MenuItem.acceleratorForeground"),
    MENUITEM_ACCELERATOR_SELECTION_FOREGROUND("MenuItem.acceleratorSelectionForeground"),
    MENUITEM_BACKGROUND("MenuItem.background"),
    MENUITEM_DISABLED_BACKGROUND("MenuItem.disabledBackground"),
    MENUITEM_DISABLED_FOREGROUND("MenuItem.disabledForeground"),
    MENUITEM_FOREGROUND("MenuItem.foreground"),
    MENUITEM_SELECTION_BACKGROUND("MenuItem.selectionBackground"),
    MENUITEM_SELECTION_FOREGROUND("MenuItem.selectionForeground"),
    OPTION_PANE_BACKGROUND("OptionPane.background"),
    OPTION_PANE_FOREGROUND("OptionPane.foreground"),
    OPTION_PANE_MESSAGE_FOREGROUND("OptionPane.messageForeground"),
    PANEL_BACKGROUND("Panel.background"),
    PANEL_FOREGROUND("Panel.foreground"),
    PASSWORD_FIELD_BACKGROUND("PasswordField.background"),
    PASSWORD_FIELD_MESSAGE_FOREGROUND("PasswordField.ç"),
    PASSWORD_FIELD_FOREGROUND("PasswordField.foreground"),
    PASSWORD_FIELD_INACTIVE_BACKGROUND("PasswordField.inactiveBackground"),
    PASSWORD_FIELD_INACTIVE_FOREGROUND("PasswordField.inactiveForeground"),
    PASSWORD_FIELD_SELECTION_BACKGROUND("PasswordField.selectionBackground"),
    PASSWORD_FIELD_SELECTION_FOREGROUND("PasswordField.selectionForeground"),
    POPUPMENU_BACKGROUND("PopupMenu.background"),
    POPUPMENU_FOREGROUND("PopupMenu.foreground"),
    POPUPMENU_SELECTION_BACKGROUND("PopupMenu.selectionBackground"),
    POPUPMENU_SELECTION_FOREGROUND("PopupMenu.selectionForeground"),
    PROGRESSBAR_BACKGROUND("ProgressBar.background"),
    PROGRESSBAR_FOREGROUND("ProgressBar.foreground"),
    PROGRESSBAR_SELECTION_BACKGROUND("ProgressBar.selectionBackground"),
    PROGRESSBAR_SELECTION_FOREGROUND("ProgressBar.selectionForeground"),
    RADIOBUTTON_BACKGROUND("RadioButton.background"),
    RADIOBUTTON_DARK_SHADOW("RadioButton.darkShadow"),
    RADIOBUTTON_DISABLED_TEXT("RadioButton.disabledText"),
    RADIOBUTTON_FOREGROUND("RadioButton.foreground"),
    RADIOBUTTON_HIGHLIGHT("RadioButton.highlight"),
    RADIOBUTTON_LIGHT("RadioButton.light"),
    RADIOBUTTON_SELECT("RadioButton.select"),
    RADIOBUTTON_SHADOW("RadioButton.shadow"),
    RADIOBUTTON_MENUITEM_ACCELERATOR_FOREGROUND("RadioButtonMenuItem.acceleratorForeground"),
    RADIOBUTTON_MENUITEM_ACCELERATOR_SELECTION_FOREGROUND("RadioButtonMenuItem.acceleratorSelectionForeground"),
    RADIOBUTTON_MENUITEM_BACKGROUND("RadioButtonMenuItem.background"),
    RADIOBUTTON_MENUITEM_DISABLED_BACKGROUND("RadioButtonMenuItem.disabledBackground"),
    RADIOBUTTON_MENUITEM_DISABLED_FOREGROUND("RadioButtonMenuItem.disabledForeground"),
    RADIOBUTTON_MENUITEM_FOREGROUND("RadioButtonMenuItem.foreground"),
    RADIOBUTTON_MENUITEM_SELECTION_BACKGROUND("RadioButtonMenuItem.selectionBackground"),
    RADIOBUTTON_MENUITEM_SELECTION_FOREGROUND("RadioButtonMenuItem.selectionForeground"),
    SCROLLBAR_BACKGROUND("ScrollBar.background"),
    SCROLLBAR_FOREGROUND("ScrollBar.foreground"),
    SCROLLBAR_THUMB("ScrollBar.thumb"),
    SCROLLBAR_THUMB_DARK_SHADOW("ScrollBar.thumbDarkShadow"),
    SCROLLBAR_THUMB_HIGHLIGHT("ScrollBar.thumbHighlight"),
    SCROLLBAR_THUMB_SHADOW("ScrollBar.thumbShadow"),
    SCROLLBAR_TRACK("ScrollBar.track"),
    SCROLLBAR_TRACK_HIGHLIGHT("ScrollBar.trackHighlight"),
    SCROLL_PANE_BACKGROUND("ScrollPane.background"),
    SCROLL_PANE_FOREGROUND("ScrollPane.foreground"),
    SEPARATOR_FOREGROUND("Separator.foreground"),
    SEPARATOR_HIGHLIGHT("Separator.highlight"),
    SEPARATOR_SHADOW("Separator.shadow"),
    SLIDER_BACKGROUND("Slider.background"),
    SLIDER_FOCUS("Slider.focus"),
    SLIDER_FOREGROUND("Slider.foreground"),
    SLIDER_HIGHLIGHT("Slider.highlight"),
    SLIDER_SHADOW("Slider.shadow"),
    SLIDER_TICK_COLOR("Slider.tickColor"),
    SPINNER_BACKGROUND("Spinner.background"),
    SPINNER_FOREGROUND("Spinner.foreground"),
    SPLIT_PANE_BACKGROUND("SplitPane.background"),
    SPLIT_PANE_DARK_SHADOW("SplitPane.darkShadow"),
    SPLIT_PANE_HIGHLIGHT("SplitPane.highlight"),
    SPLIT_PANE_SHADOW("SplitPane.shadow"),
    SPLIT_PANE_DIVIDER_DRAGGING_COLOR("SplitPaneDivider.draggingColor"),
    TABBED_PANE_BACKGROUND("TabbedPane.background"),
    TABBED_PANE_DARK_SHADOW("TabbedPane.darkShadow"),
    TABBED_PANE_FOCUS("TabbedPane.focus"),
    TABBED_PANE_FOREGROUND("TabbedPane.foreground"),
    TABBED_PANE_HIGHLIGHT("TabbedPane.highlight"),
    TABBED_PANE_LIGHT("TabbedPane.light"),
    TABBED_PANE_SHADOW("TabbedPane.shadow"),
    TABLE_BACKGROUND("Table.background"),
    TABLE_FOCUS_CELL_BACKGROUND("Table.focusCellBackground"),
    TABLE_FOCUS_CELL_FOREGROUND("Table.focusCellForeground"),
    TABLE_FOREGROUND("Table.foreground"),
    TABLE_GRID_COLOR("Table.gridColor"),
    TABLE_SELECTION_BACKGROUND("Table.selectionBackground"),
    TABLE_SELECTION_FOREGROUND("Table.selectionForeground"),
    TABLE_HEADER_BACKGROUND("TableHeader.background"),
    TABLE_HEADER_FOREGROUND("TableHeader.foreground"),
    TEXTAREA_BACKGROUND("TextArea.background"),
    TEXTAREA_CARET_FOREGROUND("TextArea.caretForeground"),
    TEXTAREA_FOREGROUND("TextArea.foreground"),
    TEXTAREA_INACTIVE_BACKGROUND("TextArea.inactiveBackground"),
    TEXTAREA_INACTIVE_FOREGROUND("TextArea.inactiveForeground"),
    TEXTAREA_SELECTION_BACKGROUND("TextArea.selectionBackground"),
    TEXTAREA_SELECTION_FOREGROUND("TextArea.selectionForeground"),
    TEXT_COMPONENT_SELECTION_BACKGROUND_INACTIVE("TextComponent.selectionBackgroundInactive"),
    TEXT_FIELD_BACKGROUND("TextField.background"),
    TEXT_FIELD_CARET_FOREGROUND("TextField.caretForeground"),
    TEXT_FIELD_DARK_SHADOW("TextField.darkShadow"),
    TEXT_FIELD_FOREGROUND("TextField.foreground"),
    TEXT_FIELD_HIGHLIGHT("TextField.highlight"),
    TEXT_FIELD_INACTIVE_BACKGROUND("TextField.inactiveBackground"),
    TEXT_FIELD_INACTIVE_FOREGROUND("TextField.inactiveForeground"),
    TEXT_FIELD_LIGHT("TextField.light"),
    TEXT_FIELD_SELECTION_BACKGROUND("TextField.selectionBackground"),
    TEXT_FIELD_SELECTION_FOREGROUND("TextField.selectionForeground"),
    TEXT_FIELD_SHADOW("TextField.shadow"),
    TEXT_PANE_BACKGROUND("TextPane.background"),
    TEXT_PANE_CARET_FOREGROUND("TextPane.caretForeground"),
    TEXT_PANE_FOREGROUND("TextPane.foreground"),
    TEXT_PANE_INACTIVE_BACKGROUND("TextPane.inactiveBackground"),
    TEXT_PANE_INACTIVE_FOREGROUND("TextPane.inactiveForeground"),
    TEXT_PANE_SELECTION_BACKGROUND("TextPane.selectionBackground"),
    TEXT_PANE_SELECTION_FOREGROUND("TextPane.selectionForeground"),
    TITLED_BORDER_TITLE_COLOR("TitledBorder.titleColor"),
    TOGGLE_BUTTON_BACKGROUND("ToggleButton.background"),
    TOGGLE_BUTTON_DARK_SHADOW("ToggleButton.darkShadow"),
    TOGGLE_BUTTON_DISABLED_TEXT("ToggleButton.disabledText"),
    TOGGLE_BUTTON_FOREGROUND("ToggleButton.foreground"),
    TOGGLE_BUTTON_HIGHLIGHT("ToggleButton.highlight"),
    TOGGLE_BUTTON_LIGHT("ToggleButton.light"),
    TOGGLE_BUTTON_SHADOW("ToggleButton.shadow"),
    TOOLBAR_BACKGROUND("ToolBar.background"),
    TOOLBAR_DARK_SHADOW("ToolBar.darkShadow"),
    TOOLBAR_DOCKING_BACKGROUND("ToolBar.dockingBackground"),
    TOOLBAR_DOCKING_FOREGROUND("ToolBar.dockingForeground"),
    TOOLBAR_FLOATING_BACKGROUND("ToolBar.floatingBackground"),
    TOOLBAR_FLOATING_FOREGROUND("ToolBar.floatingForeground"),
    TOOLBAR_FOREGROUND("ToolBar.foreground"),
    TOOLBAR_HIGHLIGHT("ToolBar.highlight"),
    TOOLBAR_LIGHT("ToolBar.light"),
    TOOLBAR_SHADOW("ToolBar.shadow"),
    TOOLTIP_BACKGROUND("ToolTip.background"),
    TREE_BACKGROUND("Tree.background"),
    TREE_FOREGROUND("Tree.foreground"),
    TREE_HASH("Tree.hash"),
    TREE_LINE("Tree.line"),
    TREE_SELECTION_BACKGROUND("Tree.selectionBackground"),
    TREE_SELECTION_BORDER_COLOR("Tree.selectionBorderColor"),
    TREE_SELECTION_FOREGROUND("Tree.selectionForeground"),
    TREE_TEXT_BACKGROUND("Tree.textBackground"),
    TREE_TEXT_FOREGROUND("Tree.textForeground"),
    VIEWPORT_BACKGROUND("Viewport.background"),
    VIEWPORT_FOREGROUND("Viewport.foreground"),
    ACTIVE_CAPTION("activeCaption"),
    ACTIVE_CAPTION_BORDER("activeCaptionBorder"),
    ACTIVE_CAPTION_TEXT("activeCaptionText"),
    CONTROL("control"),
    CONTROL_DK_SHADOW("controlDkShadow"),
    CONTROL_HIGHLIGHT("controlHighlight"),
    CONTROL_LT_HIGHLIGHT("controlLtHighlight"),
    CONTROL_SHADOW("controlShadow"),
    CONTROL_TEXT("controlText"),
    DESKTOP("desktop"),
    INACTIVE_CAPTION("inactiveCaption"),
    INACTIVE_CAPTION_BORDER("inactiveCaptionBorder"),
    INACTIVE_CAPTION_TEXT("inactiveCaptionText"),
    INFO("info"),
    INFO_TEXT("infoText"),
    MENU("menu"),
    MENU_TEXT("menuText"),
    SCROLLBAR("scrollbar"),
    TEXT("text"),
    TEXT_HIGHLIGHT("textHighlight"),
    TEXT_HIGHLIGHT_TEXT("textHighlightText"),
    TEXT_INACTIVE_TEXT("textInactiveText"),
    TEXT_TEXT("textText"),
    window("window"),
    WINDOW_BORDER("windowBorder"),
    WINDOW_TEXT("windowText");


    private String key;

    SystemColor(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}