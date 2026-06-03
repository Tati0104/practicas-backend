package com.avh.practicas.shared.service;


public interface IMailService {
    boolean enviar(String destinatario, String asunto, String htmlCuerpo);
}