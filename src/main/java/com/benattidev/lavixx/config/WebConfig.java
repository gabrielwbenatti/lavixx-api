package com.benattidev.lavixx.config;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Configuração web para servir a API e o frontend (SPA) no mesmo processo/porta.
 *
 * <ul>
 *   <li>Todos os {@code @RestController} passam a responder sob o prefixo {@code /api}
 *       (ex.: {@code /api/customers}), casando com o {@code baseURL} do frontend.</li>
 *   <li>Os arquivos estáticos do build do frontend (classpath:/static) são servidos na
 *       raiz; qualquer rota que não seja arquivo nem {@code /api} devolve o
 *       {@code index.html} (fallback de SPA para rotas do React Router).</li>
 * </ul>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api", HandlerTypePredicate.forAnnotation(RestController.class));
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location)
                            throws IOException {
                        Resource requested = location.createRelative(resourcePath);
                        if (requested.exists() && requested.isReadable()) {
                            return requested;
                        }
                        // Fallback de SPA: rotas do cliente (ex.: /clientes, /ordens/123)
                        // devolvem o index.html para o React Router assumir.
                        return new ClassPathResource("/static/index.html");
                    }
                });
    }
}
