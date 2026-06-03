package com.avh.practicas.vinculacion.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "vinculacion")
public class VinculacionProperties {

    private String directorioUpload = "uploads/vinculacion";

    private long tamanoMaximoBytes = 10L * 1024 * 1024;
}
