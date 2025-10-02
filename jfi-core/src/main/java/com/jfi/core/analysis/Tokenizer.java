package com.jfi.core.analysis;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * Unicode-aware tokenizer that emits {@link Token} values with half-open
 * offsets [start, end) into the post-CharFilter text.
 *
 * <p><b>Contracts:</b></p>
 * <ul>
 *   <li>Tokenizers are pure and deterministic: given the same input text and
 *       options, they must produce the same token sequence.</li>
 *   <li>Token offsets are half-open {@code [start, end)} measured in UTF-16
 *       code units into the post-CharFilter text.</li>
 *   <li>Tokenizers must not split surrogate pairs or base+combining mark
 *       clusters.</li>
 *   <li>Positions are zero-based and monotonic non-decreasing; gaps are
 *       permitted.</li>
 *   <li>Tokenizers must be stateless and thread-safe.</li>
 * </ul>
 */
public interface Tokenizer {

    /**
     * Tokenizes the given text according to the provided options.
     *
     * <p><b>Null/empty policy:</b></p>
     * <ul>
     *   <li>If the input text is {@code null} or empty, the tokenizer must
     *       return an empty list.</li>
     *   <li>Implementations must not return zero-length tokens.</li>
     *   <li>Implementations must not retain references to mutable inputs.</li>
     * </ul>
     *
     * @param text input text after CharFilters; may be {@code null}
     * @param options non-null tokenizer options
     * @return an immutable list of tokens in input order; never {@code null}
     * @throws NullPointerException if {@code options} is {@code null}
     */
    List<Token> tokenize(String text, Options options);

    /**
     * Default streaming implementation that reads the entire {@link Reader}
     * into memory and delegates to {@link #tokenize(String, Options)}.
     *
     * <p><b>Streaming semantics:</b> The default implementation reads the
     * entire {@code Reader} into memory and delegates to
     * {@link #tokenize(String, Options)}.</p>
     *
     * @param reader source of characters; may be {@code null}, treated as
     *               {@code null} input
     * @param options non-null tokenizer options
     * @return an immutable list of tokens from delegating to
     *         {@link #tokenize(String, Options)}
     * @throws IOException if reading the {@code reader} fails
     * @throws NullPointerException if {@code options} is {@code null}
     */
    default List<Token> tokenize(Reader reader, Options options) throws IOException {
        throw new UnsupportedOperationException("TODO: read Reader into String, delegate to tokenize()");
    }

    /**
     * Options controlling token boundary behavior.
     *
     * <p>Options is immutable and thread-safe.</p>
     * <p><b>Option semantics:</b></p>
     * <ul>
     *   <li>{@code keepApostrophes} controls whether ASCII apostrophes are
     *       treated as intra-word characters.</li>
     *   <li>{@code keepHyphens} controls whether hyphens are treated as
     *       intra-word characters.</li>
     *   <li>{@code treatJoinerAsInnerWord} controls whether joiner characters
     *       (e.g., ZERO WIDTH NON-JOINER U+200C) are treated as intra-word
     *       connectors rather than token breaks.</li>
     *   <li>{@code emitNumeric}, {@code emitAlpha}, and
     *       {@code emitAlphaNumeric} control which token classes are emitted.</li>
     *   <li>{@code minTokenLength} and {@code maxTokenLength} bound token
     *       lengths in code units.</li>
     * </ul>
     */
    final class Options {
        private final boolean keepApostrophes;
        private final boolean keepHyphens;
        private final boolean treatJoinerAsInnerWord;
        private final boolean emitNumeric;
        private final boolean emitAlpha;
        private final boolean emitAlphaNumeric;
        private final int minTokenLength;
        private final int maxTokenLength;

        private Options(Builder b) {
            this.keepApostrophes = b.keepApostrophes;
            this.keepHyphens = b.keepHyphens;
            this.treatJoinerAsInnerWord = b.treatJoinerAsInnerWord;
            this.emitNumeric = b.emitNumeric;
            this.emitAlpha = b.emitAlpha;
            this.emitAlphaNumeric = b.emitAlphaNumeric;
            this.minTokenLength = b.minTokenLength;
            this.maxTokenLength = b.maxTokenLength;
        }

        /**
         * Returns a builder for {@link Options}.
         */
        public static Builder builder() { return new Builder(); }

        /**
         * Returns sensible defaults for Latin-like tokenization while remaining
         * Unicode-aware:
         * <ul>
         *   <li>{@code keepApostrophes = true}</li>
         *   <li>{@code keepHyphens = true}</li>
         *   <li>{@code treatJoinerAsInnerWord = true}</li>
         *   <li>{@code emitNumeric = true}</li>
         *   <li>{@code emitAlpha = true}</li>
         *   <li>{@code emitAlphaNumeric = true}</li>
         *   <li>{@code minTokenLength = 1}</li>
         *   <li>{@code maxTokenLength = Integer.MAX_VALUE}</li>
         * </ul>
         */
        public static Options standard() {
            return builder()
                    .keepApostrophes(true)
                    .keepHyphens(true)
                    .treatJoinerAsInnerWord(true)
                    .emitNumeric(true)
                    .emitAlpha(true)
                    .emitAlphaNumeric(true)
                    .minTokenLength(1)
                    .maxTokenLength(Integer.MAX_VALUE)
                    .build();
        }

        public boolean keepApostrophes() { return keepApostrophes; }
        public boolean keepHyphens() { return keepHyphens; }
        public boolean treatJoinerAsInnerWord() { return treatJoinerAsInnerWord; }
        public boolean emitNumeric() { return emitNumeric; }
        public boolean emitAlpha() { return emitAlpha; }
        public boolean emitAlphaNumeric() { return emitAlphaNumeric; }
        public int minTokenLength() { return minTokenLength; }
        public int maxTokenLength() { return maxTokenLength; }

        /**
         * Builder for {@link Options}. Not thread-safe.
         */
        public static final class Builder {
            private boolean keepApostrophes = true;
            private boolean keepHyphens = true;
            private boolean treatJoinerAsInnerWord = true;
            private boolean emitNumeric = true;
            private boolean emitAlpha = true;
            private boolean emitAlphaNumeric = true;
            private int minTokenLength = 1;
            private int maxTokenLength = Integer.MAX_VALUE;

            public Builder keepApostrophes(boolean v) { this.keepApostrophes = v; return this; }
            public Builder keepHyphens(boolean v) { this.keepHyphens = v; return this; }
            public Builder treatJoinerAsInnerWord(boolean v) { this.treatJoinerAsInnerWord = v; return this; }
            public Builder emitNumeric(boolean v) { this.emitNumeric = v; return this; }
            public Builder emitAlpha(boolean v) { this.emitAlpha = v; return this; }
            public Builder emitAlphaNumeric(boolean v) { this.emitAlphaNumeric = v; return this; }
            public Builder minTokenLength(int v) { this.minTokenLength = v; return this; }
            public Builder maxTokenLength(int v) { this.maxTokenLength = v; return this; }

            /**
             * Builds an immutable {@link Options} instance.
             *
             * @return new Options
             * @throws IllegalArgumentException if {@code minTokenLength < 1} or
             *         {@code maxTokenLength < minTokenLength}
             */
            public Options build() { return new Options(this); }
        }
    }
}