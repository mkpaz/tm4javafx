/* SPDX-License-Identifier: MIT */

package tm4javafx.demo;

public enum ControlType {

    TEXT_FLOW("TextFlow"),
    RICH_TEXT("RichTextArea"),
    CODE_AREA("CodeArea");

    private final String title;

    ControlType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
