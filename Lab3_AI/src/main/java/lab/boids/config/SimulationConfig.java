package lab.boids.config;

import lab.boids.model.SimulationParams;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring читает секцию "boids" из application.yml и заполняет ею объект SimulationParams.
 * Так параметры меняются без правки кода.
 */
@Configuration
public class SimulationConfig {

    @Bean
    @ConfigurationProperties(prefix = "boids")
    public SimulationParams defaultParams() {
        return new SimulationParams();
    }
}
