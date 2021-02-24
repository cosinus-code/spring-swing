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

package org.cosinus.swing.image.icon;

import org.springframework.cache.interceptor.SimpleKeyGenerator;

import java.io.File;
import java.lang.reflect.Method;

import static java.util.Arrays.stream;
import static org.cosinus.swing.util.FileUtils.getExtension;

public class FileExtensionKeyGenerator extends SimpleKeyGenerator {

    private static final String FOLDER_ICON_KEY = ":folder:";

    @Override
    public Object generate(Object target, Method method, Object... params) {
        return super.generate(target, method, stream(params)
            .map(param -> param instanceof File ? getFileKey((File) param) : param)
            .toArray());
    }

    private String getFileKey(File file) {
        return file.isDirectory() ? FOLDER_ICON_KEY : getExtension(file);
    }

}
