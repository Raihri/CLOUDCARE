package com.example.cloud.care.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class landingController {
    @GetMapping({"/"})
    public String landing()
    {
        return "landing";
    }
}
