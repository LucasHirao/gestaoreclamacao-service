package com.banco.reclamacoes.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguracao {

    @Bean
    OpenAPI openApiGestaoReclamacoes() {
        return new OpenAPI()
            .info(
                new Info()
                    .title("Gestão de Reclamações — Portal Interno")
                    .version("v0")
                    .description(
                        """
                        APIs de consulta e endpoints de desenvolvimento do MVP de Gestão \
                        de Reclamações. Endpoints `/dev/**` ficam disponíveis apenas nos \
                        perfis local e dev."""));
    }
}
