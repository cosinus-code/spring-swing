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

import com.sun.jna.platform.mac.CoreFoundation.CFStringRef;
import lombok.extern.slf4j.Slf4j;

import static com.sun.jna.platform.mac.CoreFoundation.CFStringRef.createCFString;
import static org.cosinus.swing.file.mac.MacLaunchServices.K_LS_ROLES_ALL;
import static org.cosinus.swing.file.mac.MacLaunchServices.NO_ERROR;

/**
 * {@link MacApplicationHandler} using the macOS Launch Services through JNA.
 */
@Slf4j
class MacLaunchServicesApplicationHandler implements MacApplicationHandler {

    @Override
    public boolean setDefaultApplication(final String uniformTypeIdentifier, final String bundleId) {
        CFStringRef contentType = null;
        CFStringRef handler = null;
        try {
            contentType = createCFString(uniformTypeIdentifier);
            handler = createCFString(bundleId);
            int status =
                MacLaunchServices.INSTANCE.LSSetDefaultRoleHandlerForContentType(contentType, K_LS_ROLES_ALL, handler);
            if (status != NO_ERROR) {
                log.warn("Cannot set {} as default application of {}: status {}",
                    bundleId, uniformTypeIdentifier, status);
            }
            return status == NO_ERROR;
        } catch (RuntimeException | LinkageError ex) {
            log.warn("Cannot set {} as default application of {}", bundleId, uniformTypeIdentifier, ex);
            return false;
        } finally {
            release(contentType);
            release(handler);
        }
    }

    private void release(final CFStringRef string) {
        if (string != null) {
            string.release();
        }
    }
}
