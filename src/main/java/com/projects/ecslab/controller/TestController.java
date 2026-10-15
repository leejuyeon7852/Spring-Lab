package com.projects.ecslab.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {
    @GetMapping("/test")
    public String test(){
        return "Hello! Amazon ECS Fargate Test";
    }

    @GetMapping("/public")
    public String publicEndpoint() {
        return "Public";
    }

    @GetMapping("/member")
    public String memberEndpoint() {
        return "Member";
    }

    @GetMapping("/admin")
    public String adminEndpoint() {
        return "Admin";
    }
}
