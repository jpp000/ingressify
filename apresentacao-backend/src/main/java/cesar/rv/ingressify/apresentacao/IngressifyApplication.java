package cesar.rv.ingressify.apresentacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
		"cesar.rv.ingressify.apresentacao",
		"cesar.rv.ingressify.infraestrutura"
})
public class IngressifyApplication {

	public static void main(String[] args) {
		SpringApplication.run(IngressifyApplication.class, args);
	}
}
