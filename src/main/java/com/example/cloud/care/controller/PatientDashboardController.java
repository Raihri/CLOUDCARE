package com.example.cloud.care.controller;
import com.example.cloud.care.model.Doctor;
import com.example.cloud.care.model.Donor;
import com.example.cloud.care.model.Patient;
import com.example.cloud.care.model.User;
import com.example.cloud.care.repository.PatientRepository;
import com.example.cloud.care.service.ChatService;
import com.example.cloud.care.service.UserService;
import com.example.cloud.care.service.doctor_service;
import com.example.cloud.care.service.loggedInUserFind;
import com.example.cloud.care.service.DonorService;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/patient")
public class PatientDashboardController {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private loggedInUserFind logger;

    @Autowired
    private doctor_service doctorService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private DonorService donorService;

    @Autowired
    private com.example.cloud.care.service.RequestService requestService;

    @GetMapping("/aichat")
    public String home(Model model) {
        model.addAttribute("patient", logger.logger());
        return "aichat"; // Returns template name
    }

    @PostMapping("/chat")
    public String chat(@RequestParam("message") String message, Model model) {
        // Get response from gimmick function
        String response = getGimmickResponse(message);

        // Add data to model
        model.addAttribute("userMessage", message);
        model.addAttribute("botResponse", response);
        model.addAttribute("patient", logger.logger());

        // IMPORTANT: Return the template name, not the response text!
        return "aichat"; // This tells Spring to render aichat.html template
    }

    // GIMMICK FUNCTION - Hardcoded responses for common stuff
    private String getGimmickResponse(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "Hello! I'm Dr. Bot. How can I assist with your health concerns today? 🤖";
        }

        String lowerMsg = message.toLowerCase().trim();

        // Medical Condition Responses
        if (lowerMsg.contains("fever")) {
            return "🌡️ **Fever Alert!** Common symptoms:\n" +
                    "• Temperature above 100.4°F (38°C)\n" +
                    "• Chills & shivering\n" +
                    "• Headache\n" +
                    "• Muscle aches\n\n" +
                    "💡 **Quick Tips:**\n" +
                    "✓ Stay hydrated 💧\n" +
                    "✓ Rest well 😴\n" +
                    "✓ Use cool compress ❄️\n" +
                    "✓ Monitor temperature 📊\n\n" +
                    "🚨 **Seek help if:**\n" +
                    "• Fever > 103°F (39.4°C)\n" +
                    "• Lasts > 3 days\n" +
                    "• Severe headache/stiff neck\n\n" +
                    "⚠️ *This is general advice. See a doctor for proper diagnosis.*";
        }

        if (lowerMsg.contains("cough") || lowerMsg.contains("cold")) {
            return "🤧 **Cough & Cold Care:**\n\n" +
                    "🔹 **Symptoms:**\n" +
                    "• Runny/stuffy nose 👃\n" +
                    "• Sore throat 🗣️\n" +
                    "• Coughing\n" +
                    "• Sneezing\n" +
                    "• Mild fever\n\n" +
                    "💡 **Home Remedies:**\n" +
                    "✓ Honey & warm water 🍯\n" +
                    "✓ Steam inhalation 💨\n" +
                    "✓ Salt water gargle\n" +
                    "✓ Rest & hydration 💤💧\n\n" +
                    "💊 **OTC Options:**\n" +
                    "• Cough suppressants\n" +
                    "• Decongestants\n" +
                    "• Pain relievers\n\n" +
                    "📞 **Call doctor if:**\n" +
                    "• Shortness of breath\n" +
                    "• High fever (>102°F)\n" +
                    "• Symptoms worsen after 7 days";
        }

        if (lowerMsg.contains("headache")) {
            return "🤕 **Headache Help:**\n\n" +
                    "🔸 **Types:**\n" +
                    "1. **Tension:** Band-like pressure\n" +
                    "2. **Migraine:** Throbbing + nausea\n" +
                    "3. **Sinus:** Face pressure\n" +
                    "4. **Cluster:** Severe, one-sided\n\n" +
                    "💡 **Quick Relief:**\n" +
                    "✓ Dark, quiet room 🌑\n" +
                    "✓ Cool compress on forehead ❄️\n" +
                    "✓ Gentle neck stretches\n" +
                    "✓ Hydration 💧\n" +
                    "✓ Caffeine (in moderation) ☕\n\n" +
                    "🚨 **RED FLAGS (Go to ER):**\n" +
                    "• \"Worst headache of your life\"\n" +
                    "• Head injury followed by headache\n" +
                    "• Sudden, severe onset\n" +
                    "• With fever/stiff neck\n" +
                    "• Vision changes/speech problems";
        }

        if (lowerMsg.contains("blood pressure") || lowerMsg.contains("bp")) {
            return "❤️ **Blood Pressure Guide:**\n\n" +
                    "📊 **Categories:**\n" +
                    "• **Normal:** <120/80 mmHg ✅\n" +
                    "• **Elevated:** 120-129/<80 ⚠️\n" +
                    "• **Stage 1:** 130-139/80-89 🟡\n" +
                    "• **Stage 2:** ≥140/≥90 🔴\n" +
                    "• **Crisis:** >180/>120 🚨\n\n" +
                    "💡 **Lower BP Naturally:**\n" +
                    "✓ Reduce salt intake 🧂\n" +
                    "✓ Exercise 30 min daily 🏃‍♂️\n" +
                    "✓ Healthy weight\n" +
                    "✓ Limit alcohol 🍷\n" +
                    "✓ Manage stress 🧘‍♀️\n" +
                    "✓ Quit smoking 🚭\n\n" +
                    "📅 **Check regularly!** Home monitoring recommended.";
        }

        if (lowerMsg.contains("diabetes") || lowerMsg.contains("sugar")) {
            return "🩸 **Diabetes Info:**\n\n" +
                    "📈 **Blood Sugar Levels:**\n" +
                    "• **Normal fasting:** 70-99 mg/dL ✅\n" +
                    "• **Prediabetes:** 100-125 mg/dL ⚠️\n" +
                    "• **Diabetes:** ≥126 mg/dL 🔴\n\n" +
                    "🍽️ **Diet Tips:**\n" +
                    "✓ High fiber foods 🥦\n" +
                    "✓ Whole grains 🌾\n" +
                    "✓ Lean proteins 🐟\n" +
                    "✓ Healthy fats 🥑\n" +
                    "✗ Limit sugary drinks 🥤\n" +
                    "✗ Processed foods\n\n" +
                    "🏃‍♀️ **Lifestyle:**\n" +
                    "• Regular exercise\n" +
                    "• Weight management\n" +
                    "• Regular check-ups\n" +
                    "• Medication adherence 💊";
        }

        if (lowerMsg.contains("stomach") || lowerMsg.contains("diarrhea")) {
            return "🤢 **Stomach Issues:**\n\n" +
                    "🔹 **For Diarrhea:**\n" +
                    "✓ BRAT diet: Bananas, Rice, Applesauce, Toast\n" +
                    "✓ Hydration with electrolytes 💧\n" +
                    "✓ Probiotics 🦠\n" +
                    "✗ Avoid dairy, fatty foods\n\n" +
                    "🔸 **For Constipation:**\n" +
                    "✓ More fiber 🥦\n" +
                    "✓ Water, water, water! 💧💧💧\n" +
                    "✓ Exercise 🏃‍♂️\n" +
                    "✓ Prune juice\n\n" +
                    "🚨 **See doctor for:**\n" +
                    "• Blood in stool\n" +
                    "• Severe pain\n" +
                    "• Dehydration signs\n" +
                    "• Symptoms > 2 days";
        }

        // Common Greetings
        if (lowerMsg.matches(".*(hi|hello|hey|good morning|good afternoon).*")) {
            return "👋 Hello! I'm your virtual health assistant! How can I help you today?\n\n" +
                    "I can provide info about:\n" +
                    "• Fever 🌡️\n" +
                    "• Cough/cold 🤧\n" +
                    "• Headache 🤕\n" +
                    "• Blood pressure ❤️\n" +
                    "• Diabetes 🩸\n" +
                    "• Stomach issues 🤢\n\n" +
                    "*Just ask me anything!*";
        }

        if (lowerMsg.matches(".*(thank|thanks|appreciate).*")) {
            return "You're welcome! 😊\nRemember: Your health is your wealth! Take care! 💪";
        }

        if (lowerMsg.matches(".*(bye|goodbye|see you).*")) {
            return "Goodbye! 👋\nStay healthy, stay happy! Remember to:\n" +
                    "• Drink water 💧\n" +
                    "• Get enough sleep 😴\n" +
                    "• Move your body 🏃‍♀️\n" +
                    "• Eat your veggies 🥦\n\n" +
                    "Come back anytime!";
        }

        if (lowerMsg.contains("appointment") || lowerMsg.contains("doctor")) {
            return "📅 **Book Appointment:**\n\n" +
                    "**Online:** Patient Portal → Appointments\n" +
                    "**Phone:** (123) 456-7890 📞\n" +
                    "**Hours:** Mon-Fri 9AM-5PM ⏰\n\n" +
                    "**Bring to appointment:**\n" +
                    "✓ Insurance card\n" +
                    "✓ ID\n" +
                    "✓ Medication list\n" +
                    "✓ Questions for doctor";
        }

        if (lowerMsg.contains("emergency")) {
            return "🚨 **MEDICAL EMERGENCY PROTOCOL:**\n\n" +
                    "1. **CALL 911 IMMEDIATELY** 📞\n" +
                    "2. **Stay calm**, help is coming\n" +
                    "3. **Do not** move injured person\n" +
                    "4. **Clear path** for responders\n" +
                    "5. **Gather:** meds list, ID, insurance\n\n" +
                    "**Emergency Signs:**\n" +
                    "• Chest pain\n" +
                    "• Difficulty breathing\n" +
                    "• Severe bleeding\n" +
                    "• Loss of consciousness\n" +
                    "• Sudden weakness/numbness";
        }

        if (lowerMsg.contains("medicine") || lowerMsg.contains("medication")) {
            return "💊 **Medication Safety:**\n\n" +
                    "**Always:**\n" +
                    "✓ Take as prescribed\n" +
                    "✓ Check expiration dates\n" +
                    "✓ Store properly\n" +
                    "✓ Know side effects\n\n" +
                    "**Never:**\n" +
                    "✗ Share medications\n" +
                    "✗ Double dose\n" +
                    "✗ Stop without doctor advice\n" +
                    "✗ Mix with alcohol\n\n" +
                    "**Ask your pharmacist about:**\n" +
                    "• Best time to take\n" +
                    "• Food interactions\n" +
                    "• Storage requirements";
        }

        if (lowerMsg.contains("covid") || lowerMsg.contains("corona")) {
            return "🦠 **COVID-19 Info:**\n\n" +
                    "**Symptoms:**\n" +
                    "• Fever/chills\n" +
                    "• Cough\n" +
                    "• Shortness of breath\n" +
                    "• Fatigue\n" +
                    "• Loss of taste/smell\n\n" +
                    "**If positive:**\n" +
                    "✓ Isolate for 5 days\n" +
                    "✓ Wear mask around others\n" +
                    "✓ Monitor symptoms\n" +
                    "✓ Stay hydrated\n\n" +
                    "**Prevention:**\n" +
                    "• Vaccination 💉\n" +
                    "• Mask in crowded places 😷\n" +
                    "• Hand hygiene 🧼\n" +
                    "• Ventilation";
        }

        // Default responses for health queries
        String[] defaultResponses = {
                "🤔 I understand you're asking about health. For personalized advice, please consult with your healthcare provider.",
                "💭 That's an important health question! I recommend discussing this with your doctor for accurate guidance.",
                "👨‍⚕️ While I can provide general info, medical decisions should be made with professional advice.",
                "📋 Your health matters! Please schedule an appointment to discuss this with a medical professional.",
                "🌟 Great question about health! For your specific situation, a doctor's evaluation is best."
        };

        // Add some fun random responses
        if (Math.random() < 0.3) { // 30% chance of fun response
            String[] funResponses = {
                    "💡 Pro tip: Drink water like it's your job! 💧",
                    "😊 Remember: A healthy outside starts from the inside!",
                    "🌈 Health is like money - we never have a true idea of its value until we lose it!",
                    "⚡ Your future self will thank you for taking care of your health today!",
                    "🎯 Small daily improvements lead to stunning results in health!"
            };
            return funResponses[(int)(Math.random() * funResponses.length)];
        }

        return defaultResponses[(int)(Math.random() * defaultResponses.length)];
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Patient patient = logger.logger();
        model.addAttribute("patient", patient);
        return "patient_dashboard";
    }

    @GetMapping("/doctor_list")
    public String docList(Model model) {
        Patient patient = logger.logger();
        model.addAttribute("doctors", doctorService.getDoctors());
        model.addAttribute("patient", patient);
        return "doctor_list";
    }

    @GetMapping("/doctor/{id}")
    public String getDoctorById(@PathVariable("id") long id, Model model) {
        Doctor doc = doctorService.getDoctorByID(id);
        if (doc == null) {
            return "redirect:/list";
        }
        System.out.println("Doctor found with ID: " + id);
        System.out.println("Doctor name: " + doc.getName());
        System.out.println("Doctor profile image: " + doc.getProfileImage());
        model.addAttribute("doctor", doc);
        return "doctor_profile_view";
    }

    @GetMapping("/donor")
    public String donorDashboard(Model model) {
        Patient patient = logger.logger();
        model.addAttribute("patient", patient);
        return "donor_dashboard";
    }

    @GetMapping("/donor-form")
    public String donorForm(Model model, @RequestParam(value = "id", required = false) Long id) {
        Patient patient = logger.logger();
        model.addAttribute("patient", patient);

        if (id != null) {
            Donor donor = donorService.getDonor(id);
            if (donor != null) model.addAttribute("donor", donor);
        }

        return "donor_form";
    }

    @GetMapping("/request-form")
    public String requestForm(Model model, @RequestParam(value = "id", required = false) Long id) {
        Patient patient = logger.logger();
        model.addAttribute("patient", patient);

        if (id != null) {
            com.example.cloud.care.model.Request req = requestService.getRequest(id);
            if (req != null) model.addAttribute("request", req);
        }

        return "request_form";
    }

    @GetMapping("/donor-response")
    public String donorResponse(Model model) {
        Patient patient = logger.logger();
        model.addAttribute("patient", patient);
        return "donor_response_list";
    }
}