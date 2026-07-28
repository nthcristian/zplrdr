package io.nthcristian.zplrdr.document;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.nthcristian.zplrdr.labelary.document.ZplLabel;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Document records")
class DocumentTest {

    @Nested
    @DisplayName("PdfDocument")
    class PdfDocumentTests {

        @Test
        @DisplayName("should store byte array content")
        void shouldStoreByteArrayContent() {
            byte[] data = { 1, 2, 3, 4, 5 };
            var pdf = new PdfDocument(data);

            assertArrayEquals(data, pdf.data());
        }

        @Test
        @DisplayName("data field should be the same byte array reference passed in")
        void dataFieldShouldBeTheSameReference() {
            byte[] data = { 1, 2, 3 };
            var pdf = new PdfDocument(data);

            assertEquals(data, pdf.data());
        }

        @Test
        @DisplayName("should support empty byte array")
        void shouldSupportEmptyByteArray() {
            var pdf = new PdfDocument(new byte[0]);

            assertEquals(0, pdf.data().length);
        }

        @Test
        @DisplayName("should have toString that includes class name")
        void shouldHaveToString() {
            var pdf = new PdfDocument(new byte[] { 1, 2, 3 });
            String str = pdf.toString();

            assertEquals(str, pdf.toString());
        }
    }

    @Nested
    @DisplayName("ZplDocument")
    class ZplDocumentTests {

        @Test
        @DisplayName("should store ZPL content as bytes")
        void shouldStoreZplContentAsBytes() {
            String zpl = "^XA\n^FO50,50^ADN,36,20^FDTest^FS\n^XZ";
            byte[] data = zpl.getBytes(StandardCharsets.UTF_8);
            var doc = new ZplDocument(data);

            assertArrayEquals(data, doc.data());
        }

        @Test
        @DisplayName("data field should be the same byte array reference passed in")
        void dataFieldShouldBeTheSameReference() {
            byte[] data = "^XA^XZ".getBytes(StandardCharsets.UTF_8);
            var doc = new ZplDocument(data);

            assertEquals(data, doc.data());
        }
    }

    @Nested
    @DisplayName("ZplLabel")
    class ZplLabelTests {

        @Test
        @DisplayName("should store label content as bytes")
        void shouldStoreLabelContentAsBytes() {
            String label = "^XA\n^FO50,50^FDHello^FS\n^XZ";
            byte[] data = label.getBytes(StandardCharsets.UTF_8);
            var zplLabel = new ZplLabel(data);

            assertArrayEquals(data, zplLabel.data());
        }

        @Test
        @DisplayName("should support UTF-8 encoded ZPL content")
        void shouldSupportUtf8EncodedZplContent() {
            String label = "^XA\n^FDZPL Content^FS\n^XZ";
            var zplLabel = new ZplLabel(label.getBytes(StandardCharsets.UTF_8));

            String decoded = new String(zplLabel.data(), StandardCharsets.UTF_8);
            assertEquals(label, decoded);
        }
    }
}