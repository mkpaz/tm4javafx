/* SPDX-License-Identifier: MIT */

package tm4javafx.demo;

import tm4java.theme.IThemeSource;

import java.nio.file.Path;

public record ThemeLink(String name, Path themePath) implements Comparable<ThemeLink> {

    public IThemeSource getThemeSource() {
        return IThemeSource.fromFile(themePath);
    }

    static ThemeLink of(Path path) {
        String[] filenameParts = path.getFileName().toString().split("\\.");
        if (filenameParts.length < 2) {
            throw new RuntimeException("Unexpected theme file name: " + path);
        }
        return new ThemeLink(filenameParts[0], path);
    }

    @Override
    public int compareTo(ThemeLink other) {
        return name.compareTo(other.name);
    }
}
