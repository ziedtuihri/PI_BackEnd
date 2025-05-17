package tn.esprit.pi;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PiApplication {
	public static void main(String[] args) {
		SpringApplication.run(PiApplication.class, args);
	}

}
