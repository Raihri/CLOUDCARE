package com.example.cloud.care.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PatientController {

    @GetMapping("/patientS/{id}")
    public String patientPage(@PathVariable String id, Model model) {
        model.addAttribute("patientId", id);
        return "patientr";
    }
}
