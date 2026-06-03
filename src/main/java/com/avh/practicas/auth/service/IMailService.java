package com.avh.practicas.auth.service;

/**
 * Puerto de envío de correos (DIP). Las implementaciones viven en infrastructure/adapters.
 */
public interface IMailService {

    void enviarRecuperacionPassword(String destinatario, String tokenRecuperacion);
}
