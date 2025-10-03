/**
 * Core analysis contracts and invariants for tokenization pipelines.
 *
 * <h2>Scope</h2>
 * This package defines the foundational types for analysis:
 * {@link com.jfi.core.analysis.Token}, {@link com.jfi.core.analysis.CharFilter},
 * {@link com.jfi.core.analysis.Tokenizer}, and {@link com.jfi.core.analysis.Analyzer}.
 * Implementations in higher-level modules must adhere to these contracts.
 *
 * <h2>Determinism and Idempotence</h2>
 * <ul>
 *   <li>Analyzers, CharFilters, and Tokenizers are <b>pure and deterministic</b>:
 *       given the same input and options, they must produce the same output.</li>
 *   <li>CharFilters must be <b>idempotent</b> for fixed options:
 *       {@code apply(apply(x)) == apply(x)}.</li>
 *   <li>Tokenizers must not depend on external state (time, randomness, I/O).</li>
 * </ul>
 *
 * <h2>Offset and Position Semantics</h2>
 * <ul>
 *   <li>All token offsets are half-open {@code [start, end)} measured in
 *       <b>UTF-16 code units</b> into the <b>post-CharFilter</b> text.</li>
 *   <li>Implementations must not split surrogate pairs or base+combining mark
 *       clusters; substring via {@code source.substring(start, end)} must equal
 *       the token term.</li>
 *   <li>Positions are zero-based and <b>monotonic non-decreasing</b>; gaps are permitted.</li>
 *   <li>Zero-length tokens are invalid.</li>
 * </ul>
 *
 * <h2>Newline and Whitespace Precedence</h2>
 * <ul>
 *   <li>Newline handling is applied <b>before</b> general whitespace collapsing.</li>
 *   <li>Unicode space normalization (if enabled) is applied before collapsing.</li>
 *   <li>Unicode normalization form (e.g., NFC/NFKC) is applied prior to other
 *       character-class policies where relevant.</li>
 * </ul>
 *
 * <h2>Null/Empty Policy</h2>
 * <ul>
 *   <li>CharFilter: {@code null} input yields {@code Result(text=null, changed=false)};
 *       empty input yields empty text with {@code changed=false}.</li>
 *   <li>Tokenizer: {@code null} or empty input yields an empty token list.</li>
 *   <li>Analyzer: {@code null} input yields {@code Result(normalizedText=null, tokens=[])};
 *       empty input yields {@code Result(normalizedText="", tokens=[])}.</li>
 * </ul>
 *
 * <h2>Streaming Semantics</h2>
 * Default methods on CharFilter, Tokenizer, and Analyzer read an entire
 * {@link java.io.Reader} into memory and delegate to their string-based methods.
 * Streaming chunked normalization is out of scope for these interfaces.
 *
 * <h2>Unicode Policies (Language-Agnostic)</h2>
 * Implementations should use Unicode categories and options, not language-specific
 * assumptions. Examples:
 * <ul>
 *   <li><b>Combining marks:</b> policies may preserve or strip non-spacing marks (category Mn).</li>
 *   <li><b>Joiners:</b> ZERO WIDTH NON-JOINER (U+200C) may be preserved and treated as an
 *       intra-word connector rather than a token break.</li>
 *   <li><b>Elongation characters:</b> visual elongation (e.g., kashida) may be preserved or stripped
 *       via a generic elongation policy.</li>
 *   <li><b>Digits:</b> decimal digits (category Nd) from various scripts may be normalized to ASCII 0–9
 *       when configured.</li>
 * </ul>
 *
 * <h2>Examples</h2>
 * <h3>Offset and Term Consistency</h3>
 * Given post-CharFilter text {@code "state-of-the-art"} and a tokenizer configured with
 * {@code keepHyphens=true}, a single token might be emitted:
 * <pre>{@code
 * term = "state-of-the-art"
 * start = 0, end = 17
 * assert term.equals(source.substring(start, end));
 * }</pre>
 * If {@code keepHyphens=false}, multiple tokens are emitted; all tokens must still satisfy
 * {@code term.equals(source.substring(start, end))}.
 *
 * <h3>Newline Precedence</h3>
 * For input {@code "foo\\t  bar\\n  baz"} with a standard CharFilter that normalizes Unicode spaces,
 * preserves newlines, and collapses non-newline whitespace, the normalized text is
 * {@code "foo bar\\n baz"}.
 *
 * <h3>Combining Marks and Surrogates</h3>
 * For {@code "a\u0301 🚀"} (a + combining acute, then U+1F680 ROCKET), a tokenizer that emits
 * alphabetic tokens only should produce a single token covering {@code "a\u0301"} and must not
 * split the rocket surrogate pair.
 *
 * <h2>Thread-Safety</h2>
 * All analysis components in this package are stateless and thread-safe by contract.
 *
 * <h2>Testing Guidance</h2>
 * Implementations should be verified against:
 * <ul>
 *   <li>Determinism: same input + options ⇒ identical outputs.</li>
 *   <li>Idempotence: CharFilter applied twice equals once.</li>
 *   <li>Offset correctness: substring equality for every token.</li>
 *   <li>Grapheme safety: no surrogate or base+mark splits.</li>
 *   <li>Precedence: newline handling before whitespace collapsing.</li>
 * </ul>
 */
package com.jfi.core.analysis;