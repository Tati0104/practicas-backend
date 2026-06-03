package com.avh.practicas.cierre.service;

import com.avh.practicas.cierre.checklist.composite.ChecklistCierre;
import com.avh.practicas.cierre.checklist.dto.ResumenChecklist;
import com.avh.practicas.cierre.entity.TipoEncuesta;

public interface ChecklistCierreService {

    ResumenChecklist obtenerResumen(Long practicaId);

    ChecklistCierre construirChecklist(Long practicaId);

    boolean habilitarBotonCierre(Long practicaId);

    void enviarRecordatorioEncuesta(Long practicaId, TipoEncuesta tipo);
}
