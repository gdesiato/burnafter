package com.burnafter.burnafter.configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // PROD frontends:
                .allowedOrigins(
                        "https://gdesiato.github.io",   // GitHub Pages (project site)
                        "https://burnafter.pages.dev"   // if you ever use Cloudflare Pages
                )
                // DEV:
                .allowedOrigins("http://localhost:4200")
                // Methods/headers:
                .allowedMethods("GET","POST","OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}

