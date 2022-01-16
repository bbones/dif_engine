package com.pva.diffengine;

import com.pva.diffengine.service.DiffEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {
    private static DiffEngine diffEngine;

    @Autowired
    public DemoApplication(DiffEngine diffEngine) {
        this.diffEngine = diffEngine;
    }

    public static void main(String[] args) {

		SpringApplication.run(DemoApplication.class, args);
        // diffEngine.compare(null, null);
    }

}
