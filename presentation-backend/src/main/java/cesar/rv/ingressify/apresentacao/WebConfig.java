package cesar.rv.ingressify.apresentacao;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Value("${app.cors.origins:http://localhost:5173,http://localhost:3000}")
	private String corsOrigins;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		String[] origins = Arrays.stream(corsOrigins.split(",")).map(String::trim).toArray(String[]::new);
		registry.addMapping("/backend/**").allowedOrigins(origins).allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
				.allowCredentials(false);
	}
}
