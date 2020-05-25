package dev.zackschw.boosttorrent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BitvectorTest {

    @Test
    void setBit() {
        Bitvector bitvector = new Bitvector(25);
        bitvector.setBit(0);
        bitvector.setBit(3);
        bitvector.setBit(7);
        bitvector.setBit(8);
        bitvector.setBit(24);

        assertEquals(0b10010001, bitvector.toByteArray()[0] & 0xff);
        assertEquals(0b10000000, bitvector.toByteArray()[1] & 0xff);
        assertEquals(0, bitvector.toByteArray()[2] & 0xff);
        assertEquals(0b10000000, bitvector.toByteArray()[3] & 0xff);
    }

    @Test
    void unsetBit() {
        byte[] bytes = new byte[] { 127, 127, 127 };
        Bitvector bitvector = new Bitvector(24, bytes);

        bitvector.unsetBit(1);
        bitvector.unsetBit(7);
        bitvector.unsetBit(8);
        bitvector.unsetBit(23);

        assertEquals(0b00111110, bitvector.toByteArray()[0] & 0xff);
        assertEquals(0b01111111, bitvector.toByteArray()[1] & 0xff);
        assertEquals(0b01111110, bitvector.toByteArray()[2] & 0xff);
    }

    @Test
    void isSet() {
        byte[] bytes = new byte[] { 127, 127, 127 };
        Bitvector bitvector = new Bitvector(24, bytes);

        assertFalse(bitvector.isSet(0));
        assertTrue(bitvector.isSet(1));
        assertTrue(bitvector.isSet(7));
        assertFalse(bitvector.isSet(8));
        assertTrue(bitvector.isSet(23));
    }

    @Test
    void isEmpty() {
        byte[] b1 = new byte[] {0, 0, 9};
        byte[] b2 = new byte[] {0, 0, 0};

        Bitvector bitvector1 = new Bitvector(24, b1);
        Bitvector bitvector2 = new Bitvector(17, b2);

        assertFalse(bitvector1.isEmpty());
        assertTrue(bitvector2.isEmpty());
    }

    @Test
    public void testLength() {
        Bitvector bitvector = new Bitvector(25);
        boolean caught = false;

        try {
            bitvector.setBit(-1);
        } catch (IllegalArgumentException e) {
            caught = true;
        }

        assertTrue(caught);

        caught = false;
        try {
            bitvector.setBit(25);
        } catch (IllegalArgumentException e) {
            caught = true;
        }

        assertTrue(caught);

        caught = false;
        byte[] bytes = new byte[3];
        try {
            Bitvector bitvector2 = new Bitvector(25, bytes);
        } catch (IllegalArgumentException e) {
            caught = true;
        }

        assertTrue(caught);
    }

}