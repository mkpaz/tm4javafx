/* SPDX-License-Identifier: MIT */

package tm4javafx.richtext;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import org.jspecify.annotations.Nullable;
import tm4java.grammar.IGrammar;
import tm4java.grammar.IGrammarSource;
import tm4java.grammar.IStateStack;
import tm4java.grammar.IToken;
import tm4java.grammar.ITokenizeLineResult;
import tm4java.registry.Registry;
import tm4java.theme.ITheme;
import tm4java.theme.IThemeSource;
import tm4java.theme.StyleAttributes;

/**
 * The {@code StyleProvider} offers an API to tokenize text into styled
 * segments with a specified grammar and theme.
 * <p>
 * Usage:
 * <pre>{@code
 * var provider = new StyleProvider();
 * provider.setGrammar(IGrammarSource.fromFile(...));
 * provider.setTheme(IThemeSource.fromFile(...));
 *
 * List<StyledToken> tokens = provider.tokenize("public static void main(String[] args) {}");
 * for (var token : tokens) {
 *     // use text and style attributes from token to update some UI control
 * }
 *
 * var settings = provider.getThemeSettings();
 * // apply theme settings to some UI control
 * }</pre>
 */
public class StyleProvider {

    protected static final Duration DEFAULT_TOKENIZATION_TIMEOUT = Duration.ofSeconds(1);

    protected final Registry registry;
    protected final HashMap<StyleAttributes, StyleAttributeMap> styleCache = new HashMap<>();

    protected @Nullable IGrammar grammar;
    protected @Nullable ITheme theme;
    protected @Nullable IStateStack prevState;
    protected @Nullable ThemeSettings themeSettings;
    protected Duration tokenizationTimeout = DEFAULT_TOKENIZATION_TIMEOUT;

    /**
     * Creates a new {@code StyleProvider} with a new registry.
     */
    public StyleProvider() {
        this(new Registry());
    }

    /**
     * Creates a new {@code StyleProvider} with a specified registry.
     * <p>
     * Different instances of {@code StyleProvider} must share the same {@code Registry}
     * instance with care, as the registry is limited to a single theme. Thus, different
     * providers that share the same registry instance cannot use different themes.
     */
    public StyleProvider(Registry registry) {
        this.registry = Objects.requireNonNull(registry, "Registry must not be null");

        // handles the situation when the theme changes directly on the registry,
        // outside the scope of the style provider
        this.registry.addThemeCallback(theme -> {
            if (this.theme != theme) {
                this.theme = theme;
                flush();
            }
        });
    }

    /**
     * Returns the {@link Registry} instance used by this style provider.
     */
    public final Registry getRegistry() {
        return registry;
    }

    /**
     * Returns the current grammar instance used by this style provider,
     * or {@code null} if not set.
     */
    public @Nullable IGrammar getGrammar() {
        return grammar;
    }

    /**
     * Sets the grammar to be used by the style provider.
     */
    public void setGrammar(IGrammar grammar) {
        this.grammar = grammar;
        flush();
    }

    /**
     * Sets the grammar to be used by the style provider.
     * <p>
     * The specified grammar will be loaded from the input source and added to the registry.
     */
    public IGrammar setGrammar(IGrammarSource grammarSource) {
        grammar = registry.addGrammar(grammarSource);
        flush(); // prev state must not be used after changing grammar
        return grammar;
    }

    /**
     * Returns the {@link ITheme} instance used by this style provider.
     */
    public @Nullable ITheme getTheme() {
        return theme;
    }

    /**
     * Sets the grammar to be used by the style provider.
     * <p>
     * The specified theme will be loaded from the input source.
     */
    public ITheme setTheme(IThemeSource themeSource) {
        theme = registry.setTheme(themeSource);
        flush(); // prev state must not be used after changing theme
        return theme;
    }

    /**
     * Tokenizes the given line of text into a list of tokens containing
     * the style information.
     * <p>
     * This method also maintains the state of tokenization, so the next line
     * tokenization will take into account previous tokenization results.
     * <p>
     * See {@link IGrammar#tokenizeLine(String, IStateStack, Duration)} for more information.
     */
    public List<StyledToken> tokenize(String line) {
        return doTokenize(line);
    }

    /**
     * Returns the tokenization timeout, after which tokenization is aborted.
     */
    public Duration getTokenizationTimeout() {
        return tokenizationTimeout;
    }

    /**
     * Sets the tokenization timeout, after which tokenization is aborted.
     */
    public void setTokenizationTimeout(@Nullable Duration tokenizationTimeout) {
        this.tokenizationTimeout = Objects.requireNonNullElse(tokenizationTimeout, DEFAULT_TOKENIZATION_TIMEOUT);
    }

    /**
     * Returns the current theme settings.
     * <p>
     * See {@link ThemeSettings} for more information.
     */
    public @Nullable ThemeSettings getThemeSettings() {
        if (themeSettings != null) {
            return themeSettings;
        }

        themeSettings = theme != null ? ThemeSettings.from(theme) : null;
        return themeSettings;
    }

    /**
     * Resets the maintained tokenization state and internal caches.
     * <p>
     * This should happen automatically after changing the current theme or grammar.
     */
    public void flush() {
        prevState = null;
        styleCache.clear();
        themeSettings = null;
    }

    //*************************************************************************

    /**
     * Tokenizes the given line of text into a list of tokens containing
     * the style information.
     */
    protected List<StyledToken> doTokenize(String line) {
        if (grammar == null || theme == null || line.isEmpty()) {
            return List.of();
        }

        var styledTokens = new ArrayList<StyledToken>();
        ITokenizeLineResult<IToken[]> result = grammar.tokenizeLine(line, prevState, getTokenizationTimeout());
        prevState = result.ruleStack();

        if (result.stoppedEarly()) {
            return List.of(new StyledToken(line, null));
        }

        for (int i = 0; i < result.tokens().length; i++) {
            var token = result.tokens()[i];
            var style = resolveStyle(token.getScopes());
            var text = line.substring(token.getStartIndex(), token.getEndIndex());

            styledTokens.add(new StyledToken(text, style));
        }

        return styledTokens;
    }

    /**
     * Resolves the list of scopes associated with a token into style
     * attributes using the current theme of the style provider.
     */
    protected @Nullable StyleAttributeMap resolveStyle(List<String> scopeStack) {
        var settings = getThemeSettings();
        if (settings == null || theme == null) {
            return null;
        }

        // prepare defaults
        StyleAttributeMap defaults = settings.getMergedDefaults();

        StyleAttributes attrs = null;
        for (int i = scopeStack.size() - 1; i >= 0; i--) { // most specific scope is the last one
            var scope = scopeStack.get(i);
            attrs = theme.match(scope);
            if (attrs != null && !StyleAttributes.NO_STYLE.equals(attrs)) {
                break;
            }
        }

        // if no styles are found, use defaults
        if (attrs == null || StyleAttributes.NO_STYLE.equals(attrs)) {
            return defaults;
        }

        var cachedStyle = styleCache.get(attrs);
        if (cachedStyle != null) {
            return cachedStyle;
        }

        var style = settings.resolve(attrs);
        styleCache.put(attrs, style);

        return style;
    }
}
