package com.avh.practicas.shared.pattern.singleton;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigProgramaTest {

    @Test
    void constructor_guardaValores_validos() {
        ConfigPrograma cfg = new ConfigPrograma(10, 3);

        assertEquals(10, cfg.getMaxLom());
        assertEquals(3, cfg.getConfigPrograma());
    }

    @Test
    void constructor_rechazaNegativos() {
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> new ConfigPrograma(-1, 0));
        assertTrue(ex1.getMessage().contains("maxLom"));

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> new ConfigPrograma(0, -1));
        assertTrue(ex2.getMessage().contains("configPrograma"));
    }

    @Test
    void equals_hashCode_contratosBasicos() {
        ConfigPrograma a1 = new ConfigPrograma(5, 2);
        ConfigPrograma a2 = new ConfigPrograma(5, 2);
        ConfigPrograma b = new ConfigPrograma(6, 2);

        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
        assertNotEquals(a1, b);
        assertNotEquals(a1, null);
    }
}

