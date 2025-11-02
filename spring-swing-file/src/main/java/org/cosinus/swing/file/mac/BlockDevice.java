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

package org.cosinus.swing.file.mac;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
public class BlockDevice {

    private String uuid;

    private String path;

    private String label;

    private String type;

    private long size;

    @JsonProperty("rm")
    private boolean removableDevice;

    @JsonProperty("hotplug")
    private boolean plugAndPlayDevice;

    @JsonProperty("rota")
    private boolean rotationalDevice;

    @JsonProperty("fstype")
    private String fileSystemType;

    @JsonProperty("mountpoint")
    private String mountPoint;

    private String vendor;

    private List<BlockDevice> children = new ArrayList<>();
}
