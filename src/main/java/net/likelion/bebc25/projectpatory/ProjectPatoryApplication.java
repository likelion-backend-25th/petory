package net.likelion.bebc25.projectpatory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProjectPatoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectPatoryApplication.class, args);
    }

}
