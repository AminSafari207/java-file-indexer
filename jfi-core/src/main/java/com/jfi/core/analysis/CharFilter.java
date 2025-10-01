package com.jfi.core.analysis;

import java.io.IOException;
import java.io.Reader;

/**
 * Pre-tokenization text normalization.
 * <p>
 * CharFilters operate on raw input text before tokenization. They are used to
 * normalize whitespace, remove or replace specific Unicode characters, and
 * apply script-aware rules (e.g., handling combining marks, joiners, or
 * elongation/expansion characters). A CharFilter produces a {@link Result}
 * that contains the normalized text and a {@code changed} flag indicating
 * whether the output differs from the input.
 * <p>
 * <b>Contracts:</b>
 * <ul>
 *   <li>CharFilters are pure and deterministic: given the same input text and
 *       options, they must produce the same output.</li>
 *   <li>CharFilters must be idempotent for a fixed set of options:
 *       {@code apply(apply(x)) == apply(x)}.</li>
 *   <li>Token offsets are measured against the <em>post-CharFilter</em> text;
 *       callers must never attempt to interpret token offsets against the raw
 *       input.</li>
 *   <li>Newline handling is applied <em>before</em> general whitespace
 *       collapsing.</li>
 *   <li>This interface performs no I/O; the default streaming method exists
 *       only for convenience.</li>
 * </ul>
 * <p>
 * Implementations must not retain references to mutable input. They must be
 * stateless and thread-safe.
 */
public interface CharFilter {

    /**
     * Applies the normalization to the given text using the provided options.
     *
     * <p><b>Null/empty policy:</b></p>
     * <ul>
     *   <li>If {@code text} is {@code null}, implementations must return a
     *       {@link Result} with {@code text == null} and
     *       {@code changed == false}.</li>
     *   <li>If {@code text} is empty, implementations must return a
     *       {@link Result} with an empty string and
     *       {@code changed == false}.</li>
     * </ul>
     *
     * @param text input text; may be {@code null}
     * @param options non-null options controlling normalization
     * @return a {@link Result} containing the normalized text and a
     *         {@code changed} flag
     * @throws NullPointerException if {@code options} is {@code null}
     */
    Result apply(String text, Options options);

    /**
     * Default streaming implementation that reads the entire {@link Reader}
     * into a {@link String} and delegates to {@link #apply(String, Options)}.
     *
     * <p><b>Streaming semantics:</b> This method is provided for convenience;
     * streaming chunked normalization is out of scope for this interface.</p>
     *
     * @param reader source of characters; may be {@code null}, treated as
     *               {@code null} input
     * @param options non-null options controlling normalization
     * @return a {@link Result} from delegating to
     *         {@link #apply(String, Options)}
     * @throws IOException if reading the {@code reader} fails
     * @throws NullPointerException if {@code options} is {@code null}
     */
    default Result apply(Reader reader, Options options) throws IOException {
        throw new UnsupportedOperationException("TODO: read Reader into String, delegate to apply()");
    }

    /**
     * Immutable normalization options.
     * <p>
     * Options control how a CharFilter processes newlines, whitespace,
     * Unicode normalization, combining marks, joiners, elongation characters,
     * and digits. Options are immutable and thread-safe.
     * <p>
     * <b>Precedence:</b> {@link NewlinePolicy} is applied before whitespace
     * collapsing. Unicode space normalization (if any) occurs before
     * collapsing. Unicode normalization form (e.g., NFC/NFKC) is applied
     * prior to other character-class policies where relevant.
     */
    final class Options {
        private final NewlinePolicy newlinePolicy;
        private final WhitespaceCollapse collapsePolicy;
        private final SpaceNormalization spaceNormalization;
        private final UnicodeNormalization unicodeNormalization;
        private final CombiningMarkPolicy combiningMarkPolicy;
        private final JoinerHandling joinerHandling;
        private final ElongationHandling elongationHandling;
        private final DigitNormalization digitNormalization;

        private Options(Builder b) {
            this.newlinePolicy = b.newlinePolicy;
            this.collapsePolicy = b.collapsePolicy;
            this.spaceNormalization = b.spaceNormalization;
            this.unicodeNormalization = b.unicodeNormalization;
            this.combiningMarkPolicy = b.combiningMarkPolicy;
            this.joinerHandling = b.joinerHandling;
            this.elongationHandling = b.elongationHandling;
            this.digitNormalization = b.digitNormalization;
        }

        /**
         * Returns a new {@link Builder} for Options.
         */
        public static Builder builder() { return new Builder(); }

        /**
         * Returns a standard default set of options suitable for general-purpose
         * Latin and many non-Latin scripts:
         * <ul>
         *   <li>newlinePolicy = PRESERVE</li>
         *   <li>collapsePolicy = COLLAPSE_SEQUENCES</li>
         *   <li>spaceNormalization = CONVERT_UNICODE_SPACES_TO_ASCII</li>
         *   <li>unicodeNormalization = NFC</li>
         *   <li>combiningMarkPolicy = PRESERVE</li>
         *   <li>joinerHandling = PRESERVE_AS_INNER_WORD</li>
         *   <li>elongationHandling = STRIP</li>
         *   <li>digitNormalization = PRESERVE</li>
         * </ul>
         */
        public static Options standard() {
            return builder()
                    .newlinePolicy(NewlinePolicy.PRESERVE)
                    .collapsePolicy(WhitespaceCollapse.COLLAPSE_SEQUENCES)
                    .spaceNormalization(SpaceNormalization.CONVERT_UNICODE_SPACES_TO_ASCII)
                    .unicodeNormalization(UnicodeNormalization.NFC)
                    .combiningMarkPolicy(CombiningMarkPolicy.PRESERVE)
                    .joinerHandling(JoinerHandling.PRESERVE_AS_INNER_WORD)
                    .elongationHandling(ElongationHandling.STRIP)
                    .digitNormalization(DigitNormalization.PRESERVE)
                    .build();
        }

        public NewlinePolicy newlinePolicy() { return newlinePolicy; }
        public WhitespaceCollapse collapsePolicy() { return collapsePolicy; }
        public SpaceNormalization spaceNormalization() { return spaceNormalization; }
        public UnicodeNormalization unicodeNormalization() { return unicodeNormalization; }
        public CombiningMarkPolicy combiningMarkPolicy() { return combiningMarkPolicy; }
        public JoinerHandling joinerHandling() { return joinerHandling; }
        public ElongationHandling elongationHandling() { return elongationHandling; }
        public DigitNormalization digitNormalization() { return digitNormalization; }

        /**
         * Builder for {@link Options}.
         * <p>
         * Builders are mutable and not thread-safe. Call {@link #build()} to
         * create an immutable Options instance.
         */
        public static final class Builder {
            private NewlinePolicy newlinePolicy = NewlinePolicy.PRESERVE;
            private WhitespaceCollapse collapsePolicy = WhitespaceCollapse.COLLAPSE_SEQUENCES;
            private SpaceNormalization spaceNormalization = SpaceNormalization.CONVERT_UNICODE_SPACES_TO_ASCII;
            private UnicodeNormalization unicodeNormalization = UnicodeNormalization.NFC;
            private CombiningMarkPolicy combiningMarkPolicy = CombiningMarkPolicy.PRESERVE;
            private JoinerHandling joinerHandling = JoinerHandling.PRESERVE_AS_INNER_WORD;
            private ElongationHandling elongationHandling = ElongationHandling.STRIP;
            private DigitNormalization digitNormalization = DigitNormalization.PRESERVE;

            public Builder newlinePolicy(NewlinePolicy v) { this.newlinePolicy = v; return this; }
            public Builder collapsePolicy(WhitespaceCollapse v) { this.collapsePolicy = v; return this; }
            public Builder spaceNormalization(SpaceNormalization v) { this.spaceNormalization = v; return this; }
            public Builder unicodeNormalization(UnicodeNormalization v) { this.unicodeNormalization = v; return this; }
            public Builder combiningMarkPolicy(CombiningMarkPolicy v) { this.combiningMarkPolicy = v; return this; }
            public Builder joinerHandling(JoinerHandling v) { this.joinerHandling = v; return this; }
            public Builder elongationHandling(ElongationHandling v) { this.elongationHandling = v; return this; }
            public Builder digitNormalization(DigitNormalization v) { this.digitNormalization = v; return this; }

            /**
             * Builds an immutable {@link Options} instance.
             *
             * @return a new {@link Options}
             * @throws NullPointerException if any option is {@code null}
             */
            public Options build() { return new Options(this); }
        }
    }

    /**
     * Result of applying a CharFilter.
     *
     * @param text normalized text; may be {@code null} if input was {@code null}
     * @param changed true if normalization modified the input text
     */
    record Result(String text, boolean changed) {}

    /**
     * Newline handling policy.
     * <ul>
     *   <li>{@code PRESERVE}: keep existing newlines; normalize CRLF and CR to LF.</li>
     *   <li>{@code NORMALIZE_TO_LF}: convert all newline variants to {@code '\n'}.</li>
     *   <li>{@code REMOVE}: remove all newline characters.</li>
     * </ul>
     */
    enum NewlinePolicy { PRESERVE, NORMALIZE_TO_LF, REMOVE }

    /**
     * Whitespace collapse policy (applies after newline handling).
     * <ul>
     *   <li>{@code NONE}: do not collapse runs of whitespace.</li>
     *   <li>{@code COLLAPSE_SEQUENCES}: collapse runs of non-newline whitespace to a single ASCII space.</li>
     * </ul>
     */
    enum WhitespaceCollapse { NONE, COLLAPSE_SEQUENCES }

    /**
     * Unicode space normalization policy.
     * <ul>
     *   <li>{@code NONE}: leave Unicode spaces unchanged.</li>
     *   <li>{@code CONVERT_UNICODE_SPACES_TO_ASCII}: convert known Unicode
     *       space separators (e.g., NBSP, EM SPACE) to ASCII space before collapsing.</li>
     * </ul>
     */
    enum SpaceNormalization { NONE, CONVERT_UNICODE_SPACES_TO_ASCII }

    /**
     * Unicode normalization form applied prior to other character-class policies where relevant.
     * <ul>
     *   <li>{@code NONE}: no change.</li>
     *   <li>{@code NFC}: canonical composition.</li>
     *   <li>{@code NFKC}: compatibility composition (may fold compatibility forms).</li>
     * </ul>
     */
    enum UnicodeNormalization { NONE, NFC, NFKC }

    /**
     * Policy for Unicode combining marks (category Mn).
     * <ul>
     *   <li>{@code PRESERVE}: keep combining marks.</li>
     *   <li>{@code STRIP_NONSPACING}: remove non-spacing marks (e.g., diacritics) while preserving base characters.</li>
     * </ul>
     */
    enum CombiningMarkPolicy { PRESERVE, STRIP_NONSPACING }

    /**
     * Policy for joiner characters (e.g., ZERO WIDTH NON-JOINER U+200C).
     * <ul>
     *   <li>{@code PRESERVE}: keep joiners as-is.</li>
     *   <li>{@code REMOVE}: remove joiners.</li>
     *   <li>{@code PRESERVE_AS_INNER_WORD}: preserve joiners and signal to tokenizers
     *       that joiners should be treated as intra-word connectors rather than token breaks.</li>
     * </ul>
     */
    enum JoinerHandling { PRESERVE, REMOVE, PRESERVE_AS_INNER_WORD }

    /**
     * Policy for elongation/expansion characters used to stretch words visually
     * (e.g., Arabic kashida/tatweel U+0640, and analogous elongation marks in other scripts).
     * <ul>
     *   <li>{@code PRESERVE}: keep elongation characters.</li>
     *   <li>{@code STRIP}: remove elongation characters.</li>
     * </ul>
     */
    enum ElongationHandling { PRESERVE, STRIP }

    /**
     * Digit normalization policy for script-specific digits.
     * <ul>
     *   <li>{@code PRESERVE}: keep script-specific digits.</li>
     *   <li>{@code MAP_TO_ASCII}: normalize Unicode decimal digits (category Nd)
     *       from supported scripts to ASCII 0–9 where the mapping is unambiguous.</li>
     * </ul>
     */
    enum DigitNormalization { PRESERVE, MAP_TO_ASCII }
}