package com.avh.practicas.seguimiento.service;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.bitacora.entity.EntradaBitacora;
import com.avh.practicas.bitacora.repository.EntradaBitacoraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BitacoraServiceImpl implements BitacoraService {

    private final EntradaBitacoraRepository repository;

    @Override
    @Transactional
    public void registrar(String tablaAfectada, String accion, Usuario usuario, String detalle) {
        EntradaBitacora bitacora = EntradaBitacora.builder()
                .tablaAfectada(tablaAfectada)
                .accion(accion)
                .usuarioId(usuario != null ? usuario.getId() : null)
                .detalle(detalle)
                .build();
        repository.save(bitacora);
    }
}
