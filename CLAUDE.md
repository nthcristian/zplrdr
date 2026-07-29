# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Development Commands

```bash
# Build everything
./gradlew build

# Run all tests
./gradlew test

# Run tests for a single submodule
./gradlew :rdr:test
./gradlew :cli:test
./gradlew :prt:test

# Run a single test class
./gradlew :rdr:test --tests "io.nthcristian.zplrdr.ZplConverterTest"

# Run a specific test method
./gradlew :rdr:test --tests "io.nthcristian.zplrdr.ZplConverterTest.convertAllWithEchoProvider"

# Create a distribution (produces a runnable script in cli/build/install/)
./gradlew :cli:installDist

# Run the CLI directly (after build)
./gradlew :cli:run --args="convert --preset mypreset label.zpl"

# Clean build artifacts
./gradlew clean
```

**Target Java version:** 25 (set in each submodule's `build.gradle.kts` via `JavaLanguageVersion.of(25)`). The Gradle toolchain with `foojay-resolver-convention` auto-downloads the JDK.

**Gradle version:** 9.6.1 (wrapper checked in). Dependency versions are centrally managed in `gradle/libs.versions.toml`.

## Architecture

**zplrdr** converts ZPL (Zebra Programming Language) labels to PDF via the [Labelary API](http://api.labelary.com) and prints them on thermal label printers. It is a multi-module Gradle project with three submodules:

```
cli (application)  ──depends on──▶  :rdr, :prt
prt (java-library) ──depends on──▶  :rdr
rdr (java-library) ──standalone
```

### `:rdr` — Core library (`io.nthcristian.zplrdr`)

The core follows a **Strategy/SPI pattern** via two contracts:

- **`ConversionProvider`** (`contract/`) — converts `ZplDocument[]` → `PdfDocument[]` given a `Preset`. The sole implementation is `LabelaryConversionProvider`, which POSTs ZPL to the Labelary API, batches labels into groups of 50, and rate-limits to ≤3 requests/second.
- **`PresetSchema`** (`contract/`) — validates preset fields and provides defaults. The sole implementation is `LabelaryPresetSchema`, which validates `dpmm`, `width`, and `height`.

Key orchestration classes:
- **`ZplConverter`** — validates ZPL format (`^XA`/`^XZ` tags), then delegates to a `ConversionProvider`.
- **`PresetService`** — in-memory cache of named presets, persisted as JSON files via `PresetRepository` to `~/.local/share/zplrdr/`. Loads from disk on startup, writes on save. Silently skips invalid presets during cache init.

**Builder classes** (`builder/`) wire the production implementations together: `ZplConverterBuilder.build()` creates a `ZplConverter` with `LabelaryConversionProvider`; `PresetServiceBuilder.build()` creates a `PresetService` with `LabelaryPresetSchema` and file-based `PresetRepository`.

Exception hierarchy (all checked, extends `Exception`):
- `ZplConverterException` ← wraps converter-level errors
- `ConversionProviderException` ← wraps provider-level failures (HTTP errors, I/O)
- `PresetServiceException` ← wraps service-layer failures
- `PresetSchemaException` ← wraps validation failures
- `PresetStorageException` ← wraps persistence failures

### `:cli` — CLI application (`io.nthcristian.zplrdr.cli`)

Uses **picocli** for argument parsing. Entry point: `Main.java`. Subcommands:
- `convert` — ZPL files → PDF via a preset
- `print` — ZPL → PDF → print (single command)
- `print-pdf` — print existing PDF files
- `preset` — CRUD operations on named presets (subcommands: `list`, `create`, `show`, `set`, `delete`)
- `printers` — list available system printers

`CliSupport.java` is the shared utility that builds services, opens files, and coordinates conversion/writing.

### `:prt` — Print service library (`io.nthcristian.prt`)

Wraps the Java Print Service API for thermal label printing. **Not a CUPS client** — uses `javax.print` directly, falling back to the system's default print service.

- **`PrinterService`** — print `PdfDocument` objects (single or batch) to the default printer or a named printer. Loads PDFs via PDFBox, validates them, then submits print jobs with `LabelPrintLayout`.
- **`LabelPrintLayout`** — creates `Pageable` and `PrintRequestAttributeSet` tuned for direct-thermal label printers (full-bleed, monochrome, correct orientation). Hardcoded for 203 DPI printers like the Tomate MDK-006.

### Design Patterns

- **SPI/Strategy**: `ConversionProvider` and `PresetSchema` are pluggable interfaces.
- **Builder**: `ZplConverterBuilder` and `PresetServiceBuilder` hide wiring.
- **Repository**: `PresetRepository` abstracts file-based JSON persistence.
- **Records** (Java records): `PdfDocument`, `ZplDocument`, `ZplLabel`, `Preset`, `LabelaryClientConfig` are all immutable value types.
- **Template Method**: `AbstractPresetSchema` provides field-name/default-value management; subclasses supply `FieldDefinition` maps.

### Test Fixtures

`test-objects/` (gitignored) contains real ZPL label data and pre-converted PDF results for manual/visual verification. The ZPL data encodes 2-column product barcode labels using Aztec barcodes.

### Preset Format

Presets are stored as JSON files named `<name>.json` in `~/.local/share/zplrdr/`. Each file is a flat `Map<String, String>`. The Labelary schema requires three fields: `dpmm` (numeric, e.g. `"8"`), `width` (numeric in dots), `height` (numeric in dots).
