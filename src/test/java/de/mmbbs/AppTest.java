package de.mmbbs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for simple App.
 */
class AppTest {
    /**
     * Rigorous Test.
     */
    @Test
    void testAdd() {
        assertEquals(7,App.add(5, 2)," 5+2 sollte 7 ergeben!");
        assertEquals(10,App.add(12, -2)," 12-2 sollte 10 ergeben!");
    }
}
