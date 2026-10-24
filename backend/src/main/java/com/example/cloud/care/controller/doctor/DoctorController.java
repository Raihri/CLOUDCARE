package com.example.cloud.care.controller.doctor;

import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.service.DoctorService;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Doctor signup application.
 *
 * <p>Sign-in, dashboard and profile editing are all served by the JSON APIs
 * under {@code /doctor/api/**} ({@link DoctorApiController}); this class only
 * still exists for the one flow with no JSON equivalent — submitting a new
 * application with a profile photo and certificate.
 */
@RestController
@RequestMapping("/doctor")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping("/signup/request")
    public Map<String, Object> handleSignupRequest(@ModelAttribute Doctor doctorRequest,
                                                    @RequestParam("profileImageFile") MultipartFile profileImage,
                                                    @RequestParam("certificateFile") MultipartFile certificateFile) {
        try {
            doctorService.saveSignupRequest(doctorRequest, profileImage, certificateFile);
            return result(true, "Signup request submitted! Wait for approval.");
        } catch (IllegalArgumentException e) {
            return result(false, e.getMessage());
        } catch (IOException e) {
            return result(false, "File upload failed: " + e.getMessage());
        }
    }

    private static Map<String, Object> result(boolean ok, String message) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", ok);
        out.put("message", message);
        return out;
    }
}
