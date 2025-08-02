/* SPDX-License-Identifier: MIT */

package tm4javafx.demo;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.Nullable;

public final class DemoView extends BorderPane {

    private final ComboBox<GrammarLink> grammarCombo = new ComboBox<>();
    private final ComboBox<ThemeLink> themeCombo = new ComboBox<>();
    private final ListView<ControlType> controlTypeList = new ListView<>();
    private final VBox richTextPane = new VBox();
    private final DemoViewModel model;

    public DemoView(DemoViewModel model) {
        super();

        this.model = model;
        createLayout();
        init();
    }

    private void createLayout() {
        grammarCombo.setCellFactory(_ -> new GrammarListCell());
        grammarCombo.setButtonCell(new GrammarListCell());
        grammarCombo.setPrefWidth(200);
        HBox.setHgrow(grammarCombo, Priority.NEVER);
        grammarCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(GrammarLink grammar) {
                return grammar.name();
            }

            @Override
            public @Nullable GrammarLink fromString(String s) {
                return null;
            }
        });

        themeCombo.setPrefWidth(200);
        HBox.setHgrow(themeCombo, Priority.NEVER);
        themeCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(ThemeLink theme) {
                return theme.name();
            }

            @Override
            public @Nullable ThemeLink fromString(String s) {
                return null;
            }
        });

        var topBar = new HBox(
            10,
            new Label("Grammar:"), grammarCombo,
            new Spacer(5, 5),
            new Label("Theme:"), themeCombo
        );
        topBar.setPadding(new Insets(5));
        topBar.setAlignment(Pos.CENTER_LEFT);

        controlTypeList.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(ControlType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getTitle());
            }
        });

        var sideBar = new VBox(controlTypeList);
        VBox.setVgrow(controlTypeList, Priority.ALWAYS);
        sideBar.setPadding(new Insets(5));
        sideBar.setPrefWidth(300);

        richTextPane.setPadding(new Insets(5));

        setPadding(new Insets(5));
        setTop(topBar);
        setLeft(sideBar);
        setCenter(richTextPane);
    }

    private void init() {
        model.richTextControl.subscribe(node -> {
            if (node != null) {
                VBox.setVgrow(node, Priority.ALWAYS);
                richTextPane.getChildren().setAll(node);
            } else {
                richTextPane.getChildren().clear();
            }
        });

        Bindings.bindContent(grammarCombo.getItems(), model.grammars);
        model.selectedGrammar.bind(grammarCombo.getSelectionModel().selectedItemProperty());

        Bindings.bindContent(themeCombo.getItems(), model.themes);
        model.selectedTheme.bind(themeCombo.getSelectionModel().selectedItemProperty());

        Bindings.bindContent(controlTypeList.getItems(), model.controlsTypes);
        model.selectedControlType.bind(controlTypeList.getSelectionModel().selectedItemProperty());

        controlTypeList.getSelectionModel().selectFirst();

        themeCombo.getSelectionModel().selectFirst();
        model.themes.stream()
            .filter(link -> DemoViewModel.DEFAULT_THEME.equalsIgnoreCase(link.name()))
            .findFirst()
            .ifPresentOrElse(
                link -> themeCombo.getSelectionModel().select(link),
                () -> themeCombo.getSelectionModel().selectFirst()
            );
        model.grammars.stream()
            .filter(link -> DemoViewModel.DEFAULT_GRAMMAR.equalsIgnoreCase(link.name()))
            .findFirst()
            .ifPresentOrElse(
                link -> grammarCombo.getSelectionModel().select(link),
                () -> grammarCombo.getSelectionModel().selectFirst()
            );
    }

    //*************************************************************************

    private static class Spacer extends Region {

        public Spacer(int width, int height) {
            super();
            setMinSize(width, height);
            setMaxSize(width, height);
        }
    }

    public static class GrammarListCell extends ListCell<GrammarLink> {

        public GrammarListCell() {
            super();
        }

        @Override
        protected void updateItem(GrammarLink item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item.name());
            }
        }
    }
}
