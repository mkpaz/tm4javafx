/* SPDX-License-Identifier: MIT */

package tm4javafx.demo;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.text.TextFlow;

public class ScrollableTextFlow extends ScrollPane {

    protected final TextFlow textFlow = new TextFlow();

    public ScrollableTextFlow() {
        super();

        textFlow.getStyleClass().setAll("styled-text-flow");
        textFlow.setMinHeight(Region.USE_PREF_SIZE);
        textFlow.setMinWidth(Region.USE_PREF_SIZE);

        setPadding(new Insets(5));
        setMaxHeight(20_000);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setFitToHeight(true);
        setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setFitToWidth(true);
        setContent(textFlow);
    }

    public TextFlow getTextFlow() {
        return textFlow;
    }
}
