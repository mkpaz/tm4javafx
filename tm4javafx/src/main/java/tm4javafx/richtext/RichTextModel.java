/* SPDX-License-Identifier: MIT */

package tm4javafx.richtext;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.jspecify.annotations.Nullable;

/**
 * A base text-associated model template for rich text controls powered
 * by TextMate grammars.
 */
public abstract class RichTextModel implements StyledModel {

    /**
     * Contains the style provider associated with the model.
     */
    public ObjectProperty<@Nullable StyleProvider> styleProviderProperty() {
        return styleProvider;
    }

    protected final ObjectProperty<@Nullable StyleProvider> styleProvider = new SimpleObjectProperty<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable StyleProvider getStyleProvider() {
        return styleProvider.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStyleProvider(@Nullable StyleProvider styleProvider) {
        styleProviderProperty().set(styleProvider);
    }

    /**
     * Contains the plain, unstyled text associated with the styled model.
     * <p>
     * Changing the text causes model changes and updates its state, which
     * is reflected in the bound UI control.
     */
    public final StringProperty textProperty() {
        return text;
    }

    protected final StringProperty text = new SimpleStringProperty();

    /**
     * Returns the plain, unstyled text associated with the styled model.
     */
    public final String getText() {
        return textProperty().get();
    }

    /**
     * Sere {@link #textProperty()}.
     */
    public void setText(String text) {
        textProperty().set(text);
    }
}
