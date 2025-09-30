package com.pruebatecnica.config;

import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.ISO87APackager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JposConfig {

    @Bean
    public ISOPackager isoPackager() {
        // Usar packager predefinido en lugar de XML personalizado
        return new ISO87APackager();
    }
}