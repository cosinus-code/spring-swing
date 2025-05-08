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

package org.cosinus.swing.error;

import java.io.Serial;

import static java.lang.String.format;

public class ProcessExecutionException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public static final int PERMISSION_DENIED = 126;

    public static final String EXIT_CODE_MESSAGE = "Exit code %d (%s)";

    public static final String ERROR_MESSAGE = EXIT_CODE_MESSAGE + ": %s";

    private int processExitCode;

    private String output;

    public ProcessExecutionException(int processExitCode) {
        this.processExitCode = processExitCode;
    }

    public ProcessExecutionException(int processExitCode, String output) {
        super(format(EXIT_CODE_MESSAGE, processExitCode, output));
        this.processExitCode = processExitCode;
        this.output = output;
    }

    public ProcessExecutionException(int processExitCode, String output, String message) {
        super(format(ERROR_MESSAGE, processExitCode, output, message));
        this.processExitCode = processExitCode;
        this.output = output;
    }

    public ProcessExecutionException(String message) {
        super(message);
    }

    public ProcessExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProcessExecutionException(Throwable cause) {
        super(cause);
    }

    public int getProcessExitCode() {
        return processExitCode;
    }

    public String getOutput() {
        return output;
    }
}
