package com.avh.practicas.cierre.service;

import com.avh.practicas.cierre.checklist.ChecklistCierreFabrica;
import com.avh.practicas.cierre.checklist.composite.ChecklistCierre;
import com.avh.practicas.cierre.checklist.dto.ResumenChecklist;
import com.avh.practicas.cierre.entity.TipoEncuesta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChecklistCierreServiceImpl implements ChecklistCierreService {

    private final ChecklistCierreFabrica checklistFabrica;

    @Override
    public ResumenChecklist obtenerResumen(Long practicaId) {
        return construirChecklist(practicaId).getResumen();
    }

    @Override
    public ChecklistCierre construirChecklist(Long practicaId) {
        return checklistFabrica.construir(practicaId);
    }

    @Override
    public boolean habilitarBotonCierre(Long practicaId) {
        return construirChecklist(practicaId).habilitarBotonCierre();
    }

    @Override
    @Transactional
    public void enviarRecordatorioEncuesta(Long practicaId, TipoEncuesta tipo) {
        ChecklistCierre checklist = checklistFabrica.construir(practicaId);
        checklist.buscarItemEncuesta(tipo).enviarRecordatorio();
    }
}
