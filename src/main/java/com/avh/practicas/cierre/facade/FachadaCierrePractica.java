package com.avh.practicas.cierre.facade;

import com.avh.practicas.cierre.dto.CierrePracticaResponse;
import com.avh.practicas.cierre.checklist.dto.ResumenChecklist;
import com.avh.practicas.cierre.entity.TipoEncuesta;

public interface FachadaCierrePractica {

    ResumenChecklist verificarChecklist(Long practicaId);

    CierrePracticaResponse ejecutarCierre(Long practicaId, Long coordinadorId);

    void enviarRecordatorioEncuesta(Long practicaId, TipoEncuesta tipo);
}
