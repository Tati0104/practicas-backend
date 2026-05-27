package com.avh.practicas.correo.factory;

import java.time.LocalDateTime;
import java.util.List;

public interface Notificacion {
    String getTipo();
    String getMensaje();
    String getAsunto();
    List<String> getDestinatarios();
    LocalDateTime getFecha();
}
