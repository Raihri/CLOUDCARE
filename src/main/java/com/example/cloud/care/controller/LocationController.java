package com.example.cloud.care.controller;

import com.example.cloud.care.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/locations")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @GetMapping("/districts")
    @ResponseBody
    public ResponseEntity<?> districts() {
        return ResponseEntity.ok(Map.of("success", true, "districts", locationService.getDistricts()));
    }

    @GetMapping("/thanas")
    @ResponseBody
    public ResponseEntity<?> thanas(@RequestParam String district) {
        return ResponseEntity.ok(Map.of("success", true, "thanas", locationService.getThanasByDistrict(district)));
    }
}
