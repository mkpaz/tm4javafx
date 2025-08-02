/* SPDX-License-Identifier: MIT */

package tm4javafx.demo;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class Resources {

    public static URL getResource(String path) {
        return Objects.requireNonNull(Resources.class.getResource(path));
    }

    public static Path getDirectory(String path) {
        try {
            return Paths.get(Objects.requireNonNull(Resources.class.getResource(path)).toURI().getPath());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Path getFile(String path) {
        try {
            return Paths.get(Objects.requireNonNull(Resources.class.getResource(path)).toURI().getPath());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
