# Java File Indexer

A command-line **search engine for your local files** — built from scratch in Java 21 with modern tooling.  
Think of it as a **mini-Lucene** you can actually read, extend, and use to search your notes, logs, markdown, or codebases.

[//]: # (![Build]&#40;https://github.com/AminSafari207/java-file-indexer/actions/workflows/ci.yml/badge.svg&#41;)

---

## Features (planned)

- **Crawl directories** and index files (`.txt`, `.md`, `.log`; later `.pdf`, `.docx` via pluggable extractors).
- **Analyze text** into normalized tokens (case-folding, Unicode-aware, stopword handling).
- **Inverted index**: map each term → postings list (documents + positions).
- **Query language** with Boolean ops, phrases, wildcards, and field filters:
  ```text
  error AND (timeout OR "connection reset") AND NOT archived
  report* AND 2024 AND ext:md AND path:logs/
  ```
- **Ranking** with TF-IDF or BM25.
- **Incremental re-index**: detect changed/added/removed files quickly.
- **Snippet highlighting** for query matches.
- Optional **tiny HTTP API** (`serve --port 7070`) to plug in demo UIs.

---

## High-Level Architecture

```
ContentExtractor  →  Analyzer  →  InvertedIndex  →  QueryParser  →  Searcher  →  IndexStore
```

- **ContentExtractor**: Reads text + metadata from files.
- **Analyzer**: CharFilters + Tokenizer pipeline.
- **InvertedIndex**: Core data structure (term → postings).
- **QueryParser**: Turns user query into AST.
- **Searcher**: Evaluates AST, ranks results.
- **IndexStore**: Saves/loads the index.

---

## Project Structure

```
jfi-core       → core interfaces (CharFilter, Tokenizer, Analyzer, Token)
jfi-analysis   → implementations of filters, tokenizers, analyzers
jfi-index      → inverted index data structures
jfi-io         → filesystem crawler + content extractors
jfi-search     → query parser + search engine
jfi-cli        → command-line interface
jfi-examples   → sample corpora and demos
```

---

## Requirements

- **JDK 21** (Temurin recommended)
- **Gradle 8.9+** (wrapper included)

---

## Build & Test

```bash
# clone and enter project
git clone https://github.com/AminSafari207/java-file-indexer.git
cd java-file-indexer

# build and run tests
./gradlew clean build
```

---

## 💻 Usage (future)

Once implemented, the CLI will support:

```bash
# index a directory
./gradlew :jfi-cli:run --args="index --dir notes/ --include '*.md'"

# query the index
./gradlew :jfi-cli:run --args="query 'error AND timeout' --top 20"

# refresh (incremental update)
./gradlew :jfi-cli:run --args="refresh"

# serve HTTP API on port 7070
./gradlew :jfi-cli:run --args="serve --port 7070"
```

---

## Roadmap

- [ ] Core analysis contracts (CharFilter, Tokenizer, Analyzer)
- [ ] Standard filters (whitespace, lowercase, ASCII fold, stopwords)
- [ ] WordTokenizer + StandardAnalyzer
- [ ] Inverted index (postings, BM25 scoring)
- [ ] Query parser + Boolean search
- [ ] Filesystem crawler + content extractors
- [ ] Incremental re-index
- [ ] CLI commands
- [ ] Snippet highlighting
- [ ] JMH benchmarks
- [ ] Optional HTTP API

---

## License

MIT — free to use, hack, and share.

---

## Contributing

Contributions are more than welcome.

If you’d like to help improve **java-file-indexer**:

- Fork the repository and create a branch.
- Submit a pull request with clear commit messages.
- Open issues to report bugs or suggest enhancements.
- Improve documentation, add tests, or propose new ideas.

Whether it’s fixing a typo, writing docs, or adding new features — every contribution counts.  
I’d be genuinely happy to collaborate with anyone interested!
