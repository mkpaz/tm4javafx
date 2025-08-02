/* SPDX-License-Identifier: MIT */

package tm4javafx.richtext;

import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import org.jspecify.annotations.Nullable;

/**
 * Represents a styled token (segment).
 *
 * @param text  the text to style
 * @param style the style information
 */
public record StyledToken(String text, @Nullable StyleAttributeMap style) {

    /**
     * An empty styled token (segment).
     */
    public static StyledToken EMPTY = new StyledToken("", null);
}
