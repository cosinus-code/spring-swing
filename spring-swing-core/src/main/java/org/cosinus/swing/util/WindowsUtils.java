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
 */

package org.cosinus.swing.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

/**
 * Windows native utils
 */
public class WindowsUtils {
    private static final Logger LOG = LogManager.getLogger(WindowsUtils.class);

    private static final String REG_QUERY = "reg query ";
    private static final String REG_STR_TOKEN = "REG_SZ";
    private static final String REG_EXP_TOKEN = "REG_EXPAND_SZ";
    private static final String REG_DWORD = "REG_DWORD";

    /**
     * Get a Windows registry value
     *
     * @param register the register
     * @return the value from registry
     */
    public static Optional<String> getRegistryValue(String register) {
        return getRegistryValue(register, null);
    }

    /**
     * Get a Windows registry value for a give key
     *
     * @param register the register
     * @param key      the key
     * @return the value from registry, or Optional.empty
     */
    public static Optional<String> getRegistryValue(String register, String key) {
        String exec = REG_QUERY + "\"" + register + "\" " + (key == null ? "/ve" : "/v " + key);
        try (BufferedReader buff = new BufferedReader(new InputStreamReader(Runtime.getRuntime()
                                                                                    .exec(exec)
                                                                                    .getInputStream()))) {
            String line;
            while (null != (line = buff.readLine())) {
                if (line.contains(REG_EXP_TOKEN) || line.contains(REG_STR_TOKEN) || line.contains(REG_DWORD)) {
                    return Optional.ofNullable(line.split("\\s+"))
                            .filter(values -> values.length > 0)
                            .map(values -> values[values.length - 1]);
                }
            }
        } catch (IOException ex) {
            LOG.error("Failed to execute command: " + exec, ex);
        }
        return Optional.empty();
    }

    public static boolean getRegistryBooleanValue(String register, String key) {
        return getRegistryValue(register, key)
                .map(Integer::decode)
                .map(value -> value == 1)
                .orElse(false);
    }
}
