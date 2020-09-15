package com.badge.dx.port.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI defineOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .contact(new Contact().email("yogonza524@gmail.com").name("Gonzalo H. Mendoza")))
        .components(new Components())
        .addServersItem(localServer())
        .addServersItem(defineServers());
  }

  private Server localServer() {
    return new Server().url("http://localhost:8080/").description("Badge DX API Rest");
  }

  private Server defineServers() {
    return new Server().url("https://badge-dx.herokuapp.com/").description("Badge DX API Rest");
  }
}
