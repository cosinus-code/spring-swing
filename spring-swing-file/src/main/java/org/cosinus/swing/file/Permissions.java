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

@Getter
@Builder
public class Permissions {

    private String ownerName;

    @Setter
    private String groupName;

    private String[] availableGroupNames;

    @Setter
    private boolean ownerRead;

    @Setter
    private boolean ownerWrite;

    @Setter
    private boolean ownerExecute;

    @Setter
    private boolean groupRead;

    @Setter
    private boolean groupWrite;

    @Setter
    private boolean groupExecute;

    @Setter
    private boolean othersRead;

    @Setter
    private boolean othersWrite;

    @Setter
    private boolean othersExecute;

    @Setter
    private boolean setUserId;

    @Setter
    private boolean setGroupId;

    @Setter
    private boolean sticky;

    private String textView;

    public String numberView() {
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
