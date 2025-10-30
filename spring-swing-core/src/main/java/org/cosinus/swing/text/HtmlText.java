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

package org.cosinus.swing.text;

import java.util.List;

import static java.lang.String.join;

public interface HtmlText {

    String TAG_HTML_START = "<html>";
    String TAG_HTML_CLOSE = "</html>";
    String TAG_CENTER_START = "<center>";
    String TAG_CENTER_CLOSE = "</center>";
    String TAG_PARAGRAPH = "<p>";
    String TAG_BOLD_START = "<b>";
    String TAG_BOLD_CLOSE = "</b>";

    String getHtml();

    /**
     * Get the text as html.
     *
     * @return the text as HTML
     */
    default String htmlText(String text) {
        return TAG_HTML_START + text + TAG_HTML_CLOSE;
    }

    /**
     * Get the text as centered HTML.
     *
     * @return the text as centered HTML
     */
    default String centeredText(String text) {
        return TAG_CENTER_START + text + TAG_CENTER_CLOSE;
    }

    /**
     * Get the text as bold HTML.
     *
     * @return the text as bold HTML
     */
    default String boldText(String text) {
        return TAG_BOLD_START + text + TAG_BOLD_CLOSE;
    }

    /**
     * Get the wrapped text.
     *
     * @return the wrapped text as HTML body
     */
    default String wrappedHtml(List<String> text) {
        return join(TAG_PARAGRAPH, text.toArray(new String[0]));
    }

    /**
     * Get the wrapped text.
     *
     * @return the wrapped text as HTML body
     */
    default String wrappedHtml(String... text) {
        return join(TAG_PARAGRAPH, text);
    }

}
