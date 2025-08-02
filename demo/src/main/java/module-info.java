import org.jspecify.annotations.NullMarked;

@NullMarked
module tm4javafx.demo {
    requires static org.jspecify;

    requires tm4javafx;
    requires javafx.controls;
    requires java.logging;

    exports tm4javafx.demo;
}