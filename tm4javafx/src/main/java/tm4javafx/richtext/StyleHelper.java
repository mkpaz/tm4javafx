/* SPDX-License-Identifier: MIT */

package tm4javafx.richtext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;
import jfx.incubator.scene.control.richtext.RichTextArea;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import org.jspecify.annotations.Nullable;
import tm4java.theme.StyleAttributes;

/**
 * Utility class for common operations with styled attributes.
 */
public class StyleHelper {

    public StyleHelper() {
        // utility class
    }

    /**
     * Converts {@link StyleAttributes} to {@link StyleAttributeMap}.
     *
     * @param attrs             the styled attributes
     * @param colorMap          the theme color map
     * @param defaultBackground the default background if {@code StyleAttributes} contains none
     * @param defaultForeground the default text color if {@code StyleAttributes} contains none
     */
    public static StyleAttributeMap toStyleAttributeMap(StyleAttributes attrs,
                                                        List<String> colorMap,
                                                        @Nullable Color defaultBackground,
                                                        @Nullable Color defaultForeground) {
        var styleBuilder = StyleAttributeMap.builder();

        if (attrs.backgroundId() > 0 && colorMap.size() > attrs.backgroundId()) {
            styleBuilder.setBackground(Color.web(colorMap.get(attrs.backgroundId())));
        } else if (defaultBackground != null) {
            styleBuilder.setTextColor(defaultBackground);
        }

        if (attrs.foregroundId() > 0 && colorMap.size() > attrs.foregroundId()) {
            styleBuilder.setTextColor(Color.web(colorMap.get(attrs.foregroundId())));
        } else if (defaultForeground != null) {
            styleBuilder.setTextColor(defaultForeground);
        }

        if (attrs.fontStyle() > 0) {
            styleBuilder.setBold(attrs.isBold());
            styleBuilder.setItalic(attrs.isItalic());
            styleBuilder.setUnderline(attrs.isUnderline());
            styleBuilder.setStrikeThrough(attrs.isStrikethrough());
        }

        return styleBuilder.build();
    }

    /**
     * Applies the given theme settings to the specified {@code TextFlow}.
     */
    public static void applyThemeSettings(TextFlow textFlow, @Nullable ThemeSettings settings) {
        if (settings == null) {
            return;
        }
        addOrReplaceStyle(textFlow, "-fx-background-color", settings.getBackgroundColor());
    }

    /**
     * Applies the given theme settings to the specified {@code RichTextArea}.
     */
    public static void applyThemeSettings(RichTextArea textArea, @Nullable ThemeSettings settings) {
        if (settings == null) {
            return;
        }

        var rules = new ArrayList<String>();

        rules.add("""
            .rich-text-area .content,
            .code-area .left-side {
                -fx-background-color: %s;
            }""".formatted(settings.getBackgroundColor()));

        rules.add("""
            .rich-text-area .left-side .label {
                -fx-text-fill: %s;
            }""".formatted(settings.getForegroundColor()));


        String lineHighlight = settings.getSelectionBackgroundColor();
        if (lineHighlight != null) {
            rules.add("""
                .rich-text-area .content .selection-highlight,
                .rich-text-area .content .caret-line {
                   -fx-fill: %s;
                }
                """.formatted(lineHighlight));
        }

        textArea.getStylesheets().removeIf(s -> s.startsWith("data:text/css,"));
        textArea.getStylesheets().add(
            "data:text/css," + String.join("\n", rules)
        );
    }

    /**
     * Adds or replaces the CSS style declaration for the given node.
     */
    public static void addOrReplaceStyle(Node node, String property, String value) {
        var styles = parseStyle(node.getStyle());
        styles.put(property, value);
        node.setStyle(toStyleString(styles));
    }

    //*************************************************************************

    private static Map<String, String> parseStyle(@Nullable String style) {
        var styleMap = new TreeMap<String, String>();

        if (style == null || style.isEmpty()) {
            return styleMap;
        }

        // TODO: Replace with more robust CSS parsing
        String[] declarations = style.split(";");
        for (var declaration : declarations) {
            String[] propertyValue = declaration.trim().split(":");
            if (propertyValue.length == 2) {
                var property = propertyValue[0].trim();
                var value = propertyValue[1].trim();
                styleMap.put(property, value);
            }
        }

        return styleMap;
    }

    private static String toStyleString(Map<String, String> declarations) {
        StringBuilder sb = new StringBuilder();

        for (var entry : declarations.entrySet()) {
            sb.append(entry.getKey())
                .append(":")
                .append(entry.getValue())
                .append(";");
        }

        return sb.toString();
    }
}
