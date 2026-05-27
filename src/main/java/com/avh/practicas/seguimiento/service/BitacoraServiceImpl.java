package com.avh.practicas.seguimiento.service;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.seguimiento.entity.BitacoraAuditoria;
import com.avh.practicas.seguimiento.repository.BitacoraAuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BitacoraServiceImpl implements BitacoraService {

    private final BitacoraAuditoriaRepository repository;

    @Override
    @Transactional
    public void registrar(String tablaAfectada, String accion, Usuario usuario, String detalle) {
        BitacoraAuditoria bitacora = BitacoraAuditoria.builder()
                .tablaAfectada(tablaAfectada)
                .accion(accion)
                .usuario(usuario)
                .detalle(detalle)
                .build();
        repository.save(bitacora);
    }
}
