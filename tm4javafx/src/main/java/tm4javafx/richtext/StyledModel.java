/* SPDX-License-Identifier: MIT */

package tm4javafx.richtext;

import org.jspecify.annotations.Nullable;

/**
 * Common interface for implementations that work with the {@link StyleProvider}.
 */
public interface StyledModel {

    /**
     * The regex pattern for cross-platform splitting of text into lines.
     */
    String LINE_SPLIT_PATTERN = "\\r?\\n|\\r";

    /**
     * Returns the style provider used to obtain styled text content.
     */
    @Nullable
    StyleProvider getStyleProvider();

    /**
     * Sets the style provider for obtaining styled text content.
     */
    void setStyleProvider(@Nullable StyleProvider styleProvider);
}
