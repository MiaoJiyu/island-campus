package com.islandcampus.server;

import com.islandcampus.server.common.base.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/v1/health")
    public R<Map<String, String>> health() {
        return R.ok(Map.of(
                "status", "UP",
                "service", "island-campus",
                "version", "2.1.0"
        ));
    }
}
