package leo.demo;

import leo.demo.state.StateMachine;
import leo.demo.state.StateMachineInstance;
import leo.demo.state.StateTransition;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ArgumentTokenizer {
    /**
     * A list of special characters that support escaping.
     */
    private static final Map<Character, Character> escapeCharacters = new HashMap<>() {{
        put('n', '\n');
        put('r', '\r');
        put('\'', '\'');
        put('\"', '"');
        put('t', '\t');
    }};

    /**
     * State machine definition that will define how to handle parsing an argument string.
     */
    private static final StateMachine<States, Events, TokenizerContext, Character> stateMachine = new StateMachine<>(
        States.WHITESPACE) {{
        /*
         * Whitespace-specific states.
         */

        // Whitespace to whitespace is a no-op
        addTransition(new StateTransition<>(States.WHITESPACE, States.WHITESPACE, Events.WHITESPACE));

        // Whitespace to end state
        addTransition(new StateTransition<>(States.WHITESPACE, States.END, Events.END));

        /*
         * Non-quoted token states.
         */
        // Start escape sequence for non-quoted tokens from whitespace
        addTransition(new StateTransition<>(States.WHITESPACE, States.TOKEN_ESCAPE, Events.BACKSLASH));

        // Start non-quoted token with non-whitespace characters
        addTransition(new StateTransition<>(States.WHITESPACE, States.TOKEN, Events.NON_WHITESPACE,
            TokenizerContext::appendToken));

        // Start escape sequence for non-quoted tokens from mid-token
        addTransition(new StateTransition<>(States.TOKEN, States.TOKEN_ESCAPE, Events.BACKSLASH));

        // Append a normal character in a non-quoted token
        addTransition(
            new StateTransition<>(States.TOKEN, States.TOKEN, Events.NON_WHITESPACE, TokenizerContext::appendToken));

        // Append an escaped single-quote in a non-quoted token
        addTransition(new StateTransition<>(States.TOKEN_ESCAPE, States.TOKEN, Events.SINGLE_QUOTE,
            TokenizerContext::appendToken));

        // Append an escaped double-quote in a non-quoted token
        addTransition(new StateTransition<>(States.TOKEN_ESCAPE, States.TOKEN, Events.DOUBLE_QUOTE,
            TokenizerContext::appendToken));

        // Append an escaped character in a non-quoted token as a literal
        addTransition(new StateTransition<>(States.TOKEN_ESCAPE, States.TOKEN, Events.NON_WHITESPACE,
            TokenizerContext::appendToken));

        // Append an escaped backslash in a non-quoted token
        addTransition(
            new StateTransition<>(States.TOKEN_ESCAPE, States.TOKEN, Events.BACKSLASH, TokenizerContext::appendToken));

        // End of argument string for a non-quoted token
        addTransition(new StateTransition<>(States.TOKEN, States.END, Events.END, (context, c) -> context.endToken()));

        // End non-quoted token when whitespace is encountered
        addTransition(new StateTransition<>(States.TOKEN, States.WHITESPACE, Events.WHITESPACE,
            (context, c) -> context.endToken()));

        /*
         * Double-quoted token states.
         */
        // Start a double-quoted token with a double-quote
        addTransition(new StateTransition<>(States.WHITESPACE, States.DOUBLE_QUOTED_TOKEN, Events.DOUBLE_QUOTE));

        // Append a normal character in a double-quoted token
        addTransition(
            new StateTransition<>(States.DOUBLE_QUOTED_TOKEN, States.DOUBLE_QUOTED_TOKEN, Events.NON_WHITESPACE,
                TokenizerContext::appendToken));

        // Append a whitespace character in a double-quoted token
        addTransition(new StateTransition<>(States.DOUBLE_QUOTED_TOKEN, States.DOUBLE_QUOTED_TOKEN, Events.WHITESPACE,
            TokenizerContext::appendToken));

        // Append a single quote in a double-quoted token
        addTransition(new StateTransition<>(States.DOUBLE_QUOTED_TOKEN, States.DOUBLE_QUOTED_TOKEN, Events.SINGLE_QUOTE,
            TokenizerContext::appendToken));

        // Start escape sequence in a double-quoted token
        addTransition(
            new StateTransition<>(States.DOUBLE_QUOTED_TOKEN, States.DOUBLE_QUOTED_TOKEN_ESCAPE, Events.BACKSLASH));

        // Append an escaped backslash in a double-quoted token
        addTransition(
            new StateTransition<>(States.DOUBLE_QUOTED_TOKEN_ESCAPE, States.DOUBLE_QUOTED_TOKEN, Events.BACKSLASH,
                TokenizerContext::appendToken));

        // Append an escaped double-quote in a double-quoted token
        addTransition(
            new StateTransition<>(States.DOUBLE_QUOTED_TOKEN_ESCAPE, States.DOUBLE_QUOTED_TOKEN, Events.DOUBLE_QUOTE,
                TokenizerContext::appendToken));

        // Append an escaped single-quote in a double-quoted token
        addTransition(
            new StateTransition<>(States.DOUBLE_QUOTED_TOKEN_ESCAPE, States.DOUBLE_QUOTED_TOKEN, Events.SINGLE_QUOTE,
                TokenizerContext::appendToken));

        // Append an escaped character in a double-quoted token
        addTransition(
            new StateTransition<>(States.DOUBLE_QUOTED_TOKEN_ESCAPE, States.DOUBLE_QUOTED_TOKEN, Events.NON_WHITESPACE,
                TokenizerContext::appendEscapedToken));

        // End a double-quoted token due to a closing double-quote
        addTransition(new StateTransition<>(States.DOUBLE_QUOTED_TOKEN, States.WHITESPACE, Events.DOUBLE_QUOTE,
            (context, c) -> context.endToken()));

        /*
         * Single-quoted token states.
         */
        // Start a single-quoted token with a single-quote
        addTransition(new StateTransition<>(States.WHITESPACE, States.SINGLE_QUOTED_TOKEN, Events.SINGLE_QUOTE));

        // Append a normal character in a single-quoted token
        addTransition(
            new StateTransition<>(States.SINGLE_QUOTED_TOKEN, States.SINGLE_QUOTED_TOKEN, Events.NON_WHITESPACE,
                TokenizerContext::appendToken));

        // Append a whitespace character in a single-quoted token
        addTransition(new StateTransition<>(States.SINGLE_QUOTED_TOKEN, States.SINGLE_QUOTED_TOKEN, Events.WHITESPACE,
            TokenizerContext::appendToken));

        // Append a double quote in a single-quoted token
        addTransition(new StateTransition<>(States.SINGLE_QUOTED_TOKEN, States.SINGLE_QUOTED_TOKEN, Events.DOUBLE_QUOTE,
            TokenizerContext::appendToken));

        // Append an escape character in a single-quoted token
        addTransition(new StateTransition<>(States.SINGLE_QUOTED_TOKEN, States.SINGLE_QUOTED_TOKEN, Events.BACKSLASH,
            TokenizerContext::appendToken));

        // End a single-quoted token due to a closing double-quote
        addTransition(new StateTransition<>(States.SINGLE_QUOTED_TOKEN, States.WHITESPACE, Events.SINGLE_QUOTE,
            (context, c) -> context.endToken()));
    }};
    /**
     * A list of characters that are considered whitespace.
     */
    private static final List<Character> whiteSpaceCharacters = new LinkedList<>() {{
        add(' ');
        add('\t');
    }};

    /**
     * Tokenizes an argument string into a string array suitable for other operations that depends on typical Java args
     * handling.
     * <p>
     * The parser will handle quoted arguments for both double quotes and single quotes, and properly handle escaped
     * quotes.
     * <p>
     * Argument tokens that are not wrapped in quotations are considered separated by unescaped whitespace.
     * <p>
     * However, arguments that are wrapped in double or single quotes allow whitespace to persist as part of a single
     * argument. Quoted arguments must begin and end with the same quotation character, and may not be started in the
     * middle of another token. For example, {@code "foo bar" "baz"} are valid tokens, however, {@code fo"o bar} are not
     * valid since the quotation mark begins in the middle of another word.
     * <p>
     * Certain escape characters are allowed depending on the type of token, specifically:
     *
     * <ul>
     *     <li>{@code \n} inserts a newline character</li>
     *     <li>{@code \t} inserts a tab character</li>
     *     <li>{@code \\} inserts a backslash character</li>
     *     <li>{@code \"} inserts a double quotation mark character</li>
     *     <li>{@code \'} inserts a single quotation mark character</li>
     * </ul>
     * <p>
     * Unquoted and double-quoted tokens will allow and parse the supported escape characters, however, single-quoted
     * tokens are intended to be string literals and will not attempt to use escape characters at all. That means that
     * while the token {@code "foo\tbar"} may result in a token that resembles {@code foo    bar}, the token
     * {@code 'foo\tbar'} will be presented unmodified as {@code foo\tbar}. Additionally, since escape characters are
     * not interpreted in single-quoted tokens, backslash characters are also literal.
     *
     * @param raw The raw, un-tokenized string containing arguments.
     * @return An array of strings containing parsed argument tokens.
     */
    public String[] tokenize(String raw) {
        TokenizerContext context = new TokenizerContext();
        StateMachineInstance<States, Events, TokenizerContext, Character> instance = stateMachine.start(context);

        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);

            if (isWhiteSpace(c)) {
                instance.submit(Events.WHITESPACE, c);
            } else if (c == '\\') {
                instance.submit(Events.BACKSLASH, c);
            } else if (c == '\'') {
                instance.submit(Events.SINGLE_QUOTE, c);
            } else if (c == '"') {
                instance.submit(Events.DOUBLE_QUOTE, c);
            } else {
                instance.submit(Events.NON_WHITESPACE, c);
            }
        }

        instance.submit(Events.END, null);

        return context.getTokens();
    }

    /**
     * Returns whether the given character is considered whitespace.
     *
     * @param c Character to check.
     * @return Whether the given character is considered whitespace.
     */
    private boolean isWhiteSpace(char c) {
        return whiteSpaceCharacters.contains(c);
    }

    /**
     * List of states that define the various stages of the state machine.
     */
    private enum States {
        WHITESPACE, TOKEN, TOKEN_ESCAPE, SINGLE_QUOTED_TOKEN, DOUBLE_QUOTED_TOKEN, DOUBLE_QUOTED_TOKEN_ESCAPE, END
    }

    /**
     * List of events that will trigger transitions for the tokenizer state machine.
     */
    private enum Events {
        NON_WHITESPACE, WHITESPACE, BACKSLASH, DOUBLE_QUOTE, SINGLE_QUOTE, END
    }

    /**
     * A context object that handles aggregating characters and completed tokens when parsing an argument string.
     */
    private static class TokenizerContext {
        /**
         * A list of completed tokens parsed from an argument string.
         */
        private final List<String> tokens = new LinkedList<>();

        /**
         * A string builder that will aggregate characters until a token is deemed complete. The contents of the builder
         * will be stored in {@link #tokens} once a token is done.
         */
        private StringBuilder currentToken = new StringBuilder();

        /**
         * Adds a character to the token currently being built.
         *
         * @param c Character to add to the token.
         */
        void appendToken(char c) {
            this.currentToken.append(c);
        }

        /**
         * Appends a character based on an escape character.
         * <p>
         * This method throws an {@link IllegalArgumentException} when an unsupported escape character is encountered.
         *
         * @param c Escape character to translate and add to the token
         */
        void appendEscapedToken(char c) {
            if (escapeCharacters.containsKey(c)) {
                this.currentToken.append(escapeCharacters.get(c));
            } else {
                throw new IllegalArgumentException(c + " is not a valid escape character");
            }
        }

        /**
         * Completes the current token and adds it to the list of {@link #tokens}.
         */
        void endToken() {
            if (this.currentToken.length() > 0) {
                tokens.add(this.currentToken.toString());
            }
            this.currentToken = new StringBuilder();
        }

        /**
         * Returns the list of tokens that were parsed from an argument string.
         *
         * @return The list of completed tokens.
         */
        String[] getTokens() {
            return tokens.toArray(new String[0]);
        }
    }
}
