package io.nthcristian.zplrdr.labelary;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.nthcristian.zplrdr.document.PdfDocument;
import io.nthcristian.zplrdr.document.ZplDocument;
import io.nthcristian.zplrdr.error.ConversionProviderException;
import io.nthcristian.zplrdr.labelary.util.LabelaryClientConfig;
import io.nthcristian.zplrdr.preset.util.Preset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("LabelaryConversionProvider")
class LabelaryConversionProviderTest {

        private static final Preset DUMMY_PRESET = new Preset("test",
                        Map.of("dpmm", "8dpmm", "width", "5.9", "height", "3.9"));

        private static final String UNREACHABLE_URL = "http://127.0.0.1:1/v1/printers/{dpmm}/labels/{width}x{height}/";

        @Test
        @DisplayName("should construct with valid config")
        void shouldConstructWithValidConfig() {
                var config = new LabelaryClientConfig(
                                "http://localhost:8080/v1/printers/{dpmm}/labels/{width}x{height}/", null);
                assertDoesNotThrow(() -> new LabelaryConversionProvider(config));
        }

        @Test
        @DisplayName("should construct with no-arg constructor using default Labelary URL")
        void shouldConstructWithNoArgConstructor() {
                assertDoesNotThrow(() -> new LabelaryConversionProvider());
        }

        @Nested
        @DisplayName("error paths — unreachable API")
        class UnreachableApi {

                @Test
                @DisplayName("should return ConversionProviderException for unreachable API (connection refused)")
                void shouldThrowOnUnreachableApi() {
                        var zpl = "^XA\n^FDTest^FS\n^XZ";
                        var doc = new ZplDocument(zpl.getBytes(StandardCharsets.UTF_8));
                        var provider = new LabelaryConversionProvider(
                                        new LabelaryClientConfig(UNREACHABLE_URL, null));

                        var ex = assertThrows(ConversionProviderException.class,
                                        () -> provider.convert(new ZplDocument[] { doc }, DUMMY_PRESET));
                        assertNotNull(ex.getMessage());
                }

                @Test
                @DisplayName("should not throw during batch splitting for multiple labels")
                void shouldHandleMultipleLabelsWithoutBatchSplittingError() {
                        var zpl = "^XA\n^FDLabel1^FS\n^XZ^XA\n^FDLabel2^FS\n^XZ^XA\n^FDLabel3^FS\n^XZ";
                        var doc = new ZplDocument(zpl.getBytes(StandardCharsets.UTF_8));
                        var provider = new LabelaryConversionProvider(
                                        new LabelaryClientConfig(UNREACHABLE_URL, null));

                        var ex = assertThrows(ConversionProviderException.class,
                                        () -> provider.convert(new ZplDocument[] { doc }, DUMMY_PRESET));
                        assertNotNull(ex);
                }

                @Test
                @DisplayName("should handle whitespace between labels without error in splitting")
                void shouldHandleWhitespaceBetweenLabels() {
                        var zpl = "^XA\n^FDLabel1^FS\n^XZ\n\n\n^XA\n^FDLabel2^FS\n^XZ\n\n";
                        var doc = new ZplDocument(zpl.getBytes(StandardCharsets.UTF_8));
                        var provider = new LabelaryConversionProvider(
                                        new LabelaryClientConfig(UNREACHABLE_URL, null));

                        var ex = assertThrows(ConversionProviderException.class,
                                        () -> provider.convert(new ZplDocument[] { doc }, DUMMY_PRESET));
                        assertNotNull(ex);
                }
        }

        @Nested
        @DisplayName("bad presets")
        class BadPresets {

                @Test
                @DisplayName("should throw ConversionProviderException when dpmm property is missing from preset")
                void shouldThrowNpeWhenDpmmMissing() {
                        var doc = new ZplDocument("^XA^XZ".getBytes(StandardCharsets.UTF_8));
                        var provider = new LabelaryConversionProvider(
                                        new LabelaryClientConfig(UNREACHABLE_URL, null));
                        var badPreset = new Preset("bad", Map.of("width", "5.9", "height", "3.9"));

                        // preset.getProperty("dpmm") returns null,
                        // config.baseUrl().replace("{dpmm}", null) throws NullPointerException
                        assertThrows(ConversionProviderException.class,
                                        () -> provider.convert(new ZplDocument[] { doc }, badPreset));
                }

                @Test
                @DisplayName("should throw ConversionProviderException when width property is missing from preset")
                void shouldThrowNpeWhenWidthMissing() {
                        var doc = new ZplDocument("^XA^XZ".getBytes(StandardCharsets.UTF_8));
                        var provider = new LabelaryConversionProvider(
                                        new LabelaryClientConfig(UNREACHABLE_URL, null));
                        var badPreset = new Preset("bad", Map.of("dpmm", "8dpmm", "height", "3.9"));

                        assertThrows(ConversionProviderException.class,
                                        () -> provider.convert(new ZplDocument[] { doc }, badPreset));
                }

                @Test
                @DisplayName("should throw ConversionProviderException when height property is missing from preset")
                void shouldThrowNpeWhenHeightMissing() {
                        var doc = new ZplDocument("^XA^XZ".getBytes(StandardCharsets.UTF_8));
                        var provider = new LabelaryConversionProvider(
                                        new LabelaryClientConfig(UNREACHABLE_URL, null));
                        var badPreset = new Preset("bad", Map.of("dpmm", "8dpmm", "width", "5.9"));

                        assertThrows(ConversionProviderException.class,
                                        () -> provider.convert(new ZplDocument[] { doc }, badPreset));
                }

                @Test
                @DisplayName("should throw ConversionProviderException when all preset properties are missing")
                void shouldThrowNpeWhenAllPropertiesMissing() {
                        var doc = new ZplDocument("^XA^XZ".getBytes(StandardCharsets.UTF_8));
                        var provider = new LabelaryConversionProvider(
                                        new LabelaryClientConfig(UNREACHABLE_URL, null));
                        var badPreset = new Preset("empty", Map.of());

                        assertThrows(ConversionProviderException.class,
                                        () -> provider.convert(new ZplDocument[] { doc }, badPreset));
                }

                @Test
                @DisplayName("should accept preset with unexpected extra fields")
                void shouldAcceptPresetWithExtraFields() {
                        var doc = new ZplDocument("^XA^XZ".getBytes(StandardCharsets.UTF_8));
                        var provider = new LabelaryConversionProvider(
                                        new LabelaryClientConfig(UNREACHABLE_URL, null));
                        var presetWithExtra = new Preset("extra",
                                        Map.of("dpmm", "8dpmm", "width", "5.9", "height", "3.9", "color", "red"));

                        // Should not throw during URL construction — fails at HTTP level
                        var ex = assertThrows(ConversionProviderException.class,
                                        () -> provider.convert(new ZplDocument[] { doc }, presetWithExtra));
                        assertNotNull(ex);
                }

                @Test
                @DisplayName("should accept preset with unusual but valid dimension values")
                void shouldAcceptPresetWithUnusualDimensions() {
                        var doc = new ZplDocument("^XA^XZ".getBytes(StandardCharsets.UTF_8));
                        var provider = new LabelaryConversionProvider(
                                        new LabelaryClientConfig(UNREACHABLE_URL, null));
                        var unusualPreset = new Preset("unusual",
                                        Map.of("dpmm", "24dpmm", "width", "100", "height", "0.1"));

                        var ex = assertThrows(ConversionProviderException.class,
                                        () -> provider.convert(new ZplDocument[] { doc }, unusualPreset));
                        assertNotNull(ex);
                }
        }

        @Nested
        @DisplayName("invalid ZPL")
        class InvalidZpl {

                @Test
                @DisplayName("should return empty result for empty ZPL document")
                void shouldReturnEmptyResultForEmptyZpl() throws Exception {
                        var doc = new ZplDocument(new byte[0]);
                        var provider = new LabelaryConversionProvider(
                                        new LabelaryClientConfig(UNREACHABLE_URL, null));

                        PdfDocument[] results = provider.convert(new ZplDocument[] { doc }, DUMMY_PRESET);

                        assertNotNull(results);
                        assertEquals(0, results.length, "empty ZPL should produce no batches and no PDFs");
                }

                @Test
                @DisplayName("should return empty result for ZPL with only whitespace")
                void shouldReturnEmptyResultForWhitespaceOnlyZpl() throws Exception {
                        var zpl = "\n\n\n   \n\n";
                        var doc = new ZplDocument(zpl.getBytes(StandardCharsets.UTF_8));
                        var provider = new LabelaryConversionProvider(
                                        new LabelaryClientConfig(UNREACHABLE_URL, null));

                        PdfDocument[] results = provider.convert(new ZplDocument[] { doc }, DUMMY_PRESET);

                        assertNotNull(results);
                        assertEquals(0, results.length, "whitespace-only ZPL should produce no labels");
                }

                @Test
                @DisplayName("should split ZPL without ^XA prefix but still send batches")
                void shouldHandleZplWithoutXaPrefix() {
                        // ZPL missing ^XA prefix — splitIntoBatches still works since it splits on ^XZ
                        var zpl = "^FDTest^FS\n^XZ";
                        var doc = new ZplDocument(zpl.getBytes(StandardCharsets.UTF_8));
                        var provider = new LabelaryConversionProvider(
                                        new LabelaryClientConfig(UNREACHABLE_URL, null));

                        var ex = assertThrows(ConversionProviderException.class,
                                        () -> provider.convert(new ZplDocument[] { doc }, DUMMY_PRESET));
                        assertNotNull(ex);
                }

                @Test
                @DisplayName("should handle ZPL with ^XZ that has no ^XA — single chunk")
                void shouldHandleSingleZplChunk() {
                        // Only ^XZ separator present, ^XA comes before it
                        var zpl = "^XA\n^FDTest^FS\n^XZ";
                        var doc = new ZplDocument(zpl.getBytes(StandardCharsets.UTF_8));
                        var provider = new LabelaryConversionProvider(
                                        new LabelaryClientConfig(UNREACHABLE_URL, null));

                        var ex = assertThrows(ConversionProviderException.class,
                                        () -> provider.convert(new ZplDocument[] { doc }, DUMMY_PRESET));
                        assertNotNull(ex);
                }

                @Test
                @DisplayName("should handle ZPL with only ^XZ delimiter but no ^XA")
                void shouldHandleZplWithOnlyXzDelimiter() {
                        var zpl = "^FDtest1^FS\n^XZ^FDtest2^FS\n^XZ";
                        var doc = new ZplDocument(zpl.getBytes(StandardCharsets.UTF_8));
                        var provider = new LabelaryConversionProvider(
                                        new LabelaryClientConfig(UNREACHABLE_URL, null));

                        var ex = assertThrows(ConversionProviderException.class,
                                        () -> provider.convert(new ZplDocument[] { doc }, DUMMY_PRESET));
                        assertNotNull(ex);
                }
        }

        @Nested
        @DisplayName("preset / ZPL combos via real API")
        class RealApiEdgeCases {

                @Test
                @DisplayName("should handle ZPL without proper content gracefully via real API")
                void shouldHandleZplWithoutContent() throws Exception {
                        var provider = new LabelaryConversionProvider();
                        var zpl = "^XA^XZ"; // minimal valid ZPL
                        var doc = new ZplDocument(zpl.getBytes(StandardCharsets.UTF_8));

                        PdfDocument[] results = provider.convert(new ZplDocument[] { doc }, DUMMY_PRESET);

                        assertNotNull(results);
                        assertTrue(results.length > 0, "should return at least one PDF document");
                        byte[] pdfData = results[0].data();
                        assertTrue(pdfData.length > 0, "PDF data should not be empty");
                        String header = new String(pdfData, 0, Math.min(pdfData.length, 5), StandardCharsets.UTF_8);
                        assertTrue(header.startsWith("%PDF"),
                                        "Expected PDF content starting with '%PDF', got: " + header);
                }
        }

        @Nested
        @DisplayName("integration — real Labelary API")
        class RealApi {

                @Test
                @DisplayName("should convert a single ZPL label to valid PDF via Labelary API")
                void shouldConvertSingleZplLabel() throws Exception {
                        var provider = new LabelaryConversionProvider();
                        var zpl = "^XA\n^FO50,50^ADN,36,20^FDHello, World!^FS\n^XZ";
                        var doc = new ZplDocument(zpl.getBytes(StandardCharsets.UTF_8));

                        PdfDocument[] results = provider.convert(new ZplDocument[] { doc }, DUMMY_PRESET);

                        assertNotNull(results);
                        assertTrue(results.length > 0, "should return at least one PDF document");
                        assertNotNull(results[0].data());
                        assertTrue(results[0].data().length > 0, "PDF data should not be empty");

                        // Verify PDF magic bytes
                        byte[] pdfData = results[0].data();
                        String header = new String(pdfData, 0, Math.min(pdfData.length, 5), StandardCharsets.UTF_8);
                        assertTrue(header.startsWith("%PDF"),
                                        "Expected PDF content starting with '%PDF', got: " + header);
                }
        }
}