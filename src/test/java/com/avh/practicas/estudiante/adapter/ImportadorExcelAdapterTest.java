package com.avh.practicas.estudiante.adapter;

import com.avh.practicas.configuracion.entity.Programa;
import com.avh.practicas.configuracion.repository.ProgramaRepository;
import com.avh.practicas.estudiante.dto.ResultadoImportacion;
import com.avh.practicas.estudiante.entity.EstadoAptitud;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportadorExcelAdapterTest {

    @Mock
    private EstudianteRepository estudianteRepository;
    @Mock
    private ProgramaRepository programaRepository;

    @InjectMocks
    private ImportadorExcelAdapter importador;

    @Test
    void importar_excelValido_retornaEstudianteExitoso() {
        when(estudianteRepository.existsByIdentificacion("123")).thenReturn(false);
        when(estudianteRepository.existsByCorreo("est@test.com")).thenReturn(false);
        when(programaRepository.findById(1L)).thenReturn(Optional.of(programa(true)));

        ResultadoImportacion resultado = importador.importar(excelConFila("123", "Estudiante", "est@test.com", "1", "8", "90", "4.0"));

        assertEquals(1, resultado.getTotal());
        assertEquals(1, resultado.getExitosos().size());
        assertTrue(resultado.getErrores().isEmpty());
        assertEquals(EstadoAptitud.SIN_EVALUAR, resultado.getExitosos().get(0).getEstadoAptitud());
    }

    @Test
    void importar_archivoSinCabecera_observaColumnaObligatoriaFaltante() {
        ResultadoImportacion resultado = importador.importar(excelVacio());

        assertEquals(0, resultado.getTotal());
        assertEquals(1, resultado.getErrores().size());
        assertTrue(resultado.getErrores().get(0).getDescripcion().contains("vac"));
    }

    @Test
    void importar_sinColumnaCorreo_retornaErrorDeCabecera() {
        ResultadoImportacion resultado = importador.importar(excelSinColumnaCorreo());

        assertEquals(0, resultado.getTotal());
        assertEquals(1, resultado.getErrores().size());
        assertTrue(resultado.getErrores().get(0).getDescripcion().contains("correo"));
    }

    @Test
    void importar_filaSinIdentificacion_retornaErrorDeFila() {
        ResultadoImportacion resultado = importador.importar(excelConFila("", "Estudiante", "est@test.com", "1", "8", "90", "4.0"));

        assertEquals(1, resultado.getTotal());
        assertTrue(resultado.getExitosos().isEmpty());
        assertEquals(2, resultado.getErrores().get(0).getFila());
        assertTrue(resultado.getErrores().get(0).getDescripcion().contains("obligatorio"));
    }

    @Test
    void importar_correoInvalido_retornaErrorDeFormato() {
        when(estudianteRepository.existsByIdentificacion("123")).thenReturn(false);
        when(estudianteRepository.existsByCorreo("correo-invalido")).thenReturn(false);
        when(programaRepository.findById(1L)).thenReturn(Optional.of(programa(true)));

        ResultadoImportacion resultado = importador.importar(excelConFila("123", "Estudiante", "correo-invalido", "1", "8", "90", "4.0"));

        assertTrue(resultado.getExitosos().isEmpty());
        assertTrue(resultado.getErrores().get(0).getDescripcion().contains("formato"));
    }

    @Test
    void importar_identificacionYaRegistrada_retornaError() {
        when(estudianteRepository.existsByIdentificacion("123")).thenReturn(true);
        when(estudianteRepository.existsByCorreo("est@test.com")).thenReturn(false);
        when(programaRepository.findById(1L)).thenReturn(Optional.of(programa(true)));

        ResultadoImportacion resultado = importador.importar(excelConFila("123", "Estudiante", "est@test.com", "1", "8", "90", "4.0"));

        assertTrue(resultado.getExitosos().isEmpty());
        assertTrue(resultado.getErrores().get(0).getDescripcion().contains("registrada"));
    }

    @Test
    void importar_programaNoExiste_retornaError() {
        when(estudianteRepository.existsByIdentificacion("123")).thenReturn(false);
        when(estudianteRepository.existsByCorreo("est@test.com")).thenReturn(false);
        when(programaRepository.findById(99L)).thenReturn(Optional.empty());

        ResultadoImportacion resultado = importador.importar(excelConFila("123", "Estudiante", "est@test.com", "99", "8", "90", "4.0"));

        assertTrue(resultado.getExitosos().isEmpty());
        assertTrue(resultado.getErrores().get(0).getDescripcion().contains("No existe"));
    }

    @Test
    void importar_programaInactivo_retornaError() {
        when(estudianteRepository.existsByIdentificacion("123")).thenReturn(false);
        when(estudianteRepository.existsByCorreo("est@test.com")).thenReturn(false);
        when(programaRepository.findById(1L)).thenReturn(Optional.of(programa(false)));

        ResultadoImportacion resultado = importador.importar(excelConFila("123", "Estudiante", "est@test.com", "1", "8", "90", "4.0"));

        assertTrue(resultado.getExitosos().isEmpty());
        assertTrue(resultado.getErrores().get(0).getDescripcion().contains("inactivo"));
    }

    @Test
    void importar_identificacionDuplicadaEnArchivo_retornaUnExitosoYUnError() {
        when(estudianteRepository.existsByIdentificacion("123")).thenReturn(false);
        when(estudianteRepository.existsByCorreo("uno@test.com")).thenReturn(false);
        when(estudianteRepository.existsByCorreo("dos@test.com")).thenReturn(false);
        when(programaRepository.findById(1L)).thenReturn(Optional.of(programa(true)));

        ResultadoImportacion resultado = importador.importar(excelConDosFilasMismaIdentificacion());

        assertEquals(2, resultado.getTotal());
        assertEquals(1, resultado.getExitosos().size());
        assertEquals(1, resultado.getErrores().size());
        assertTrue(resultado.getErrores().get(0).getDescripcion().contains("duplicada"));
    }

    @Test
    void validar_retornaErroresDeImportar() {
        assertEquals(1, importador.validar(excelConFila("", "Estudiante", "est@test.com", "1", "8", "90", "4.0")).size());
    }

    private byte[] excelConFila(String identificacion, String nombre, String correo, String programaId,
                                String semestre, String creditos, String promedio) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("estudiantes");
            crearCabecera(sheet);
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue(identificacion);
            row.createCell(1).setCellValue(nombre);
            row.createCell(2).setCellValue(correo);
            row.createCell(3).setCellValue(programaId);
            row.createCell(4).setCellValue(semestre);
            row.createCell(5).setCellValue(creditos);
            row.createCell(6).setCellValue(promedio);
            workbook.write(output);
            return output.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private byte[] excelConDosFilasMismaIdentificacion() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("estudiantes");
            crearCabecera(sheet);
            Row first = sheet.createRow(1);
            first.createCell(0).setCellValue("123");
            first.createCell(1).setCellValue("Uno");
            first.createCell(2).setCellValue("uno@test.com");
            first.createCell(3).setCellValue("1");
            Row second = sheet.createRow(2);
            second.createCell(0).setCellValue("123");
            second.createCell(1).setCellValue("Dos");
            second.createCell(2).setCellValue("dos@test.com");
            second.createCell(3).setCellValue("1");
            workbook.write(output);
            return output.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private byte[] excelSinColumnaCorreo() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("estudiantes");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("identificacion");
            header.createCell(1).setCellValue("nombre");
            header.createCell(2).setCellValue("programaId");
            workbook.write(output);
            return output.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private byte[] excelVacio() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            workbook.createSheet("estudiantes");
            workbook.write(output);
            return output.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void crearCabecera(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("identificacion");
        header.createCell(1).setCellValue("nombre");
        header.createCell(2).setCellValue("correo");
        header.createCell(3).setCellValue("programaId");
        header.createCell(4).setCellValue("semestre");
        header.createCell(5).setCellValue("creditosAprobados");
        header.createCell(6).setCellValue("promedioAcumulado");
    }

    private Programa programa(Boolean activo) {
        return Programa.builder().id(1L).nombre("Sistemas").activo(activo).build();
    }
}
