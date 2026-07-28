package io.nthcristian.zplrdr.labelary;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

        // Unreachable URL ensures deterministic IOException ->
        // ConversionProviderException
        private static final String UNREACHABLE_URL = "http://127.0.0.1:1/v1/printers/{dpmm}/labels/{width}x{height}/";

        @Test
        @DisplayName("should construct with valid config")
        void shouldConstructWithValidConfig() {
                var config = new LabelaryClientConfig(
                                "http://localhost:8080/v1/printers/{dpmm}/labels/{width}x{height}/", null);
                assertDoesNotThrow(() -> new LabelaryConversionProvider(config));
        }

        @Nested
        @DisplayName("splitIntoBatches and convert pipeline")
        class Pipeline {

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

                        // Should throw from HTTP layer, not from splitIntoBatches
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
}