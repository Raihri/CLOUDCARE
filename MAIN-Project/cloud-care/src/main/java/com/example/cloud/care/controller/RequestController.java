package com.example.cloud.care.controller;

import com.example.cloud.care.model.Request;
import com.example.cloud.care.service.RequestService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@CrossOrigin("*")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    // Use filterRequests to match Dashboard logic
    @GetMapping
    public List<Request> getRequests() {
        return requestService.filterRequests(null, null, null);
    }

    @PostMapping
    public Request addRequest(@RequestBody Request request) {
        return requestService.saveRequest(request);
    }

    @PutMapping("/{id}")
    public Request updateRequest(@PathVariable Long id, @RequestBody Request request) {
        request.setId(id);
        return requestService.saveRequest(request);
    }

    @DeleteMapping("/{id}")
    public void deleteRequest(@PathVariable Long id) {
        requestService.deleteRequest(id);
    }
}
