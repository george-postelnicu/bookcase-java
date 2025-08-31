package ro.georgepostelnicu.app.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringUtilTest {

    @Test
    void splitCapitalizeAndJoin_handlesSingleAndMultipleWords() {
        assertEquals("Hello", StringUtil.splitCapitalizeAndJoin("hello"));
        assertEquals("Hello World", StringUtil.splitCapitalizeAndJoin("hello world"));
        assertEquals("Hello  World", StringUtil.splitCapitalizeAndJoin("hello  world"));
        assertEquals("A B", StringUtil.splitCapitalizeAndJoin("a b"));
    }
}
