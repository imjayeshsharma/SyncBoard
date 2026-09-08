package com.syncboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SyncBoard — internal ticketing system.
 *
 * <p>Feature layers are switched on per build stage (see docs/BUILD_STAGES.md).
 * Everything the application needs to boot at stage 2 lives here; later stages
 * add configuration under {@code com.syncboard.config} without touching this class.
 */
@SpringBootApplication
public class SyncBoardApplication {

    public static void main(String[] args) {
        SpringApplication.run(SyncBoardApplication.class, args);
    }
}
