package io.nthcristian.zplrdr;

import static org.junit.jupiter.api.Assertions.*;

import io.nthcristian.zplrdr.contract.ConversionProvider;
import io.nthcristian.zplrdr.document.PdfDocument;
import io.nthcristian.zplrdr.document.ZplDocument;
import io.nthcristian.zplrdr.error.ConversionProviderException;
import io.nthcristian.zplrdr.error.ZplConverterException;
import io.nthcristian.zplrdr.preset.util.Preset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@DisplayName("ZplConverter")
class ZplConverterTest {

    private static final byte[] VALID_ZPL = "^XA\n^FO50,50^ADN,36,20^FDHello, World!^FS\n^XZ".getBytes();
    private static final byte[] INVALID_ZPL_NO_START = "FO50,50^ADN,36,20^FDHello^FS\n^XZ".getBytes();
    private static final byte[] INVALID_ZPL_NO_END = "^XA\n^FO50,50^ADN,36,20^FDHello^FS\n".getBytes();
    private static final Preset DUMMY_PRESET = new Preset("test",
            Map.of("dpmm", "8dpmm", "width", "5.9", "height", "3.9"));

    /**
     * A conversion provider that simply echoes each ZplDocument as a PDF with the
     * same bytes.
     */
    private static class EchoConversionProvider implements ConversionProvider {
        @Override
        public PdfDocument[] convert(ZplDocument[] zplFiles, Preset preset) throws ConversionProviderException {
            PdfDocument[] result = new PdfDocument[zplFiles.length];
            for (int i = 0; i < zplFiles.length; i++) {
                result[i] = new PdfDocument(zplFiles[i].data());
            }
            return result;
        }
    }

    /** A conversion provider that always throws. */
    private static class FailingConversionProvider implements ConversionProvider {
        @Override
        public PdfDocument[] convert(ZplDocument[] zplFiles, Preset preset) throws ConversionProviderException {
            throw new ConversionProviderException("simulated failure");
        }
    }

    @Nested
    @DisplayName("convertAll")
    class ConvertAll {

        @Test
        @DisplayName("should convert a single valid ZPL file")
        void shouldConvertSingleValidZpl() throws Exception {
            ZplConverter converter = new ZplConverter(new EchoConversionProvider());
            InputStream[] files = { new ByteArrayInputStream(VALID_ZPL) };

            PdfDocument[] results = converter.convertAll(files, DUMMY_PRESET);

            assertEquals(1, results.length);
            assertArrayEquals(VALID_ZPL, results[0].data());
        }

        @Test
        @DisplayName("should convert multiple valid ZPL files")
        void shouldConvertMultipleValidZpl() throws Exception {
            ZplConverter converter = new ZplConverter(new EchoConversionProvider());
            byte[] zpl2 = "^XA\n^FO100,100^FS\n^XZ".getBytes();
            InputStream[] files = {
                    new ByteArrayInputStream(VALID_ZPL),
                    new ByteArrayInputStream(zpl2)
            };

            PdfDocument[] results = converter.convertAll(files, DUMMY_PRESET);

            assertEquals(2, results.length);
            assertArrayEquals(VALID_ZPL, results[0].data());
            assertArrayEquals(zpl2, results[1].data());
        }

        @Test
        @DisplayName("should return empty array for empty input")
        void shouldReturnEmptyArrayForEmptyInput() throws Exception {
            ZplConverter converter = new ZplConverter(new EchoConversionProvider());

            PdfDocument[] results = converter.convertAll(new InputStream[0], DUMMY_PRESET);

            assertEquals(0, results.length);
        }

        @Test
        @DisplayName("should wrap ConversionProviderException in ZplConverterException")
        void shouldWrapConversionProviderException() {
            ZplConverter converter = new ZplConverter(new FailingConversionProvider());
            InputStream[] files = { new ByteArrayInputStream(VALID_ZPL) };

            ZplConverterException ex = assertThrows(ZplConverterException.class,
                    () -> converter.convertAll(files, DUMMY_PRESET));
            assertTrue(ex.getMessage().contains("Could not convert files"));
            assertInstanceOf(ConversionProviderException.class, ex.getCause());
        }
    }

    @Nested
    @DisplayName("format validation")
    class FormatValidation {

        @Test
        @DisplayName("should reject file not starting with ^XA")
        void shouldRejectFileNotStartingWithXa() {
            ZplConverter converter = new ZplConverter(new EchoConversionProvider());
            InputStream[] files = { new ByteArrayInputStream(INVALID_ZPL_NO_START) };

            ZplConverterException ex = assertThrows(ZplConverterException.class,
                    () -> converter.convertAll(files, DUMMY_PRESET));
            assertTrue(ex.getMessage().contains("not a valid ZPL document"));
        }

        @Test
        @DisplayName("should reject file not ending with ^XZ")
        void shouldRejectFileNotEndingWithXz() {
            ZplConverter converter = new ZplConverter(new EchoConversionProvider());
            InputStream[] files = { new ByteArrayInputStream(INVALID_ZPL_NO_END) };

            ZplConverterException ex = assertThrows(ZplConverterException.class,
                    () -> converter.convertAll(files, DUMMY_PRESET));
            assertTrue(ex.getMessage().contains("not a valid ZPL document"));
        }

        @Test
        @DisplayName("should reject first invalid file even when others would be valid")
        void shouldRejectFirstInvalidEvenWhenOthersValid() {
            ZplConverter converter = new ZplConverter(new EchoConversionProvider());
            InputStream[] files = {
                    new ByteArrayInputStream(INVALID_ZPL_NO_START),
                    new ByteArrayInputStream(VALID_ZPL)
            };

            assertThrows(ZplConverterException.class,
                    () -> converter.convertAll(files, DUMMY_PRESET));
        }

        @Test
        @DisplayName("should accept file containing ^XA and ^XZ with content in between")
        void shouldAcceptFileWithContentBetweenTags() {
            ZplConverter converter = new ZplConverter(new EchoConversionProvider());
            String zpl = "^XA\n^FO100,100^ADN,36,20^FDTest^FS\nmore content\n^XZ";
            InputStream[] files = { new ByteArrayInputStream(zpl.getBytes(StandardCharsets.UTF_8)) };

            assertDoesNotThrow(() -> converter.convertAll(files, DUMMY_PRESET));
        }

        @Test
        @DisplayName("should accept minimal ZPL with only start and end tags")
        void shouldAcceptMinimalZpl() {
            ZplConverter converter = new ZplConverter(new EchoConversionProvider());
            InputStream[] files = { new ByteArrayInputStream("^XA^XZ".getBytes(StandardCharsets.UTF_8)) };

            assertDoesNotThrow(() -> converter.convertAll(files, DUMMY_PRESET));
        }
    }

    @Nested
    @DisplayName("IOException handling")
    class IOExceptionHandling {

        @Test
        @DisplayName("should wrap IOException from input stream in ZplConverterException")
        void shouldWrapIOException() {
            ZplConverter converter = new ZplConverter(new EchoConversionProvider());
            InputStream failingStream = new InputStream() {
                @Override
                public int read() throws java.io.IOException {
                    throw new java.io.IOException("simulated read failure");
                }
            };
            InputStream[] files = { failingStream };

            ZplConverterException ex = assertThrows(ZplConverterException.class,
                    () -> converter.convertAll(files, DUMMY_PRESET));
            assertTrue(ex.getMessage().contains("Failed to read input stream"));
            assertInstanceOf(java.io.IOException.class, ex.getCause());
        }
    }
}