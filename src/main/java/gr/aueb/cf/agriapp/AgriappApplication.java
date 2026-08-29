package gr.aueb.cf.agriapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AgriappApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgriappApplication.class, args);
	}
}
