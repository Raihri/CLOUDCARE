package com.example.cloud.care.controller;

import com.example.cloud.care.model.Task;
import com.example.cloud.care.service.TaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Controller
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    // Dashboard / Daily To-Do List
    @GetMapping({"/tasks"})
    public String dashboard(
            Model model,
            @RequestParam(required = false) Boolean showSuccessModal,
            @RequestParam(required = false) String successMessage) {

        List<Task> allTasks = service.findAll();

        // TODAY'S TASKS ONLY ("Completed Today" count)
        List<Task> todayTasks = allTasks.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isEqual(LocalDate.now()))
                .toList();

        // Count how many of TODAY'S tasks are completed
        long completedTodayCount = todayTasks.stream()
                .filter(Task::isCompleted)
                .count();

        model.addAttribute("tasks", allTasks);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("completedTodayCount", completedTodayCount);  // ← ONLY NEW LINE

        if (showSuccessModal != null && showSuccessModal) {
            model.addAttribute("showSuccessModal", true);
            model.addAttribute("successMessage",
                    successMessage != null ? successMessage : "Task saved successfully!");
        }
        return "dashboard1";
    }

    // Today's Tasks (Table view)
    @GetMapping("/tasks/today")
    public String todayTasks(Model model) {
        List<Task> todayTasks = service.findAll().stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isEqual(LocalDate.now()))
                .toList();

        model.addAttribute("todayTasks", todayTasks);
        model.addAttribute("today", LocalDate.now());

        long completedToday = todayTasks.stream().filter(Task::isCompleted).count();
        double dailyRate = todayTasks.isEmpty() ? 0 : (completedToday * 100.0 / todayTasks.size());

        LocalDate weekAgo = LocalDate.now().minusDays(6);
        long totalLastWeek = service.findAll().stream()
                .filter(t -> t.getDueDate() != null && !t.getDueDate().isBefore(weekAgo))
                .count();
        long completedLastWeek = service.findAll().stream()
                .filter(t -> t.isCompleted() && t.getDueDate() != null && !t.getDueDate().isBefore(weekAgo))
                .count();

        double weeklyRate = totalLastWeek == 0 ? 0 : (completedLastWeek * 100.0 / totalLastWeek);

        model.addAttribute("dailyRate", Math.round(dailyRate));
        model.addAttribute("weeklyRate", Math.round(weeklyRate));

        return "today";
    }

    // Create New Task Form
    @GetMapping("/tasks/new")
    public String createForm(Model model) {
        Task task = new Task();
        model.addAttribute("task", task);
        model.addAttribute("title", "Add New Task");
        return "form";
    }

    // Edit Task Form
    @GetMapping("/tasks/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Task task = service.findById(id);
        if (task == null || task.isCompleted()) {
            return "redirect:/tasks";
        }
        model.addAttribute("task", task);
        model.addAttribute("title", "Update Task");
        return "form";
    }

    // Save New / Updated Task
    @PostMapping("/tasks/save")
    public String saveTask(@ModelAttribute Task task) {
        service.save(task);
        String message = (task.getId() == null)
                ? "New task added! Let's crush it!"
                : "Task updated successfully!";
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        return "redirect:/tasks?showSuccessModal=true&successMessage=" + encoded;
    }

    // Complete Task
    @GetMapping("/tasks/complete/{id}")
    public String completeTask(@PathVariable Long id) {
        Task task = service.findById(id);
        if (task != null && !task.isCompleted()) {
            task.setCompleted(true);
            task.setStatus("Completed");
            service.save(task);
        }
        String encoded = URLEncoder.encode("Task completed! Amazing work!", StandardCharsets.UTF_8);
        return "redirect:/tasks?showSuccessModal=true&successMessage=" + encoded;
    }

    // Delete Task
    @GetMapping("/tasks/delete/{id}")
    public String deleteTask(@PathVariable Long id) {
        service.deleteById(id);
        String encoded = URLEncoder.encode("Task deleted.", StandardCharsets.UTF_8);
        return "redirect:/tasks?showSuccessModal=true&successMessage=" + encoded;
    }
}