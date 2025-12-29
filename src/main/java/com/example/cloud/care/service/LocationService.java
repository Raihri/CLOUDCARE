package com.example.cloud.care.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class LocationService {

    private JsonNode root;

    @PostConstruct
    public void init() {
        try (InputStream is = new ClassPathResource("static/data/bd_locations.json").getInputStream()) {
            ObjectMapper om = new ObjectMapper();
            root = om.readTree(is);
        } catch (Exception e) {
            e.printStackTrace();
            root = null;
        }
    }

    public List<String> getDistricts() {
        try {
            if (root == null) return Collections.emptyList();
            JsonNode districts = root.get("districts");
            List<String> out = new ArrayList<>();
            for (JsonNode d : districts) out.add(d.get("name").asText());
            return out;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<String> getThanasByDistrict(String district) {
        try {
            if (root == null) return Collections.emptyList();
            JsonNode districts = root.get("districts");
            for (JsonNode d : districts) {
                if (d.get("name").asText().equalsIgnoreCase(district)) {
                    List<String> t = new ArrayList<>();
                    for (JsonNode th : d.get("thanas")) t.add(th.asText());
                    return t;
                }
            }
            return Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
