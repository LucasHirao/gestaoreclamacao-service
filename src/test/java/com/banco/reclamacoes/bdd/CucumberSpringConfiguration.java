package com.banco.reclamacoes.bdd;

import com.banco.reclamacoes.bootstrap.GestaoReclamacoesApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(classes = GestaoReclamacoesApplication.class)
@Import({CenarioContexto.class, BddFronteirasSanidade.class, BddGlueSpringScan.class})
@ActiveProfiles({"test", "bdd"})
public class CucumberSpringConfiguration {}
