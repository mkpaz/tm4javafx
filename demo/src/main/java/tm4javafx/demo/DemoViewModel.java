/* SPDX-License-Identifier: MIT */

package tm4javafx.demo;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import jfx.incubator.scene.control.richtext.CodeArea;
import jfx.incubator.scene.control.richtext.RichTextArea;
import tm4javafx.richtext.RichTextAreaModel;
import tm4javafx.richtext.StatelessSyntaxDecorator;
import tm4javafx.richtext.StyleHelper;
import tm4javafx.richtext.StyleProvider;
import tm4javafx.richtext.TextFlowModel;
import org.jspecify.annotations.Nullable;

public class DemoViewModel {

    static final Path GRAMMARS_DIR = Resources.getDirectory("/tm4javafx/demo/grammars");
    static final Path THEMES_DIR = Resources.getDirectory("/tm4javafx/demo/themes");
    static final String DEFAULT_GRAMMAR = "java";
    static final String DEFAULT_THEME = "one-dark-pro";

    // model
    private final StyleProvider styleProvider = new StyleProvider();
    private final TextFlowModel textFlowModel = new TextFlowModel();
    private final RichTextAreaModel richTextAreaModel = new RichTextAreaModel();

    // properties
    final ObservableList<GrammarLink> grammars = FXCollections.observableArrayList();
    final ObjectProperty<@Nullable GrammarLink> selectedGrammar = new SimpleObjectProperty<>();
    final ObservableList<ThemeLink> themes = FXCollections.observableArrayList();
    final ObjectProperty<@Nullable ThemeLink> selectedTheme = new SimpleObjectProperty<>();
    final ObservableList<ControlType> controlsTypes = FXCollections.observableArrayList();
    final ObjectProperty<@Nullable ControlType> selectedControlType = new SimpleObjectProperty<>();
    final ObjectProperty<@Nullable Node> richTextControl = new SimpleObjectProperty<>();

    public DemoViewModel() {
        grammars.setAll(findGrammars());
        themes.setAll(findThemes());
        controlsTypes.setAll(ControlType.values());

        init();
    }

    public void init() {
        textFlowModel.setStyleProvider(styleProvider);
        richTextAreaModel.setStyleProvider(styleProvider);

        selectedGrammar.subscribe(link -> {
            if (link != null) {
                styleProvider.setGrammar(link.getGrammarSource());
            }
            updateExampleText();
        });

        selectedTheme.subscribe(link -> {
            if (link != null) {
                try {
                    styleProvider.setTheme(link.getThemeSource());
                    refreshRichTextControl();
                    // wouldn't be needed, but firing fireStyleChangeEvent()
                    // in decorator for some reason doesn't update CodeArea correctly
                    updateExampleText();
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        selectedControlType.subscribe((_, val) -> {
            switch (val) {
                case TEXT_FLOW -> {
                    var stf = new ScrollableTextFlow();
                    textFlowModel.setTextFlow(stf.getTextFlow());
                    richTextControl.set(stf);
                }
                case RICH_TEXT -> {
                    var rta = new RichTextArea();
                    richTextAreaModel.setRichTextArea(rta);
                    richTextControl.set(rta);
                }
                case CODE_AREA -> {
                    var ca = new CodeArea();
                    ca.setSyntaxDecorator(new StatelessSyntaxDecorator(styleProvider));
                    ca.setLineNumbersEnabled(true);
                    ca.setHighlightCurrentParagraph(true);
                    richTextControl.set(ca);
                }
                case null -> {
                    richTextControl.set(null);
                    textFlowModel.setTextFlow(null);
                    richTextAreaModel.setRichTextArea(null);
                }
            }

            refreshRichTextControl();
            updateExampleText();
        });
    }

    //*************************************************************************

    private void updateExampleText() {
        var link = selectedGrammar.get();
        if (link == null) {
            return;
        }

        var text = link.getExampleText("Loading code example failed '" + link.examplePath() + "'");

        switch (richTextControl.get()) {
            case ScrollableTextFlow _ -> textFlowModel.setText(text);
            case CodeArea ca -> ca.setText(text);
            case RichTextArea _ -> richTextAreaModel.setText(text);
            case null, default -> {
            }
        }
    }

    private void refreshRichTextControl() {
        switch (richTextControl.get()) {
            case ScrollableTextFlow stf -> {
                textFlowModel.refresh();
                StyleHelper.applyThemeSettings(stf.getTextFlow(), styleProvider.getThemeSettings());
            }
            case CodeArea ca -> {
                if (ca.getSyntaxDecorator() instanceof StatelessSyntaxDecorator d) {
                    d.refresh(ca.getModel());
                }
                StyleHelper.applyThemeSettings(ca, styleProvider.getThemeSettings());
            }
            case RichTextArea rta -> {
                richTextAreaModel.refresh();
                StyleHelper.applyThemeSettings(rta, styleProvider.getThemeSettings());
            }
            case null, default -> {
            }
        }
    }

    private List<GrammarLink> findGrammars() {
        var grammars = new ArrayList<GrammarLink>();
        var languages = new TreeSet<GrammarLink.Sample>();
        var samples = new HashMap<String, GrammarLink.Sample>();

        try {
            Files.walkFileTree(GRAMMARS_DIR, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    var sample = GrammarLink.Sample.of(file);
                    if (sample.isGrammar() || sample.isExample()) {
                        languages.add(sample);
                        samples.put(sample.id(), sample);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        for (var sample : languages) {
            var grammar = samples.get(sample.grammarId());
            var example = samples.get(sample.exampleId());
            if (grammar != null && example != null) {
                grammars.add(new GrammarLink(
                    sample.language().toUpperCase(),
                    grammar.path(),
                    example.path()
                ));
            }
        }

        Collections.sort(grammars);

        return grammars;
    }

    private List<ThemeLink> findThemes() {
        var themes = new ArrayList<ThemeLink>();

        try {
            Files.walkFileTree(THEMES_DIR, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    try {
                        themes.add(ThemeLink.of(file));
                    } catch (IllegalArgumentException _) {
                        System.err.println("Skipping invalid theme: " + file);
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        Collections.sort(themes);

        return themes;
    }
}
