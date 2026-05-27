package com.avh.practicas.shared.pattern.singleton;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class GestorConfiguracionTest {

    @BeforeEach
    void resetSingleton() throws Exception {
        Field f = GestorConfiguracion.class.getDeclaredField("instancia");
        f.setAccessible(true);
        f.set(null, null);
    }

    @Test
    void getInstancia_retornaLaMismaInstancia() {
        GestorConfiguracion a = GestorConfiguracion.getInstancia();
        GestorConfiguracion b = GestorConfiguracion.getInstancia();
        assertSame(a, b);
    }

    @Test
    void getInstancia_esThreadSafe_basico() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Callable<GestorConfiguracion>> tasks = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                tasks.add(GestorConfiguracion::getInstancia);
            }
            List<Future<GestorConfiguracion>> futures = pool.invokeAll(tasks);

            GestorConfiguracion ref = futures.get(0).get();
            for (Future<GestorConfiguracion> f : futures) {
                assertSame(ref, f.get());
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void set_get_recargar_y_obtenerConfiguraciones() {
        GestorConfiguracion gestor = GestorConfiguracion.getInstancia();

        gestor.setConfig("PROG1", new ConfigPrograma(10, 1));
        assertEquals(new ConfigPrograma(10, 1), gestor.getConfig("PROG1"));

        gestor.recargarParametros("PROG1", 20, 2);
        assertEquals(new ConfigPrograma(20, 2), gestor.getConfig("PROG1"));

        Map<String, ConfigPrograma> snap = gestor.obtenerConfiguraciones();
        assertEquals(1, snap.size());
        assertThrows(UnsupportedOperationException.class, () -> snap.put("X", new ConfigPrograma(1, 1)));
    }

    @Test
    void validaPrograma_y_configNoNula() {
        GestorConfiguracion gestor = GestorConfiguracion.getInstancia();

        assertThrows(IllegalArgumentException.class, () -> gestor.getConfig(null));
        assertThrows(IllegalArgumentException.class, () -> gestor.getConfig(" "));
        assertThrows(NullPointerException.class, () -> gestor.setConfig("PROG1", null));
    }
}

