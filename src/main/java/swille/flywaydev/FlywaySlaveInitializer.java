package swille.flywaydev;


import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class FlywaySlaveInitializer {

//    @Autowired private DataSource db1;
//    @Autowired private DataSource db2;

    @PostConstruct
    public void migrateFlyway() {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:mysql://localhost:3306/flyway_db_1", "root", "root");
        flyway.migrate();

        flyway.setDataSource("jdbc:mysql://localhost:3306/flyway_db_2", "root", "root");
        flyway.migrate();
    }
}
