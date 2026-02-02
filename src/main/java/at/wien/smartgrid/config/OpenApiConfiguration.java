package at.wien.smartgrid.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI smartGridOpenApi() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("Smart Grid Energy API")
                        .version("v1")
                        .description("Ingest smart-meter readings and inspect aggregate grid load.")
                        .contact(new Contact().name("Dzovani Koller")));
    }
}

