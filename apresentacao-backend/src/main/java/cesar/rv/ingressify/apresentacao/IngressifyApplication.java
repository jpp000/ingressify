package cesar.rv.ingressify.apresentacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {
		"cesar.rv.ingressify.apresentacao",
		"cesar.rv.ingressify.infraestrutura"
})
@EnableJpaRepositories(basePackages = "cesar.rv.ingressify.infraestrutura.persistencia.springdata")
@EntityScan(basePackages = "cesar.rv.ingressify.infraestrutura.persistencia.jpa")
public class IngressifyApplication {

	public static void main(String[] args) {
		SpringApplication.run(IngressifyApplication.class, args);
	}
}
