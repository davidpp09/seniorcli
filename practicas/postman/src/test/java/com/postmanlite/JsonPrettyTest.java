package com.postmanlite;

import com.postmanlite.service.JsonPretty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonPrettyTest {

    @Test
    void formatsObjectsAndArrays() {
        assertEquals("{\n  \"a\": 1,\n  \"b\": [\n    1,\n    2\n  ]\n}", JsonPretty.format("{\"a\":1,\"b\":[1,2]}"));
    }

    @Test
    void doesNotTouchSpecialCharsInsideStrings() {
        assertEquals("{\n  \"k\": \"a,b:{c}\\\"d\"\n}", JsonPretty.format("{\"k\":\"a,b:{c}\\\"d\"}"));
    }

    @Test
    void keepsEmptyContainersCompact() {
        assertEquals("{\n  \"a\": {},\n  \"b\": []\n}", JsonPretty.format("{\"a\":{},\"b\":[ ]}"));
    }

    @Test
    void returnsNonJsonUntouched() {
        assertEquals("hola mundo", JsonPretty.format("hola mundo"));
    }
}
