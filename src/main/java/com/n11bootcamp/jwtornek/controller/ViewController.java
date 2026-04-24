package com.n11bootcamp.jwtornek.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping({"/", "/demo"})
    public String demoPage() {
        return "demo";
    }
}
