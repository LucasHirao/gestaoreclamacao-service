package com.banco.reclamacoes.bdd;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/** Registra steps Cucumber (pacote bdd) no contexto Spring dos testes. */
@Configuration
@ComponentScan(basePackageClasses = ProcessamentoSteps.class)
public class BddGlueSpringScan {}
