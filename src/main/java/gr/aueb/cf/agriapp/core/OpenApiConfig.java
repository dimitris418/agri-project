package gr.aueb.cf.agriapp.core;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AgriApp API")
                        .version("1.0.0")
                        .description("Ψηφιακό ημερολόγιο αγρού για καλλιέργειες σιτηρών. "
                                + "Η εγγραφή και η σύνδεση είναι ανοιχτές, κάθε άλλη κλήση "
                                + "απαιτεί JWT στο header Authorization."));
    }
}
