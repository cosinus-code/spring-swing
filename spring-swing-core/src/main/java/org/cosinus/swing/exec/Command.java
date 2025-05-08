/*
 *
 *  * Copyright 2024 Cosinus Software
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package org.cosinus.swing.exec;

import java.util.stream.Stream;

import static java.lang.ProcessBuilder.Redirect.INHERIT;
import static java.lang.ProcessBuilder.Redirect.PIPE;
import static java.util.Arrays.stream;
import static java.util.stream.Stream.concat;

public class Command {

    public static String[] of(String... command) {
        return command;
    }

    public static String[][] commands(String[]... commands) {
        return commands;
    }

    public static String[] prefixed(String prefix, String... command) {
        return concat(Stream.of(prefix), stream(command))
            .toArray(String[]::new);
    }

    public static ProcessBuilder pipeProcess(String... command) {
        return new ProcessBuilder(command)
            .inheritIO()
            .redirectOutput(PIPE);
    }

    public static ProcessBuilder outputProcess(String... command) {
        return new ProcessBuilder(command)
            .redirectError(INHERIT);
    }
}
