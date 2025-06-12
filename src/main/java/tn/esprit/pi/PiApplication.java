package tn.esprit.pi;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import tn.esprit.pi.role.Role;
import tn.esprit.pi.role.RoleRepository;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync

public class PiApplication {

	public static void main(String[] args) {
		SpringApplication.run(PiApplication.class, args);
	}

	@Bean
	public CommandLineRunner runner(RoleRepository roleRepository) {
		return args -> {
			if (roleRepository.findByName("USER").isEmpty()) {
				roleRepository.save(Role.builder().name("USER").build());
			}
			if (roleRepository.findByName("TEACHER").isEmpty()) {
				roleRepository.save(Role.builder().name("TEACHER").build());
			}
			if (roleRepository.findByName("HR_COMPANY").isEmpty()) {
				roleRepository.save(Role.builder().name("HR_COMPANY").build());
			}
		};
	}

}
