/*
 * Copyright 2025 Cosinus Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.cosinus.swing.ui;

public enum SystemText {
    OPTION_PANE_YES_BUTTON_TEXT("OptionPane.yesButtonText"),
    OPTION_PANE_NO_BUTTON_TEXT("OptionPane.noButtonText"),
    OPTION_PANE_CANCEL_BUTTON_TEXT("OptionPane.cancelButtonText"),
    COLOR_CHOOSER_OK_TEXT("ColorChooser.okText"),
    COLOR_CHOOSER_CANCEL_TEXT("ColorChooser.cancelText"),
    COLOR_CHOOSER_RESET_TEXT("ColorChooser.resetText"),
    COLOR_CHOOSER_SWATCHES_NAME_TEXT("ColorChooser.swatchesNameText"),
    COLOR_CHOOSER_SWATCHES_RECENT_TEXT("ColorChooser.swatchesRecentText"),
    COLOR_CHOOSER_HSB_NAME_TEXT("ColorChooser.hsbNameText"),
    COLOR_CHOOSER_RGB_NAME_TEXT("ColorChooser.rgbNameText"),
    COLOR_CHOOSER_PREVIEW_TEXT("ColorChooser.previewText"),
    COLOR_CHOOSER_RGB_RED_TEXT("ColorChooser.rgbRedText"),
    COLOR_CHOOSER_RGB_GREENTEXT("ColorChooser.rgbGreenText"),
    COLOR_CHOOSER_RGB_BLUE_TEXT("ColorChooser.rgbBlueText"),
    COLOR_CHOOSER_HSB_RED_TEXT("ColorChooser.hsbRedText"),
    COLOR_CHOOSER_HSB_GREEN_TEXT("ColorChooser.hsbGreenText"),
    COLOR_CHOOSER_HSB_BLUE_TEXT("ColorChooser.hsbBlueText"),
    COLOR_CHOOSER_HSB_HUE_TEXT("ColorChooser.hsbHueText"),
    COLOR_CHOOSER_HSB_SATURATION_TEXT("ColorChooser.hsbSaturationText"),
    COLOR_CHOOSER_HSB_BRIGHTNESS_TEXT("ColorChooser.hsbBrightnessText"),
    COLOR_CHOOSER_SAMPLE_TEXT("ColorChooser.sampleText"),
    FILE_CHOOSER_ACCEPT_ALL_FILE_FILTER_TEXT("FileChooser.acceptAllFileFilterText"),
    FILE_CHOOSER_CANCEL_BUTTON_TEXT("FileChooser.cancelButtonText"),
    FILE_CHOOSER_CANCEL_BUTTON_TOOLTIP_TEXT("FileChooser.cancelButtonToolTipText"),
    FILE_CHOOSER_DETAILS_VIEW_BUTTON_ACCESSIBLE_NAME("FileChooser.detailsViewButtonAccessibleName"),
    FILE_CHOOSER_DETAILS_VIEW_BUTTON_TOOLTIP_TEXT("FileChooser.detailsViewButtonToolTipText"),
    FILE_CHOOSER_DIRECTORY_DESCRIPTION_TEXT("FileChooser.directoryDescriptionText"),
    FILE_CHOOSER_FILE_DESCRIPTION_TEXT("FileChooser.fileDescriptionText"),
    FILE_CHOOSER_FILE_NAME_LABEL_TEXT("FileChooser.fileNameLabelText"),
    FILE_CHOOSER_FILES_OF_TYPE_LABEL_TEXT("FileChooser.filesOfTypeLabelText"),
    FILE_CHOOSER_HELP_BUTTON_TEXT("FileChooser.helpButtonText"),
    FILE_CHOOSER_HELP_BUTTON_TOOLTIP_TEXT("FileChooser.helpButtonToolTipText"),
    FILE_CHOOSER_HOME_FOLDER_ACCESSIBLE_NAME("FileChooser.homeFolderAccessibleName"),
    FILE_CHOOSER_HOME_FOLDER_TOOLTIP_TEXT("FileChooser.homeFolderToolTipText"),
    FILE_CHOOSER_LIST_VIEW_BUTTON_ACCESSIBLE_NAME("FileChooser.listViewButtonAccessibleName"),
    FILE_CHOOSER_LIST_VIEW_BUTTON_TOOLTIP_TEXT("FileChooser.listViewButtonToolTipText"),
    FILE_CHOOSER_LOOK_IN_LABEL_TEXT("FileChooser.lookInLabelText"),
    FILE_CHOOSER_NEW_FOLDER_ACCESSIBLE_NAME("FileChooser.newFolderAccessibleName"),
    FILE_CHOOSER_NEW_FOLDER_ERROR_TEXT("FileChooser.newFolderErrorText"),
    FILE_CHOOSER_NEW_FOLDER_TOOLTIP_TEXT("FileChooser.newFolderToolTipText"),
    FILE_CHOOSER_OPEN_BUTTON_TEXT("FileChooser.openButtonText"),
    FILE_CHOOSER_OPEN_BUTTON_TOOLTIP_TEXT("FileChooser.openButtonToolTipText"),
    FILE_CHOOSER_SAVE_BUTTON_TEXT("FileChooser.saveButtonText"),
    FILE_CHOOSER_SAVE_BUTTON_TOOLTIP_TEXT("FileChooser.saveButtonToolTipText"),
    FILE_CHOOSER_UPDATE_BUTTON_TEXT("FileChooser.updateButtonText"),
    FILE_CHOOSER_UPDATE_BUTTON_TOOLTIP_TEXT("FileChooser.updateButtonToolTipText"),
    FILE_CHOOSER_UP_FOLDER_ACCESSIBLE_NAME("FileChooser.upFolderAccessibleName"),
    FILE_CHOOSER_UP_FOLDER_TOOLTIP_TEXT("FileChooser.upFolderToolTipText"),
    ABSTRACT_BUTTON_CLICK_TEXT("AbstractButton.clickText"),
    ABSTRACT_DOCUMENT_ADDITION_TEXT("AbstractDocument.additionText"),
    ABSTRACT_DOCUMENT_DELETION_TEXT("AbstractDocument.deletionText"),
    ABSTRACT_DOCUMENT_REDO_TEXT("AbstractDocument.redoText"),
    ABSTRACT_DOCUMENT_STYLE_CHANGE_TEXT("AbstractDocument.styleChangeText"),
    ABSTRACT_DOCUMENT_UNDO_TEXT("AbstractDocument.undoText"),
    ABSTRACT_UNDOABLE_EDIT_REDO_TEXT("AbstractUndoableEdit.redoText"),
    ABSTRACT_UNDOABLE_EDIT_UNDO_TEXT("AbstractUndoableEdit.undoText"),
    FORM_VIEW_BROWSE_FILE_BUTTON_TEXT("FormView.browseFileButtonText"),
    FORM_VIEW_RESET_BUTTON_TEXT("FormView.resetButtonText"),
    FORM_VIEW_SUBMIT_BUTTON_TEXT("FormView.submitButtonText");
    
    private final String key;


    SystemText(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
