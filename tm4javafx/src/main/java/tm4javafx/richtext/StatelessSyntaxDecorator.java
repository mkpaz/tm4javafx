/* SPDX-License-Identifier: MIT */

package tm4javafx.richtext;

import java.util.ArrayList;
import java.util.List;
import jfx.incubator.scene.control.richtext.CodeArea;
import jfx.incubator.scene.control.richtext.SyntaxDecorator;
import jfx.incubator.scene.control.richtext.TextPos;
import jfx.incubator.scene.control.richtext.model.CodeTextModel;
import jfx.incubator.scene.control.richtext.model.RichParagraph;
import jfx.incubator.scene.control.richtext.model.StyledTextModel;
import org.jspecify.annotations.Nullable;

/**
 * A {@link CodeArea} syntax decorator that doesn't store the tokenization
 * state, but recreates styled paragraphs each time model changes.
 */
public class StatelessSyntaxDecorator implements SyntaxDecorator, StyledModel {

    private List<RichParagraph> paragraphs = new ArrayList<>();
    private @Nullable StyleProvider styleProvider;

    public StatelessSyntaxDecorator() {
        this(null);
    }

    public StatelessSyntaxDecorator(@Nullable StyleProvider styleProvider) {
        this.styleProvider = styleProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable StyleProvider getStyleProvider() {
        return styleProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStyleProvider(@Nullable StyleProvider styleProvider) {
        this.styleProvider = styleProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RichParagraph createRichParagraph(CodeTextModel model, int index) {
        if (paragraphs.isEmpty() || index >= paragraphs.size()) {
            return RichParagraph.builder().build();
        }
        return paragraphs.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleChange(CodeTextModel model, TextPos start,
                             TextPos end, int charsTop, int linesAdded, int charsBottom) {
        if (styleProvider == null) {
            paragraphs = List.of();
            return;
        }
        String text = getPlainText(model);

        if (text.isEmpty()) {
            paragraphs = List.of();
            return;
        }

        paragraphs = createRichParagraphs(styleProvider, text);
    }

    /**
     * Refreshes the text and styles of the associated rich text control.
     */
    public void refresh(StyledTextModel model) {
        model.fireStyleChangeEvent(TextPos.ZERO, model.getDocumentEnd());
    }

    //*************************************************************************

    protected List<RichParagraph> createRichParagraphs(StyleProvider provider, String text) {
        String[] lines = text.split(LINE_SPLIT_PATTERN);
        var paragraphs = new ArrayList<RichParagraph>(lines.length);

        for (var line : lines) {
            var tokens = provider.tokenize(line);
            var paragraph = RichParagraph.builder();

            for (var token : tokens) {
                applyStyles(paragraph, token);
            }

            paragraphs.add(paragraph.build());
        }

        return paragraphs;
    }

    protected String getPlainText(CodeTextModel model) {
        var sb = new StringBuilder();
        boolean newLine = false;

        for (int i = 0; i < model.size(); i++) {
            if (newLine) {
                sb.append('\n');
            } else {
                newLine = true;
            }
            sb.append(model.getPlainText(i));
        }

        return sb.toString();
    }

    protected void applyStyles(RichParagraph.Builder paragraphBuilder, StyledToken token) {
        if (token.style() == null) {
            paragraphBuilder.addSegment(token.text());
            return;
        }
        paragraphBuilder.addSegment(token.text(), token.style());
    }
}
