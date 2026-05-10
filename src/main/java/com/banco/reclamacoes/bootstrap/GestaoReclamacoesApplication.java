package com.banco.reclamacoes.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.banco.reclamacoes")
@EntityScan(basePackages = "com.banco.reclamacoes")
@EnableJpaRepositories(basePackages = "com.banco.reclamacoes")
public class GestaoReclamacoesApplication {

    public static void main(final String[] args) {
        SpringApplication.run(GestaoReclamacoesApplication.class, args);
    }
}
