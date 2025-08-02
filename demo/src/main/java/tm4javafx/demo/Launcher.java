/* SPDX-License-Identifier: MIT */

package tm4javafx.demo;

import java.io.IOException;
import java.util.logging.LogManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Launcher extends Application {

    static {
        setupLogger();
    }

    public static void main(String[] args) {
        launch(args);
    }

    /*
     * Issues:
     * - Cursor doesn't change from TEXT to DEFAULT when hover over scrollbar.
     * - Setting font-family not working in CoreArea (but works in RichTextArea).
     * - Jerking scrolling with mouse drag on scrollbar (but smooth with mouse wheel), both controls.
     * - RichParagraph.getSegments() is abstract and package-private and a part of public API,
     *   which doesn't make sense as no one can extend from it with package-private abstract method.
     *
     * Enhancements:
     * - Implement add(StyledSegment) in RichParagraph.Builder and *StyledModel, so StyledSegment could
     *   be used as DTO without intermediate objects (see StyledToken) that serve the same purpose.
     * - Expose -fx-editor-background to change default background color for content and line numbers.
     * - Expose -fx-text-fill to change default text color for line numbers.
     * - Expose -fx-highlight-fill to change selection color.
     */
    @Override
    public void start(Stage stage) {
        var view = new DemoView(new DemoViewModel());
        var scene = new Scene(view, 1024, 768);

        scene.getStylesheets().add(Resources.getResource("/tm4javafx/demo/assets/fonts.css").toString());
        // code area rules not working
        scene.getStylesheets().add("""
            data:text/css,
            
            .styled-text-flow,
            .rich-text-area .content,
            .code-area .content .label,
            .code-area .content TextFlow {
              -fx-font-family: "JetBrains Mono";
              -fx-font-size: 14px;
            }
            """);

        stage.setScene(scene);

        stage.show();
    }

    private static void setupLogger() {
        try {
            LogManager.getLogManager().readConfiguration(
                Launcher.class.getResourceAsStream("/logging.properties")
            );
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
