package com.avh.practicas.empresa.service;

import com.avh.practicas.configuracion.entity.CatalogoItem;
import com.avh.practicas.configuracion.entity.TipoCatalogo;
import com.avh.practicas.configuracion.repository.CatalogoItemRepository;
import com.avh.practicas.empresa.entity.Empresa;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.EmpresaRepository;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.vacante.repository.VacanteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceImplTest {

    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private TutorEmpresarialRepository tutorEmpresarialRepository;
    @Mock
    private VacanteRepository vacanteRepository;
    @Mock
    private CatalogoItemRepository catalogoItemRepository;

    @InjectMocks
    private EmpresaServiceImpl service;

    @Test
    void registrar_empresaValida_asignaSectorYActiva() {
        Empresa empresa = empresa("900", false);
        CatalogoItem sector = sector();
        when(empresaRepository.existsByNit("900")).thenReturn(false);
        when(catalogoItemRepository.findById(3L)).thenReturn(Optional.of(sector));
        when(empresaRepository.save(empresa)).thenReturn(empresa);

        Empresa response = service.registrar(empresa);

        assertSame(sector, response.getSector());
        assertTrue(response.getActivo());
        verify(empresaRepository).save(empresa);
    }

    @Test
    void registrar_nitDuplicado_lanzaNegocioException() {
        Empresa empresa = empresa("900", true);
        when(empresaRepository.existsByNit("900")).thenReturn(true);

        assertThrows(NegocioException.class, () -> service.registrar(empresa));
        verify(empresaRepository, never()).save(any());
    }

    @Test
    void registrar_sectorNoExiste_lanzaRecursoNoEncontrado() {
        Empresa empresa = empresa("900", true);
        when(empresaRepository.existsByNit("900")).thenReturn(false);
        when(catalogoItemRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.registrar(empresa));
        verify(empresaRepository, never()).save(any());
    }

    @Test
    void editar_empresaExistente_actualizaCampos() {
        Empresa existente = empresa("900", true);
        Empresa actualizada = empresa("901", true);
        actualizada.setRazonSocial("Nueva SAS");
        actualizada.setDireccion("Calle 1");
        actualizada.setMunicipio("Bogota");
        actualizada.setTelefono("555");
        CatalogoItem sector = sector();
        when(empresaRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(empresaRepository.existsByNit("901")).thenReturn(false);
        when(catalogoItemRepository.findById(3L)).thenReturn(Optional.of(sector));
        when(empresaRepository.save(existente)).thenReturn(existente);

        Empresa response = service.editar(5L, actualizada);

        assertEquals("901", response.getNit());
        assertEquals("Nueva SAS", response.getRazonSocial());
        assertEquals("Bogota", response.getMunicipio());
        assertSame(sector, response.getSector());
    }

    @Test
    void editar_empresaNoExiste_lanzaRecursoNoEncontrado() {
        when(empresaRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.editar(5L, empresa("901", true)));
    }

    @Test
    void editar_nitDuplicadoDistinto_lanzaNegocioException() {
        Empresa existente = empresa("900", true);
        Empresa actualizada = empresa("901", true);
        when(empresaRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(empresaRepository.existsByNit("901")).thenReturn(true);

        assertThrows(NegocioException.class, () -> service.editar(5L, actualizada));
        verify(empresaRepository, never()).save(any());
    }

    @Test
    void editar_mismoNitNoSeConsideraDuplicado() {
        Empresa existente = empresa("900", true);
        Empresa actualizada = empresa("900", true);
        when(empresaRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(empresaRepository.existsByNit("900")).thenReturn(true);
        when(catalogoItemRepository.findById(3L)).thenReturn(Optional.of(sector()));
        when(empresaRepository.save(existente)).thenReturn(existente);

        Empresa response = service.editar(5L, actualizada);

        assertEquals("900", response.getNit());
        verify(empresaRepository).save(existente);
    }

    @Test
    void desactivar_sinMotivo_lanzaNegocioException() {
        assertThrows(NegocioException.class, () -> service.desactivar(5L, " "));
        verifyNoInteractions(empresaRepository);
    }

    @Test
    void desactivar_conVacantePendiente_lanzaNegocioException() {
        Empresa empresa = empresa("900", true);
        when(empresaRepository.findById(5L)).thenReturn(Optional.of(empresa));
        when(vacanteRepository.existsByEmpresaIdAndEstado(5L, "PENDIENTE_APROBACION")).thenReturn(true);

        assertThrows(NegocioException.class, () -> service.desactivar(5L, "Motivo"));
        verify(empresaRepository, never()).save(any());
    }

    @Test
    void desactivar_sinVacantesActivas_inactivaEmpresaYTutores() {
        Empresa empresa = empresa("900", true);
        TutorEmpresarial tutor = TutorEmpresarial.builder().activo(true).build();
        when(empresaRepository.findById(5L)).thenReturn(Optional.of(empresa));
        when(vacanteRepository.existsByEmpresaIdAndEstado(5L, "PENDIENTE_APROBACION")).thenReturn(false);
        when(vacanteRepository.existsByEmpresaIdAndEstado(5L, "ACTIVA")).thenReturn(false);
        when(tutorEmpresarialRepository.findByEmpresaId(5L)).thenReturn(List.of(tutor));

        service.desactivar(5L, "  Cierre  ");

        assertFalse(empresa.getActivo());
        assertEquals("Cierre", empresa.getMotivoInactivacion());
        assertNotNull(empresa.getFechaInactivacion());
        assertFalse(tutor.getActivo());
        verify(tutorEmpresarialRepository).save(tutor);
        verify(empresaRepository).save(empresa);
    }

    @Test
    void activar_empresaExistente_limpiaMotivoYFecha() {
        Empresa empresa = empresa("900", false);
        empresa.setMotivoInactivacion("Cierre");
        empresa.setFechaInactivacion(java.time.LocalDateTime.now());
        when(empresaRepository.findById(5L)).thenReturn(Optional.of(empresa));

        service.activar(5L);

        assertTrue(empresa.getActivo());
        assertNull(empresa.getMotivoInactivacion());
        assertNull(empresa.getFechaInactivacion());
        verify(empresaRepository).save(empresa);
    }

    @Test
    void obtenerPorNit_delegaEnRepositorio() {
        Empresa empresa = empresa("900", true);
        when(empresaRepository.findByNit("900")).thenReturn(Optional.of(empresa));

        assertSame(empresa, service.obtenerPorNit("900").orElseThrow());
    }

    @Test
    void obtenerPorPrograma_delegaEnRepositorio() {
        List<Empresa> empresas = List.of(empresa("900", true));
        when(empresaRepository.findByProgramaId(1L)).thenReturn(empresas);

        assertSame(empresas, service.obtenerPorPrograma(1L));
    }

    @Test
    void listar_conFiltros_delegaEnRepositorioConPageable() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(empresaRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(empresa("900", true))));

        assertEquals(1, service.listar("Tecnologia", "Sistemas", true, pageable).getTotalElements());
    }

    private Empresa empresa(String nit, Boolean activo) {
        return Empresa.builder()
                .nit(nit)
                .razonSocial("Empresa SAS")
                .sector(CatalogoItem.builder().id(3L).build())
                .activo(activo)
                .build();
    }

    private CatalogoItem sector() {
        return CatalogoItem.builder()
                .id(3L)
                .tipo(TipoCatalogo.SECTOR_ECONOMICO)
                .nombre("Tecnologia")
                .activo(true)
                .build();
    }
}
