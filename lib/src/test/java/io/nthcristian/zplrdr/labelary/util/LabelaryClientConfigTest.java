package io.nthcristian.zplrdr.labelary.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("LabelaryClientConfig")
class LabelaryClientConfigTest {

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("should store baseUrl and apiKey")
        void shouldStoreBaseUrlAndApiKey() {
            var config = new LabelaryClientConfig("http://api.example.com", "secret-key");

            assertEquals("http://api.example.com", config.baseUrl());
            assertEquals("secret-key", config.apiKey());
        }

        @Test
        @DisplayName("should support null apiKey")
        void shouldSupportNullApiKey() {
            var config = new LabelaryClientConfig("http://api.example.com", null);

            assertEquals("http://api.example.com", config.baseUrl());
            assertNull(config.apiKey());
        }

        @Test
        @DisplayName("should support empty apiKey string")
        void shouldSupportEmptyApiKeyString() {
            var config = new LabelaryClientConfig("http://api.example.com", "");

            assertEquals("", config.apiKey());
        }
    }

    @Nested
    @DisplayName("record behavior")
    class RecordBehavior {

        @Test
        @DisplayName("should have value-based equality")
        void shouldHaveValueBasedEquality() {
            var config1 = new LabelaryClientConfig("http://api.example.com", "key");
            var config2 = new LabelaryClientConfig("http://api.example.com", "key");
            var config3 = new LabelaryClientConfig("http://other.example.com", "key");

            assertEquals(config1, config2);
            assertNotEquals(config1, config3);
        }

        @Test
        @DisplayName("should have consistent hashCode")
        void shouldHaveConsistentHashCode() {
            var config1 = new LabelaryClientConfig("http://api.example.com", "key");
            var config2 = new LabelaryClientConfig("http://api.example.com", "key");

            assertEquals(config1.hashCode(), config2.hashCode());
        }

        @Test
        @DisplayName("should have descriptive toString")
        void shouldHaveDescriptiveToString() {
            var config = new LabelaryClientConfig("http://api.example.com", "key");

            String str = config.toString();
            assertEquals(str, config.toString());
        }
    }
}