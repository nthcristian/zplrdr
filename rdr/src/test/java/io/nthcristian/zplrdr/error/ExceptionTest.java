package io.nthcristian.zplrdr.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Exception classes")
class ExceptionTest {

    @Nested
    @DisplayName("ConversionProviderException")
    class ConversionProviderExceptionTests {

        @Test
        @DisplayName("should store message")
        void shouldStoreMessage() {
            var ex = new ConversionProviderException("test message");
            assertEquals("test message", ex.getMessage());
        }

        @Test
        @DisplayName("should store message and cause")
        void shouldStoreMessageAndCause() {
            var cause = new RuntimeException("root cause");
            var ex = new ConversionProviderException("test message", cause);

            assertEquals("test message", ex.getMessage());
            assertEquals(cause, ex.getCause());
        }

        @Test
        @DisplayName("should be instance of Exception")
        void shouldBeInstanceOfException() {
            var ex = new ConversionProviderException("msg");
            assertInstanceOf(Exception.class, ex);
        }
    }

    @Nested
    @DisplayName("PresetSchemaException")
    class PresetSchemaExceptionTests {

        @Test
        @DisplayName("should store message")
        void shouldStoreMessage() {
            var ex = new PresetSchemaException("validation failed");
            assertEquals("validation failed", ex.getMessage());
        }

        @Test
        @DisplayName("should store message and cause")
        void shouldStoreMessageAndCause() {
            var cause = new IllegalArgumentException("bad arg");
            var ex = new PresetSchemaException("validation failed", cause);

            assertEquals("validation failed", ex.getMessage());
            assertEquals(cause, ex.getCause());
        }

        @Test
        @DisplayName("should be instance of Exception")
        void shouldBeInstanceOfException() {
            var ex = new PresetSchemaException("msg");
            assertInstanceOf(Exception.class, ex);
        }
    }

    @Nested
    @DisplayName("PresetServiceException")
    class PresetServiceExceptionTests {

        @Test
        @DisplayName("should store message")
        void shouldStoreMessage() {
            var ex = new PresetServiceException("service error");
            assertEquals("service error", ex.getMessage());
        }

        @Test
        @DisplayName("should store message and cause")
        void shouldStoreMessageAndCause() {
            var cause = new IllegalStateException("state error");
            var ex = new PresetServiceException("service error", cause);

            assertEquals("service error", ex.getMessage());
            assertEquals(cause, ex.getCause());
        }

        @Test
        @DisplayName("should be instance of Exception")
        void shouldBeInstanceOfException() {
            var ex = new PresetServiceException("msg");
            assertInstanceOf(Exception.class, ex);
        }
    }

    @Nested
    @DisplayName("PresetStorageException")
    class PresetStorageExceptionTests {

        @Test
        @DisplayName("should store message")
        void shouldStoreMessage() {
            var ex = new PresetStorageException("storage error");
            assertEquals("storage error", ex.getMessage());
        }

        @Test
        @DisplayName("should store message and cause")
        void shouldStoreMessageAndCause() {
            var cause = new java.io.IOException("disk full");
            var ex = new PresetStorageException("storage error", cause);

            assertEquals("storage error", ex.getMessage());
            assertEquals(cause, ex.getCause());
        }

        @Test
        @DisplayName("should be instance of Exception")
        void shouldBeInstanceOfException() {
            var ex = new PresetStorageException("msg");
            assertInstanceOf(Exception.class, ex);
        }
    }

    @Nested
    @DisplayName("ZplConverterException")
    class ZplConverterExceptionTests {

        @Test
        @DisplayName("should store message")
        void shouldStoreMessage() {
            var ex = new ZplConverterException("conversion error");
            assertEquals("conversion error", ex.getMessage());
        }

        @Test
        @DisplayName("should store message and cause")
        void shouldStoreMessageAndCause() {
            var cause = new RuntimeException("underlying");
            var ex = new ZplConverterException("conversion error", cause);

            assertEquals("conversion error", ex.getMessage());
            assertEquals(cause, ex.getCause());
        }

        @Test
        @DisplayName("should be instance of Exception")
        void shouldBeInstanceOfException() {
            var ex = new ZplConverterException("msg");
            assertInstanceOf(Exception.class, ex);
        }
    }

    @Nested
    @DisplayName("Exception wrapping patterns")
    class ExceptionWrapping {

        @Test
        @DisplayName("should support chaining across exception types")
        void shouldSupportChainingAcrossTypes() {
            var ioEx = new java.io.IOException("disk full");
            var storageEx = new PresetStorageException("failed to save", ioEx);
            var serviceEx = new PresetServiceException("save operation failed", storageEx);

            assertEquals("save operation failed", serviceEx.getMessage());
            assertInstanceOf(PresetStorageException.class, serviceEx.getCause());
            assertInstanceOf(java.io.IOException.class, serviceEx.getCause().getCause());
        }

        @Test
        @DisplayName("should support message-only construction without cause")
        void shouldSupportMessageOnlyConstructionWithoutCause() {
            var ex = new PresetServiceException("just a message");
            assertEquals("just a message", ex.getMessage());
            assertNull(ex.getCause());
        }
    }
}