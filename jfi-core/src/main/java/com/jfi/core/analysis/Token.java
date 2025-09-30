package com.jfi.core.analysis;

/**
 * Immutable lexical unit with half-open offsets [start, end) into the
 * post-CharFilter normalized text provided to the Tokenizer.
 * <p>
 * Offsets are half-open {@code [start, end)} measured in UTF-16 code units into the post-CharFilter normalized text provided to the Tokenizer.
 * For tokenizer contracts, {@code term} must equal {@code source.substring(start, end)} of the tokenizer’s input.
 * {@code position} is a zero-based absolute term position; token streams must be monotonic non-decreasing in {@code position}.
 * This type is immutable and thread-safe.
 */
public record Token(String term, int start, int end, int position) {

    /**
     * Canonical constructor enforcing invariants.
     * <p>
     * {@code term} must be non-null and non-empty.
     * {@code start} and {@code end} must satisfy {@code 0 ≤ start < end}.
     * {@code position} must be ≥ 0.
     * Zero-length tokens are invalid.
     *
     * @throws NullPointerException if {@code term} is null
     * @throws IllegalArgumentException if {@code term} is empty,
     *                                  or {@code start < 0},
     *                                  or {@code end < start},
     *                                  or {@code end == start},
     *                                  or {@code position < 0}
     */
    public Token {
        // TODO: validations.
    }

    /**
     * Returns the half-open span length in code units ({@code end - start}).
     */
    public int length() {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Returns a new token with the same offsets and position but a different term.
     *
     * @param newTerm non-null, non-empty term text
     * @return a new Token instance with {@code term == newTerm}
     * @throws NullPointerException if {@code newTerm} is null
     * @throws IllegalArgumentException if {@code newTerm} is empty
     */
    public Token withTerm(String newTerm) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Shifts both offsets by {@code delta}; throws if invariants would be violated.
     * Intended for re-basing offsets when preceding text is inserted/removed.
     *
     * @param delta the signed shift to apply to both offsets
     * @return a new Token instance with shifted offsets
     * @throws IllegalArgumentException if the shifted offsets violate invariants
     */
    public Token shift(int delta) {
        throw new UnsupportedOperationException("TODO");
    }
}
