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

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.mac.CoreFoundation.CFStringRef;

/**
 * The part of the macOS Launch Services (in the "CoreServices" framework) not mapped by JNA platform.
 * <p>
 * The library is loaded on first use, so this interface can be referenced on any platform.
 */
interface MacLaunchServices extends Library {

    MacLaunchServices INSTANCE = Native.load("CoreServices", MacLaunchServices.class);

    /**
     * The status of a successful Launch Services call.
     */
    int NO_ERROR = 0;

    /**
     * All the roles of an application for a content type: viewer, editor, shell.
     */
    int K_LS_ROLES_ALL = 0xFFFFFFFF;

    /**
     * Set the default application of a content type.
     * <p>
     * Deprecated since macOS 12, but still working; its replacement, NSWorkspace, is Objective-C only.
     *
     * @param contentType     the uniform type identifier, e.g. "public.plain-text"
     * @param role            the roles, e.g. {@link #K_LS_ROLES_ALL}
     * @param handlerBundleId the bundle id of the application, e.g. "com.apple.TextEdit"
     * @return {@link #NO_ERROR} if successful, an error status otherwise
     */
    int LSSetDefaultRoleHandlerForContentType(CFStringRef contentType, int role, CFStringRef handlerBundleId);
}
