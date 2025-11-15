package com.example.cloud.care.service;

import com.example.cloud.care.model.Request;
import com.example.cloud.care.repository.RequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequestService {
    private final RequestRepository requestRepository;

    public RequestService(RequestRepository requestRepository){ this.requestRepository = requestRepository; }

    public List<Request> getAllRequests(){ return requestRepository.findAll(); }
    public Request saveRequest(Request request){ return requestRepository.save(request); }
    public void deleteRequest(Long id){ requestRepository.deleteById(id); }
    public Request getRequest(Long id){ return requestRepository.findById(id).orElse(null); }

    // Added method to match DashboardController
    public List<Request> filterRequests(String type, String bloodGroup, String city){
        // For now, just return all requests (parameters can be used later for filtering)
        return requestRepository.findAll();
    }
}
