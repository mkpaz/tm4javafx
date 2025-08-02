import org.jspecify.annotations.NullMarked;

@NullMarked
module tm4javafx {
    requires static org.jspecify;

    requires transitive tm4java;
    requires transitive jfx.incubator.richtext;
    requires transitive jfx.incubator.input;

    exports tm4javafx.richtext;
}
