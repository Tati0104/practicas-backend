package com.avh.practicas.correo.service;

public interface IMailService {
    boolean enviar(String destinatario, String asunto, String htmlCuerpo);
}
