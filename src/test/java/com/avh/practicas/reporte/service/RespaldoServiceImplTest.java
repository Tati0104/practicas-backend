package com.avh.practicas.reporte.service;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.reporte.backup.pattern.abstractfactory.RespaldoFabricaExcel;
import com.avh.practicas.seguimiento.service.BitacoraService;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.enums.Scope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para validar las operaciones y patrones del servicio de respaldos (RespaldoServiceImpl).
 */
@ExtendWith(MockitoExtension.class)
class RespaldoServiceImplTest {

    @Mock
    private RespaldoFabricaExcel respaldoFabricaExcel;

    @Mock
    private BitacoraService bitacoraService;

    private RespaldoServiceImpl service;
    private Usuario mockUsuario;

    @BeforeEach
    void setUp() {
        service = new RespaldoServiceImpl(respaldoFabricaExcel, bitacoraService);

        mockUsuario = Usuario.builder()
                .nombre("Coordinador Test")
                .correo("coordinador@test.com")
                .rol(Rol.COORD_PRACTICA)
                .scope(Scope.PROGRAMA)
                .activo(true)
                .build();
        mockUsuario.setId(1L);
    }

    @Test
    void generarRespaldoAsync_Exitoso() throws Exception {
        // Arrange: Configurar fábrica abstracta para retornar datos de prueba para todas las hojas requeridas
        List<String> hojas = List.of(
                "Usuarios", "Estudiantes", "Expedientes con prácticas",
                "Empresas", "Vacantes", "Notas registradas",
                "Evaluaciones y encuestas", "Bitácora últimos 2 años"
        );

        for (String hoja : hojas) {
            when(respaldoFabricaExcel.obtenerEncabezados(hoja)).thenReturn(List.of("Header1", "Header2"));
            when(respaldoFabricaExcel.obtenerDatos(hoja)).thenReturn(List.of(
                    List.of("Valor1.1", "Valor1.2"),
                    List.of("Valor2.1", 12345.67)
            ));
        }

        // Act
        CompletableFuture<byte[]> future = service.generarRespaldoAsync(mockUsuario);
        byte[] bytes = future.get(); // Esperar resolución asíncrona

        // Assert
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        // Verificar que las hojas y sus datos fueron consultados a la fábrica
        for (String hoja : hojas) {
            verify(respaldoFabricaExcel, times(1)).obtenerEncabezados(hoja);
            verify(respaldoFabricaExcel, times(1)).obtenerDatos(hoja);
        }

        // Verificar logs de auditoría
        verify(bitacoraService, times(1)).registrar(
                eq("respaldo"), eq("GENERAR"), eq(mockUsuario), contains("Inicio")
        );
        verify(bitacoraService, times(1)).registrar(
                eq("respaldo"), eq("GENERAR"), eq(mockUsuario), contains("Finalización")
        );
    }

    @Test
    void generarRespaldoAsync_FallaPorErrorDeFabrica() throws Exception {
        // Arrange: Simular una excepción en la fábrica de Excel
        when(respaldoFabricaExcel.obtenerEncabezados(anyString())).thenThrow(new RuntimeException("Simulated Database Error"));

        // Act
        CompletableFuture<byte[]> future = service.generarRespaldoAsync(mockUsuario);

        // Assert
        assertThrows(Exception.class, future::get);

        // Verificar que se registró el error en la bitácora
        verify(bitacoraService, times(1)).registrar(
                eq("respaldo"), eq("GENERAR"), eq(mockUsuario), contains("Inicio")
        );
        verify(bitacoraService, times(1)).registrar(
                eq("respaldo"), eq("GENERAR"), eq(mockUsuario), contains("Error")
        );
    }

    @Test
    void guardarRespaldoLocal_Exitoso() {
        // Arrange
        byte[] dummyBytes = "Excel File Bytes Demo".getBytes();
        String testFilename = "test_respaldo_" + System.currentTimeMillis() + ".xlsx";
        File expectedFile = new File("backups", testFilename);

        // Asegurarse de que no exista previamente
        if (expectedFile.exists()) {
            expectedFile.delete();
        }

        // Act
        service.guardarRespaldoLocal(dummyBytes, testFilename, mockUsuario);

        // Assert
        assertTrue(expectedFile.exists());
        assertEquals(dummyBytes.length, expectedFile.length());

        // Limpiar archivo creado
        expectedFile.delete();

        // Verificar registro de auditoría
        verify(bitacoraService, times(1)).registrar(
                eq("respaldo"), eq("GUARDAR_LOCAL"), eq(mockUsuario), contains("guardado localmente")
        );
    }
}
