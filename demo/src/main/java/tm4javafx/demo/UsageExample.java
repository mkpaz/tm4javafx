/*
 * Copyright © 2025 tm4java authors
 * Original authors (EPL-2.0): Sebastian Thomschke, Angelo Zerr (tm4e).
 * Initial code (MIT): Microsoft Corporation (vscode-textmate).
 *
 * This program is licensed under the Eclipse Public License 2.0 (EPL-2.0).
 * See https://www.eclipse.org/legal/epl-2.0/ for details.
 */

package tm4javafx.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import jfx.incubator.scene.control.richtext.CodeArea;
import jfx.incubator.scene.control.richtext.RichTextArea;
import tm4javafx.richtext.RichTextAreaModel;
import tm4javafx.richtext.StatelessSyntaxDecorator;
import tm4javafx.richtext.StyleHelper;
import tm4javafx.richtext.StyleProvider;
import tm4javafx.richtext.TextFlowModel;
import tm4java.grammar.IGrammarSource;
import tm4java.theme.IThemeSource;

public class UsageExample extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        var styleProvider = new StyleProvider();
        styleProvider.setGrammar(IGrammarSource.fromFile(
            Resources.getFile("/tm4javafx/demo/grammars/java.tmLanguage.json")
        ));
        styleProvider.setTheme(IThemeSource.fromFile(
            Resources.getFile("/tm4javafx/demo/themes/one-dark-pro.json")
        ));

        final String sampleText = """
            public static void main(String[] args) {
                System.out.println("Hello World!");
            }
            """;

        // ~
        var textFlow = new TextFlow();
        textFlow.setPrefHeight(200);

        var textFlowModel = new TextFlowModel();
        textFlowModel.setTextFlow(textFlow);
        textFlowModel.setStyleProvider(styleProvider);
        textFlowModel.setText(sampleText);

        // ~
        var richTextArea = new RichTextArea();
        richTextArea.setPrefHeight(200);

        var richTextAreaModel = new RichTextAreaModel();
        richTextAreaModel.setRichTextArea(richTextArea);
        richTextAreaModel.setStyleProvider(styleProvider);
        richTextAreaModel.setText(sampleText);

        // ~
        var syntaxDecorator = new StatelessSyntaxDecorator();
        syntaxDecorator.setStyleProvider(styleProvider);

        var codeArea = new CodeArea();
        codeArea.setPrefHeight(200);
        codeArea.setSyntaxDecorator(syntaxDecorator);
        codeArea.setText(sampleText);

        // ~
        var root = new VBox(0, textFlow, richTextArea, codeArea);
        var scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setOnShown(_ -> {
            StyleHelper.applyThemeSettings(textFlow, styleProvider.getThemeSettings());
            StyleHelper.applyThemeSettings(richTextArea, styleProvider.getThemeSettings());
            StyleHelper.applyThemeSettings(codeArea, styleProvider.getThemeSettings());
        });
        stage.show();
    }
}
