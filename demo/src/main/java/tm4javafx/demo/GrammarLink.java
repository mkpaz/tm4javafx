/* SPDX-License-Identifier: MIT */

package tm4javafx.demo;

import tm4java.grammar.IGrammarSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record GrammarLink(String name, Path grammarPath, Path examplePath) implements Comparable<GrammarLink> {

    public String getExampleText(String defaultText) {
        try {
            return Files.readString(examplePath);
        } catch (IOException e) {
            return defaultText;
        }
    }

    public IGrammarSource getGrammarSource() {
        return IGrammarSource.fromFile(grammarPath);
    }

    @Override
    public int compareTo(GrammarLink other) {
        return name.compareTo(other.name);
    }

    //*************************************************************************

    record Sample(String language, String type, Path path) implements Comparable<Sample> {

        public String id() {
            return language + "." + type;
        }

        public String grammarId() {
            return language + ".tmLanguage";
        }

        public String exampleId() {
            return language + ".example";
        }

        public boolean isGrammar() {
            return "tmLanguage".equals(type);
        }

        public boolean isExample() {
            return "example".equals(type);
        }

        @Override
        public int hashCode() {
            return language.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Sample sample)) {
                return false;
            }
            return language.equals(sample.language);
        }

        @Override
        public int compareTo(Sample other) {
            return language.compareTo(other.language);
        }

        static Sample of(Path path) {
            String[] filenameParts = path.getFileName().toString().split("\\.");
            String language = filenameParts.length >= 2 ? filenameParts[0] : "";
            String type = filenameParts.length >= 2 ? filenameParts[1] : "";
            return new Sample(language, type, path);
        }
    }
}
