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

package org.cosinus.swing.exec;

import java.io.File;
import java.util.Optional;

import static java.lang.String.format;

/**
 * Implementation of {@link ProcessExecutor} for Windows
 */
public class WindowsProcessExecutor implements ProcessExecutor {

    /**
     * The well-known SID of "Everyone".
     */
    private static final String EVERYONE_SID = "S-1-1-0";

    public static final String POWERSHELL_COMMAND = "powershell";
    public static final String POWERSHELL_NO_PROFILE = "-NoProfile";
    public static final String POWERSHELL_NON_INTERACTIVE = "-NonInteractive";
    public static final String POWERSHELL_ENCODED_COMMAND = "-EncodedCommand";

    /**
     * Defines the $everyone variable with the (localized) account name of "Everyone".
     */
    public static final String POWERSHELL_EVERYONE = format(
        "$everyone = (New-Object System.Security.Principal.SecurityIdentifier '%s')" +
            ".Translate([System.Security.Principal.NTAccount]).Value; ", EVERYONE_SID);

    /**
     * Lists the non-special SMB shares of a path (%s) as a JSON array of objects
     * with the fields: name, path, comment and allowOthersToEdit (Everyone has full access).
     */
    public static final String POWERSHELL_FIND_SHARES = "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; " +
        POWERSHELL_EVERYONE +
        "$shares = @(Get-SmbShare -Special $false -ErrorAction SilentlyContinue | " +
        "Where-Object { $_.Path -eq %s } | " +
        "ForEach-Object { [pscustomobject]@{ " +
        "name = $_.Name; " +
        "path = $_.Path; " +
        "comment = $_.Description; " +
        "allowOthersToEdit = [bool](Get-SmbShareAccess -Name $_.Name -ErrorAction SilentlyContinue | " +
        "Where-Object { $_.AccountName -eq $everyone -and " +
        "$_.AccessControlType -eq 'Allow' -and $_.AccessRight -eq 'Full' }) } }); " +
        "ConvertTo-Json -InputObject $shares -Compress";

    /**
     * Removes the SMB share with a given name (%s).
     */
    public static final String POWERSHELL_REMOVE_SHARE = "Remove-SmbShare -Name %s -Force";

    /**
     * Adds an SMB share with name, path, description (%s) and the access of Everyone
     * ({@link #POWERSHELL_FULL_ACCESS} or {@link #POWERSHELL_READ_ACCESS}).
     */
    public static final String POWERSHELL_ADD_SHARE = POWERSHELL_EVERYONE +
        "New-SmbShare -Name %s -Path %s -Description %s %s $everyone | Out-Null";

    public static final String POWERSHELL_FULL_ACCESS = "-FullAccess";
    public static final String POWERSHELL_READ_ACCESS = "-ReadAccess";

    @Override
    @SuppressWarnings("SpellCheckingInspection")
    public void executeFile(File file) {
        execute(false, file.getParentFile(), false,
            "rundll32", "url.dll,FileProtocolHandler", file.getName());
    }

    @Override
    public Optional<String> executeWithPrivilegesAndGetOutput(String... command) {
        //TODO
        return Optional.empty();
    }

    @Override
    public Optional<String> executePipelineWithPrivilegesAndGetOutput(String[]... commands) {
        return Optional.empty();
    }

    @Override
    public void executeWithPrivileges(String... command) {
    }

}
