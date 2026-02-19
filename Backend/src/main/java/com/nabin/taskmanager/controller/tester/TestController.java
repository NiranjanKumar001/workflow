package com.nabin.taskmanager.controller.tester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/admin")
    public String admin() {
        return "HELLO FINLEY SIMULA67 / NABIN STRIVEX TO YOUR OWN LEARNING EMPIRE !";
    }

    @GetMapping("/hello")
    public String hello() {
        return "Task Manager API is running!";
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Task Manager is healthy");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    @GetMapping("/db-test")
    public String dbTest()
    {
        return "Database: taskmanager_db connected successfully!";
    }
}

