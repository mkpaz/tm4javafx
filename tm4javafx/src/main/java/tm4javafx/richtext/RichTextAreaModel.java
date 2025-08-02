/* SPDX-License-Identifier: MIT */

package tm4javafx.richtext;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import jfx.incubator.scene.control.richtext.RichTextArea;
import jfx.incubator.scene.control.richtext.model.SimpleViewOnlyStyledModel;
import jfx.incubator.scene.control.richtext.model.StyledTextModel;
import org.jspecify.annotations.Nullable;

/**
 * The model that automatically updates the {@code RichTextArea} content upon changes
 * to its {@link #textProperty()}. The style information is obtained from the
 * {@link StyleProvider} associated with the model.
 * <p>
 * This model uses JavaFX {@link SimpleViewOnlyStyledModel}, making the resulting text area
 * read-only as well.
 */
public class RichTextAreaModel extends RichTextModel {

    /**
     * Creates a new {@code RichTextArea} model.
     */
    public RichTextAreaModel() {
        init();
    }

    protected void init() {
        textProperty().subscribe(this::onTextContentChanged);
        richTextAreaProperty().subscribe(this::onRichTextAreaChanged);
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

    protected final ObjectProperty<@Nullable StyledTextModel> styledTextModel = new SimpleObjectProperty<>();
    protected final ObjectProperty<@Nullable RichTextArea> richTextArea = new SimpleObjectProperty<>();

    /**
     * Contains a {@code RichTextArea} associated with the model.
     */
    public ObjectProperty<@Nullable RichTextArea> richTextAreaProperty() {
        return richTextArea;
    }

    /**
     * Returns the {@code RichTextArea} associated with the model.
     */
    public @Nullable RichTextArea getRichTextArea() {
        return richTextAreaProperty().get();
    }

    /**
     * Sets the {@code RichTextArea} associated with the model.
     */
    public void setRichTextArea(@Nullable RichTextArea richTextArea) {
        richTextAreaProperty().set(richTextArea);
    }

    //*************************************************************************

    protected void onTextContentChanged() {
        var content = text.get();
        var provider = styleProvider.get();

        if (content == null || provider == null) {
            styledTextModel.set(null);
            return;
        }

        var model = new SimpleViewOnlyStyledModel();
        for (var line : content.split(LINE_SPLIT_PATTERN)) {
            var tokens = provider.tokenize(line);

            for (var token : tokens) {
                applyStyles(model, token);
            }

            model.nl();
        }

        styledTextModel.set(model);
    }

    protected void onRichTextAreaChanged(@Nullable RichTextArea old, @Nullable RichTextArea val) {
        if (old != null) {
            old.modelProperty().unbind();
        }
        if (val != null) {
            val.modelProperty().bind(styledTextModel);
        }
    }

    protected void applyStyles(SimpleViewOnlyStyledModel model, StyledToken token) {
        if (token.style() == null) {
            model.addSegment(token.text());
            return;
        }

        model.addSegment(token.text(), token.style());
    }
}
