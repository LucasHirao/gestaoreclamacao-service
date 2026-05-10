package com.banco.reclamacoes.infrastructure.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RelogioConfiguracao {

    @Bean
    Clock relogioUtc() {
        return Clock.systemUTC();
    }
}
