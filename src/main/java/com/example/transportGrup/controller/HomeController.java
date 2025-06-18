package com.example.transportGrup.controller;

import com.example.transportGrup.entity.User;
import com.example.transportGrup.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String showHome(Principal principal, Model model) {
        if (principal == null) {
            model.addAttribute("loggedIn", false);
            return "guest-page";
        }

        String username = principal.getName(); // This returns the username used at login

        model.addAttribute("username", username);
        model.addAttribute("loggedIn", true);

        return "index";
    }

    @GetMapping("/signup")
    public String showSignup(Model model) {
        model.addAttribute("mode", "signup");
        return "auth";
    }

    @GetMapping("/login")
    public String showLogin(Model model) {
        model.addAttribute("mode", "login");
        return "auth";
    }


    @GetMapping("/verify")
    public String showVerifyPage(@RequestParam String email, Model model,
                                 @ModelAttribute("message") String message,
                                 @ModelAttribute("error") String error) {
        model.addAttribute("email", email);
        model.addAttribute("message", message);
        model.addAttribute("error", error);
        return "verify";
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Principal principal, Model model) {
        String username = principal.getName();

        if (username == null) return "redirect:/login";

        User user = userRepository.findByUsername(username).orElseThrow();

        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("phoneNumber", user.getPhoneNumber());
        model.addAttribute("phoneVerified", user.isPhoneVerified());
        model.addAttribute("loggedIn", true);

        return "profile";
    }

    @GetMapping("/add-phone")
    public String showAddPhoneForm() {
        return "add-phone";
    }

    @GetMapping("/verify-phone")
    public String showVerifyPhonePage() {
        return "verify-phone";
    }
}