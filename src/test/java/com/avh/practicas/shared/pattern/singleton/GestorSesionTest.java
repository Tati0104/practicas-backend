package com.avh.practicas.shared.pattern.singleton;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class GestorSesionTest {

    @BeforeEach
    void resetSingleton() throws Exception {
        Field f = GestorSesion.class.getDeclaredField("instancia");
        f.setAccessible(true);
        f.set(null, null);
    }

    @Test
    void getInstancia_retornaLaMismaInstancia() {
        GestorSesion a = GestorSesion.getInstancia();
        GestorSesion b = GestorSesion.getInstancia();
        assertSame(a, b);
    }

    @Test
    void getInstancia_esThreadSafe_basico() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Callable<GestorSesion>> tasks = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                tasks.add(GestorSesion::getInstancia);
            }
            List<Future<GestorSesion>> futures = pool.invokeAll(tasks);

            GestorSesion ref = futures.get(0).get();
            for (Future<GestorSesion> f : futures) {
                assertSame(ref, f.get());
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void porDefecto_esInvitado_y_scopePublico() {
        GestorSesion sesion = GestorSesion.getInstancia();
        assertEquals("INVITADO", sesion.getRol());
        assertEquals("PUBLICO", sesion.getScope());
    }

    @Test
    void iniciarSesion_simple_seteaRolYSCOPE_porDefecto() {
        GestorSesion sesion = GestorSesion.getInstancia();
        sesion.iniciarSesion("tati");

        assertEquals("USER", sesion.getRol());
        assertEquals("APP", sesion.getScope());
    }

    @Test
    void iniciarSesion_completo_normalizaRolYScope() {
        GestorSesion sesion = GestorSesion.getInstancia();
        sesion.iniciarSesion("tati", "admin", "app");

        assertEquals("ADMIN", sesion.getRol());
        assertEquals("APP", sesion.getScope());
    }

    @Test
    void cerrarSesion_restableceAInvitadoPublico() {
        GestorSesion sesion = GestorSesion.getInstancia();
        sesion.iniciarSesion("tati", "ADMIN", "APP");
        sesion.cerrarSesion();

        assertEquals("INVITADO", sesion.getRol());
        assertEquals("PUBLICO", sesion.getScope());
    }

    @Test
    void tienePermiso_enPublico_soloLectura() {
        GestorSesion sesion = GestorSesion.getInstancia(); // PUBLICO por defecto

        assertTrue(sesion.tienePermiso("MOD", "LECTURA"));
        assertTrue(sesion.tienePermiso("MOD", " lectura "));
        assertFalse(sesion.tienePermiso("MOD", "CREAR"));
        assertFalse(sesion.tienePermiso("MOD", "ELIMINAR"));
    }

    @Test
    void tienePermiso_fueraDePublico_dependeDeJerarquiaRol() {
        GestorSesion sesion = GestorSesion.getInstancia();
        sesion.iniciarSesion("tati", "USER", "APP");
        assertTrue(sesion.tienePermiso("MOD", "CREAR"));
        assertFalse(sesion.tienePermiso("MOD", "EDITAR"));

        sesion.iniciarSesion("tati", "EDITOR", "APP");
        assertTrue(sesion.tienePermiso("MOD", "EDITAR"));
        assertFalse(sesion.tienePermiso("MOD", "ELIMINAR"));

        sesion.iniciarSesion("tati", "ADMIN", "APP");
        assertTrue(sesion.tienePermiso("MOD", "ELIMINAR"));
        assertTrue(sesion.tienePermiso("MOD", "ACCION_DESCONOCIDA")); // default -> requiere ADMIN
    }

    @Test
    void validaciones_de_parametros_obligatorios() {
        GestorSesion sesion = GestorSesion.getInstancia();

        assertThrows(IllegalArgumentException.class, () -> sesion.iniciarSesion(null));
        assertThrows(IllegalArgumentException.class, () -> sesion.iniciarSesion(" "));
        assertThrows(IllegalArgumentException.class, () -> sesion.iniciarSesion("tati", null, "APP"));
        assertThrows(IllegalArgumentException.class, () -> sesion.iniciarSesion("tati", "USER", " "));

        assertThrows(IllegalArgumentException.class, () -> sesion.tienePermiso(null, "LECTURA"));
        assertThrows(IllegalArgumentException.class, () -> sesion.tienePermiso("MOD", null));
    }
}

