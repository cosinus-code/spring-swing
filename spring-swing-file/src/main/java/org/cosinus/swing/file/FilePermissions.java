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

package org.cosinus.swing.file;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cosinus.swing.security.Permissions;

@Getter
@Builder
public class FilePermissions implements Permissions {

    @Setter
    private String ownerName;

    @Setter
    private String groupName;

    private String[] availableGroupNames;

    private boolean editable;

    private boolean ownerRead;

    private boolean ownerWrite;

    private boolean ownerExecute;

    private boolean groupRead;

    private boolean groupWrite;

    private boolean groupExecute;

    private boolean othersRead;

    private boolean othersWrite;

    private boolean othersExecute;

    private boolean setUserId;

    private boolean setGroupId;

    private boolean sticky;

    private String textView;

    private String numberView;

    public void setOwnerRead(boolean ownerRead) {
        this.ownerRead = ownerRead;
        updateViews();
    }

    public void setOthersWrite(boolean othersWrite) {
        this.othersWrite = othersWrite;
        updateViews();
    }

    public void setOwnerExecute(boolean ownerExecute) {
        this.ownerExecute = ownerExecute;
        updateViews();
    }

    public void setGroupRead(boolean groupRead) {
        this.groupRead = groupRead;
        updateViews();
    }

    public void setGroupWrite(boolean groupWrite) {
        this.groupWrite = groupWrite;
        updateViews();
    }

    public void setGroupExecute(boolean groupExecute) {
        this.groupExecute = groupExecute;
        updateViews();
    }

    public void setOthersRead(boolean othersRead) {
        this.othersRead = othersRead;
        updateViews();
    }

    public void setOwnerWrite(boolean ownerWrite) {
        this.ownerWrite = ownerWrite;
        updateViews();
    }

    public void setOthersExecute(boolean othersExecute) {
        this.othersExecute = othersExecute;
        updateViews();
    }

    public void setSetUserId(boolean setUserId) {
        this.setUserId = setUserId;
        updateViews();
    }

    public void setSetGroupId(boolean setGroupId) {
        this.setGroupId = setGroupId;
        updateViews();
    }

    public void setSticky(boolean sticky) {
        this.sticky = sticky;
        updateViews();
    }

    public FilePermissions updateViews() {
        this.textView = computeTextView();
        this.numberView = computeNumberView();
        return this;
    }

    public FilePermissions updateNumberViews() {
        this.numberView = computeNumberView();
        return this;
    }

    public String computeTextView() {
        return textView.charAt(0) +
            (ownerRead ? "r" : "-") +
            (ownerWrite ? "w" : "-") +
            (ownerExecute ? setUserId ? "s" : "x" : setUserId ? "S" : "-") +
            (groupRead ? "r" : "-") +
            (groupWrite ? "w" : "-") +
            (groupExecute ? setGroupId ? "s" : "x" : setGroupId ? "S" : "-") +
            (othersRead ? "r" : "-") +
            (othersWrite ? "w" : "-") +
            (othersExecute ? sticky ? "t" : "x" : sticky ? "T" : "-");
    }

    public String computeNumberView() {
        String numberView = "%d%d%d".formatted(ownerNumber(), groupNumber(), othersNumber());
        int stickyNumber = numberFormat(setUserId, setGroupId, sticky);
        return stickyNumber > 0 ? stickyNumber + numberView : numberView;
    }

    public int ownerNumber() {
        return numberFormat(ownerRead, ownerWrite, ownerExecute);
    }

    public int groupNumber() {
        return numberFormat(groupRead, groupWrite, groupExecute);
    }

    public int othersNumber() {
        return numberFormat(othersRead, othersWrite, othersExecute);
    }

    private int numberFormat(boolean read, boolean write, boolean execute) {
        return (read ? 4 : 0) + (write ? 2 : 0) + (execute ? 1 : 0);
    }
}
