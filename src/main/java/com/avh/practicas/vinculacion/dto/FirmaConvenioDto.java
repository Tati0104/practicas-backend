package com.avh.practicas.vinculacion.dto;

import java.time.LocalDateTime;

public record FirmaConvenioDto(
        String tipoFirmante,
        boolean firmado,
        LocalDateTime fechaFirma
) {
}
