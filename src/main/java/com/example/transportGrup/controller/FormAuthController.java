package com.example.transportGrup.controller;

import com.example.transportGrup.dto.LoginUserDto;
import com.example.transportGrup.dto.RegisterUserDto;
import com.example.transportGrup.dto.VerifyUserDto;
import com.example.transportGrup.entity.User;
import com.example.transportGrup.repository.UserRepository;
import com.example.transportGrup.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Random;

@Controller
public class FormAuthController {

    private final AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private final AuthenticationManager authenticationManager;

    public FormAuthController(AuthenticationService authenticationService, AuthenticationManager authenticationManager) {
        this.authenticationService = authenticationService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/signup-form")
    public String registerFromForm(@RequestParam String username,
                                   @RequestParam String email,
                                   @RequestParam String password,
                                   RedirectAttributes redirectAttributes) {
        try {
            RegisterUserDto dto = new RegisterUserDto(username, email, password);
            authenticationService.signup(dto);
            return "redirect:/verify?email=" + email;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/signup";
        }
    }

    @PostMapping("/verify")
    public String verifyUserFromForm(@RequestParam String email,
                                     @RequestParam String verificationCode,
                                     RedirectAttributes redirectAttributes) {
        try {
            VerifyUserDto dto = new VerifyUserDto(email, verificationCode);
            authenticationService.verifyUser(dto);
            redirectAttributes.addFlashAttribute("message", "Account verified successfully!");
            return "redirect:/";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/verify?email=" + email;
        }
    }

    @PostMapping("/login-form")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpServletRequest request,
                        Model model) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // Set auth in context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Store it in session
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());

            return "redirect:/orders/my";
        } catch (Exception e) {
            model.addAttribute("login", true);
            model.addAttribute("mode", "login");
            model.addAttribute("error", "Invalid email or password.");
            return "auth";
        }
    }


    @PostMapping("/send-phone-code")
    public String sendPhoneCode(@RequestParam String phoneNumber, HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");

        if (email == null) return "redirect:/login";

        User user = userRepository.findByEmail(email).orElseThrow();

        String code = generate6DigitCode();
        user.setPhoneNumber(phoneNumber);
        user.setPhoneVerificationCode(code);
        user.setPhoneVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));
        user.setPhoneVerified(false);

        userRepository.save(user);

        System.out.println(">>> Phone verification code: " + code);

        redirectAttributes.addFlashAttribute("phone", phoneNumber);
        return "redirect:/verify-phone-form";
    }

    private String generate6DigitCode() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }

    @PostMapping("/verify-phone")
    public String verifyPhone(@RequestParam String code, HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");

        if (email == null) return "redirect:/login-form";

        User user = userRepository.findByEmail(email).orElseThrow();

        if (user.getPhoneVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "Code expired.");
            return "redirect:/verify-phone-form";
        }

        if (user.getPhoneVerificationCode().equals(code)) {
            user.setPhoneVerified(true);
            user.setPhoneVerificationCode(null);
            user.setPhoneVerificationCodeExpiresAt(null);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("message", "Phone verified!");
            return "redirect:/profile";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid code.");
            return "redirect:/verify-phone-form";
        }
    }

}