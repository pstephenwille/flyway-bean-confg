package swille.flywaydev.config;


import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

import java.util.Arrays;
import java.util.Properties;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;

@Configuration
public class FlywaySlaveInitializer {

    @Autowired
    Environment env;


    private Properties getUserDefinedFlywayProps() {
        Properties flywayProps = new Properties();

        MutablePropertySources allProps = ((AbstractEnvironment) env).getPropertySources();

        StreamSupport.stream(allProps.spliterator(), true)
            .filter(ps -> {
                /* files */
                return ps instanceof EnumerablePropertySource;
            })
            .map(ps -> {
                /* props obj */
                return ((EnumerablePropertySource) ps).getPropertyNames();
            })
            .flatMap(Arrays::<String>stream)/* convert [] to stream */
            .filter(prop -> prop.startsWith("flyway"))
//            .collect(Collectors.toMap(Function.identity(), env::getProperty))
            .forEach(propName -> {
                flywayProps.setProperty(propName, env.getProperty(propName));
            });

        return flywayProps;
    }


    @Bean(initMethod = "migrate")
    @PostConstruct
    Flyway flyway() {
        Properties props = getUserDefinedFlywayProps();

        String[] schemas = props.getProperty("flyway.schemas").split(",");
        String[] schemaFiles = env.getProperty("flyway.locations").split(",");

        Flyway flyway = new Flyway();

        for (int schema = 0; schema < schemas.length; schema++) {
            flyway.setDataSource(props.getProperty("flyway.url") + "/" + schemas[schema],
                                 props.getProperty("flyway.user"),
                                 props.getProperty("flyway.password"));
            flyway.setSchemas(schemas[schema]);
            flyway.setLocations(schemaFiles[schema]);
            flyway.migrate();
        }

        return flyway;
    }
}
