/* SPDX-License-Identifier: MIT */

package tm4javafx.richtext;

import java.util.ArrayList;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import org.jspecify.annotations.Nullable;

/**
 * The model that automatically updates the {@code TextFlow} content upon changes
 * to its {@link #textProperty()}. The style information is obtained from the
 * {@link StyleProvider} associated with the model.
 */
public class TextFlowModel extends RichTextModel {

    /**
     * Creates a new {@code TextFlow} model.
     */
    public TextFlowModel() {
        init();
    }

    protected void init() {
        textProperty().subscribe(val -> onTextContentChanged());
        styleProviderProperty().subscribe(this::onTextContentChanged);
        textFlowProperty().subscribe(this::onTextFlowChanged);
    }

    /**
     * Refreshes the text and styles of the associated rich text control.
     */
    public void refresh() {
        onTextContentChanged();
    }

    //*************************************************************************
    // Properties
    //*************************************************************************

    protected final ObservableList<Text> styledText = FXCollections.observableArrayList();
    protected final ObservableList<Text> readOnlyStyledText = FXCollections.unmodifiableObservableList(styledText);

    /**
     * Returns a read-only list containing the styled {@code Text} nodes.
     */
    public final ObservableList<Text> getStyledText() {
        return readOnlyStyledText;
    }

    // ~

    /**
     * Contains a {@code TextFlow} associated with the model.
     */
    public ObjectProperty<@Nullable TextFlow> textFlowProperty() {
        return textFlow;
    }

    protected final ObjectProperty<@Nullable TextFlow> textFlow = new SimpleObjectProperty<>();

    /**
     * Returns the {@code TextFlow} associated with the model.
     */
    public @Nullable TextFlow getTextFlow() {
        return textFlowProperty().get();
    }

    /**
     * Sets the {@code TextFlow} associated with the model.
     */
    public void setTextFlow(@Nullable TextFlow textFlow) {
        textFlowProperty().set(textFlow);
    }

    //*************************************************************************

    protected void onTextContentChanged() {
        var content = text.get();
        var provider = styleProvider.get();

        if (content == null || provider == null) {
            getStyledText().clear();
            return;
        }

        var styledTextNodes = new ArrayList<Text>();
        for (var line : content.split(LINE_SPLIT_PATTERN)) {
            var tokens = provider.tokenize(line);

            for (var token : tokens) {
                var textNode = new Text(token.text());
                applyStyles(textNode, token.style());
                styledTextNodes.add(textNode);
            }

            if (!styledTextNodes.isEmpty()) {
                var last = styledTextNodes.getLast();
                last.setText(last.getText() + "\n");
            }
        }

        styledText.setAll(styledTextNodes);
    }

    protected void onTextFlowChanged(@Nullable TextFlow old, @Nullable TextFlow val) {
        if (old != null) {
            Bindings.unbindContent(old.getChildren(), readOnlyStyledText);
        }
        if (val != null) {
            Bindings.bindContent(val.getChildren(), readOnlyStyledText);
        }
    }

    protected void applyStyles(Text textNode, @Nullable StyleAttributeMap style) {
        if (style == null) {
            return;
        }

        if (style.getTextColor() != null) {
            textNode.setFill(style.getTextColor());
        }
        if (style.isBold()) {
            StyleHelper.addOrReplaceStyle(textNode, "-fx-font-weight", "bold");
        }
        if (style.isItalic()) {
            StyleHelper.addOrReplaceStyle(textNode, "-fx-font-style", "italic");
        }
        if (style.isUnderline()) {
            textNode.setUnderline(true);
        }
        if (style.isStrikeThrough()) {
            textNode.setStrikethrough(true);
        }
    }
}
