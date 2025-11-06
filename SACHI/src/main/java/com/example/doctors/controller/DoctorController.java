package com.example.doctors.controller;

import com.example.doctors.model.Doctor;
import com.example.doctors.repository.DoctorRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/doctors")
public class DoctorController {

    private final DoctorRepository repo;

    public DoctorController(DoctorRepository repo) {
        this.repo = repo;
    }

    @PostConstruct
    public void init() {
        repo.initSample();
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("doctors", repo.findAll());
        return "list";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        Optional<Doctor> d = repo.findById(id);
        if (d.isPresent()) {
            model.addAttribute("doctor", d.get());
            return "view";
        } else {
            return "redirect:/doctors";
        }
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("doctor", new Doctor());
        model.addAttribute("action", "Create");
        return "form";
    }

    @PostMapping("/save")
public String save(
        @ModelAttribute Doctor doctor,
        @RequestParam(required=false) Long oldId,
        @RequestParam(value = "profileImageFile", required = false) MultipartFile profileImageFile,
        RedirectAttributes ra) {

    // 1) If a new file was uploaded, store it to ./uploads and set doctor.pictureUrl
    if (profileImageFile != null && !profileImageFile.isEmpty()) {
        try {
            // validate content-type (basic)
            String contentType = profileImageFile.getContentType();
            if (contentType == null || !(contentType.equals("image/png") || contentType.equals("image/jpeg"))) {
                ra.addFlashAttribute("msg", "Only PNG/JPEG images are allowed.");
                return "redirect:/doctors";
            }

            // ensure uploads dir exists
            java.nio.file.Path uploadDir = java.nio.file.Paths.get("uploads");
            if (!java.nio.file.Files.exists(uploadDir)) {
                java.nio.file.Files.createDirectories(uploadDir);
            }

            // create safe unique filename (uuid + original extension)
            String original = profileImageFile.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.'));
            }
            String filename = java.util.UUID.randomUUID().toString() + ext;
            java.nio.file.Path target = uploadDir.resolve(filename);

            // save file
            try (java.io.InputStream in = profileImageFile.getInputStream()) {
                java.nio.file.Files.copy(in, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            // set accessible URL (served via WebConfig mapping)
            doctor.setPictureUrl("/uploads/" + filename);

        } catch (Exception ex) {
            ex.printStackTrace();
            ra.addFlashAttribute("msg", "Failed to upload image: " + ex.getMessage());
            return "redirect:/doctors";
        }
    }

    // 2) existing ID-change/normal save logic
    if (oldId != null && !oldId.equals(doctor.getId())) {
        repo.saveWithIdChange(oldId, doctor);
        ra.addFlashAttribute("msg", "Doctor saved (ID changed).");
    } else {
        repo.save(doctor);
        ra.addFlashAttribute("msg", "Doctor saved.");
    }

    return "redirect:/doctors";
}

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Optional<Doctor> d = repo.findById(id);
        if (d.isPresent()) {
            model.addAttribute("doctor", d.get());
            model.addAttribute("action", "Edit");
            model.addAttribute("oldId", id);
            return "form";
        } else {
            return "redirect:/doctors";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        repo.delete(id);
        ra.addFlashAttribute("msg", "Doctor deleted.");
        return "redirect:/doctors";
    }
}
