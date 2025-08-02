## tm4javafx

TextMate (VSCode) syntax highlighting for JavaFX.

This project is powered by [tm4java](https://github.com/mkpaz/tm4java) and offers TextMate syntax highlighting
for `TextFlow`, `RichTextArea` and `CodeArea`.

<p align="center">
<img src="https://raw.githubusercontent.com/mkpaz/tm4javafx/master/.screenshots/demo.png" alt="img"/>
</p>

Note that JavaFX RichTextArea is in the incubator, so it requires an early access (EA) build, and the API may
be subject to changes.

### Usage

Maven:

```xml

<dependency>
    <groupId>io.github.mkpaz</groupId>
    <artifactId>tm4javafx</artifactId>
    <version>0.1.0</version>
</dependency>
```

Gradle:

```groovy
repositories {
    mavenCentral()
}
dependencies {
    implementation 'io.github.mkpaz:tm4javafx:0.1.0'
}
```

#### Example

```java
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

var richTextArea = new RichTextArea();
richTextArea.setPrefHeight(200);

var richTextAreaModel = new RichTextAreaModel();
richTextAreaModel.setRichTextArea(richTextArea);
richTextAreaModel.setStyleProvider(styleProvider);
richTextAreaModel.setText(sampleText);

var scene = new Scene(richTextArea, 800, 600);
stage.setScene(scene);
stage.setOnShown(_ -> {
    StyleHelper.applyThemeSettings(richTextArea, styleProvider.getThemeSettings());
});
stage.show();
```

You can find the full version of this in [UsageExample.java](demo/src/main/java/tm4javafx/demo/UsageExample.java).

### Demo

To run the demo (shown in the screenshot) and play with grammars/themes, use:

```sh
JAVA_HOME=/path/to/jdk/24
cd demo
mvn javafx:run
```
