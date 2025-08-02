/* SPDX-License-Identifier: MIT */

package tm4javafx.richtext;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.paint.Color;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import org.jspecify.annotations.Nullable;
import tm4java.theme.ITheme;
import tm4java.theme.StyleAttributes;

/**
 * Provides an interface to obtain Textmate theme settings.
 *
 * <p>Theme settings primarily include the default background, text color, and font style,
 * but there are also various editor options. It's better to see than to explain:
 *
 * <pre>{@code
 * "editor.background": "#FAFAFA",
 * "editor.findMatchHighlightBackground": "#526FFF33",
 * "editor.foreground": "#383A42",
 * "editor.lineHighlightBackground": "#383A420C",
 * "editor.selectionBackground": "#E5E5E6",
 * "editorCursor.foreground": "#526FFF",
 * "editorGroup.background": "#EAEAEB",
 * }</pre>
 *
 * <p>
 * There are two types of sources to obtain settings:
 *
 * <li>{@link ITheme#getDefaults()} returns the default background, text color, and
 * font style only, obtained from an element with an <b>empty selector</b> (scope).
 * This acts as a global selector that applies to all scopes. not every theme provides
 * such a selector. If none is provided, the background falls back to "#FFF" (white) and
 * the text color falls back to "#000" (black).
 *
 * <li>{@link ITheme#getEditorColors()} returns a map containing values from the "colors"
 * dictionary (VSCode themes only), or from an element with an <b>empty selector</b>
 * in that order. Unlike {@link ITheme#getDefaults()}, it returns all properties,
 * not just the background, text color, and font style. Not every theme provides editor
 * colors either, and the number of properties may vary.
 *
 * <p>
 * This class tries its best to obtain the defaults and provide some options to obtain other
 * settings.
 */
public class ThemeSettings {

    protected static final String BACKGROUND = "BACKGROUND";
    protected static final String FOREGROUND = "FOREGROUND";
    protected static final String SELECTION_HIGHLIGHT = "SELECTION_HIGHLIGHT";

    protected final List<String> colorMap;
    protected final StyleAttributes defaults;
    protected final Map<String, String> editorColors;
    protected final Map<String, @Nullable String> cache = new HashMap<>();
    protected @Nullable StyleAttributeMap mergedDefaults;

    protected ThemeSettings(List<String> colorMap,
                            StyleAttributes defaults,
                            Map<String, String> editorColors) {
        this.colorMap = colorMap;
        this.defaults = defaults;
        this.editorColors = editorColors;
    }

    /**
     * See {@link ITheme#getColorMap()}.
     */
    public List<String> getColorMap() {
        return colorMap;
    }

    /**
     * Returns the original theme defaults.
     * See {@link ITheme#getDefaults()} ()}.
     */
    public StyleAttributes getDefaults() {
        return defaults;
    }

    /**
     * Returns the theme defaults merged with the editor colors.
     * <p>
     * The editor colors have priority; in other words, to obtain
     * the default background color, the editor colors are checked first,
     * then the defaults.
     */
    public StyleAttributeMap getMergedDefaults() {
        if (mergedDefaults != null) {
            return mergedDefaults;
        }

        var styleBuilder = StyleAttributeMap.builder()
                               .setBackground(Color.web(getBackgroundColor()))
                               .setTextColor(Color.web(getForegroundColor()));

        if (defaults.fontStyle() > 0) {
            styleBuilder.setBold(defaults.isBold());
            styleBuilder.setItalic(defaults.isItalic());
            styleBuilder.setUnderline(defaults.isUnderline());
            styleBuilder.setStrikeThrough(defaults.isStrikethrough());
        }

        return styleBuilder.build();
    }

    /**
     * Returns theme editor colors.
     * See {@link ITheme#getDefaults()} ()}.
     */
    public Map<String, String> getEditorColors() {
        return editorColors;
    }

    /**
     * Returns the editor color (theme property) for the given key.
     * <p>
     * This method accepts multiple arguments because, depending on the theme source
     * (or version), different property names may be used with the same meaning.
     * For example, the background color can be either "background" or "editor.background".
     */
    public @Nullable String getEditorColor(String... keys) {
        for (var key : keys) {
            var value = editorColors.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * Returns the theme default background color.
     * <p>
     * This method tries its best by checking the editor colors first,
     * then the defaults, and finally falling back to "#FFF" (white).
     */
    public String getBackgroundColor() {
        String color = cache.get(BACKGROUND);
        if (color != null) {
            return color;
        }

        color = getEditorColor("background", "editor.background");

        if (color == null && defaults.backgroundId() > 0 && colorMap.size() > defaults.backgroundId()) {
            color = colorMap.get(defaults.backgroundId());
        }

        if (color == null) {
            color = "#FFFFFF";
        }

        cache.put(BACKGROUND, color);
        return color;
    }

    /**
     * Returns the theme default foreground (text) color.
     * <p>
     * This method tries its best by checking the editor colors first,
     * then the defaults, and finally falling back to "#000" (black).
     */
    public String getForegroundColor() {
        String color = cache.get(FOREGROUND);
        if (color != null) {
            return color;
        }

        color = getEditorColor("foreground", "editor.foreground");

        if (color == null && defaults.foregroundId() > 0 && colorMap.size() > defaults.foregroundId()) {
            color = colorMap.get(defaults.foregroundId());
        }

        if (color == null) {
            color = "#000000";
        }

        cache.put(FOREGROUND, color);
        return color;
    }

    /**
     * Returns the theme selection highlight color, if any.
     * <p>
     * This property can only be provided by the editor color settings.
     */
    public @Nullable String getSelectionBackgroundColor() {
        String color = cache.get(SELECTION_HIGHLIGHT);
        if (color != null) {
            return color;
        }

        color = getEditorColor("lineHighlight", "editor.lineHighlightBackground");
        cache.put(SELECTION_HIGHLIGHT, color);

        return color;
    }

    /**
     * Converts the theme-specific {@link StyleAttributes} to the rich text
     * specific {@link StyleAttributeMap}.
     */
    public StyleAttributeMap resolve(StyleAttributes attrs) {
        var defaults = getMergedDefaults();
        return StyleHelper.toStyleAttributeMap(attrs, colorMap, defaults.getBackground(), defaults.getTextColor());
    }

    //*************************************************************************

    /**
     * Obtains the theme settings from the specified theme instance.
     */
    public static ThemeSettings from(ITheme theme) {
        return new ThemeSettings(
            Collections.unmodifiableList(theme.getColorMap()),
            theme.getDefaults(),
            Collections.unmodifiableMap(theme.getEditorColors())
        );
    }
}
