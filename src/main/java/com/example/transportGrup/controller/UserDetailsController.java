package com.example.transportGrup.controller;

import com.example.transportGrup.dto.PhoneRequestDTO;
import com.example.transportGrup.dto.VerifyPhoneDTO;
import com.example.transportGrup.entity.User;
import com.example.transportGrup.repository.UserRepository;
import com.example.transportGrup.service.AuthenticationService;
import com.example.transportGrup.service.EmailService;
import com.example.transportGrup.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Controller
public class UserDetailsController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserService userService;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    public UserDetailsController(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }


    @GetMapping("/edit-username")
    public String showEditUsernameForm(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";

        User user = userService.findByUsername(principal.getName());
        model.addAttribute("currentUsername", user.getUsername());
        return "edit-username";
    }

    @PostMapping("/edit-username")
    public String updateUsername(@RequestParam String newUsername,
                                 RedirectAttributes redirectAttributes,
                                 Principal principal) {
        if (principal == null) return "redirect:/login";

        User user = userService.findByUsername(principal.getName());

        if (user.getUsername().equals(newUsername)) {
            redirectAttributes.addFlashAttribute("error", "New username can't be the same as old one!");
            return "redirect:/profile";
        }

        user.setUsername(newUsername);
        userRepository.save(user);

        // ✅ Refresh authentication context with new username
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                user.getUsername(), user.getPassword(),  // or null for password, since it's not re-checking
                SecurityContextHolder.getContext().getAuthentication().getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        redirectAttributes.addFlashAttribute("message", "Username updated successfully.");
        return "redirect:/";
    }


    @GetMapping("/edit-email")
    public String editEmailPage(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        model.addAttribute("email", userService.findByUsername(principal.getName()).getEmail());
        return "edit-email";
    }

    @PostMapping("/edit-email")
    public String updateEmail(@RequestParam String newEmail, HttpSession session, RedirectAttributes redirectAttributes, Principal principal) throws MessagingException {
        String username = principal.getName();
        User user = userService.findByUsername(username);

        if (user.getEmail().equals(newEmail)) {
            redirectAttributes.addFlashAttribute("error", "New email can't be the same as old one!");
            return "redirect:/profile";
        } else {
            user.setEmail(newEmail);
            String verificationCode = authenticationService.generateVerificationCode();
            user.setVerificationCode(verificationCode);
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
            user.setEnabled(false);

            userRepository.save(user);
            session.setAttribute("userEmail", newEmail);

            emailService.sendVerificationEmail(newEmail, "Verify your new email",
                    "<p>Please use this code to verify your email change:</p><h3>" + verificationCode + "</h3>");
            redirectAttributes.addFlashAttribute("message", "Email updated successfully.");
            return "redirect:/verify?email=" + newEmail;
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPassword() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email,
                                        RedirectAttributes redirectAttributes) {
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            redirectAttributes.addFlashAttribute("error", "Invalid email format.");
            return "redirect:/forgot-password";
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No user is registered with that email.");
            return "redirect:/forgot-password";
        }

        User user = optionalUser.get();

        String resetCode = generateResetCode();
        user.setVerificationCode(resetCode);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        try {
            emailService.sendVerificationEmail(
                    email,
                    "Reset Your Password",
                    "<p>Your reset code:</p><h3>" + resetCode + "</h3>"
            );
        } catch (MessagingException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to send reset email.");
            return "redirect:/forgot-password";
        }

        redirectAttributes.addFlashAttribute("message", "Reset code sent to your email.");
        return "redirect:/reset-password?email=" + email;
    }


    private String generateResetCode() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000)); // 6-digit code
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam(required = false) String email, Model model) {
        model.addAttribute("email", email);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String email,
                                       @RequestParam String verificationCode,
                                       @RequestParam String newPassword,
                                       RedirectAttributes redirectAttributes) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Invalid email.");
            return "redirect:/reset-password?email=" + email;
        }

        User user = optionalUser.get();

        if (user.getVerificationCode() == null ||
                !user.getVerificationCode().equals(verificationCode)) {
            redirectAttributes.addFlashAttribute("error", "Invalid verification code.");
            return "redirect:/reset-password?email=" + email;
        }

        if (user.getVerificationCodeExpiresAt() == null ||
                user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "Verification code has expired.");
            return "redirect:/reset-password?email=" + email;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("message", "Password reset successful. Please login.");
        return "redirect:/login";
    }

    @GetMapping("/change-phone")
    public String showChangePhoneForm(Model model, HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        model.addAttribute("phoneRequest", new PhoneRequestDTO());
        return "change-phone";
    }

    @PostMapping("/change-phone")
    public String processPhoneChange(@ModelAttribute PhoneRequestDTO phoneRequest,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        Optional<User> currentUserOpt = userRepository.findByEmail(email);
        if (currentUserOpt.isEmpty()) return "redirect:/login";

        User currentUser = currentUserOpt.get();
        String newPhone = phoneRequest.getPhoneNumber();

        Optional<User> phoneTaken = userRepository.findByPhoneNumber(newPhone);
        if (phoneTaken.isPresent() && !phoneTaken.get().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "Phone number already in use.");
            return "redirect:/change-phone";
        }

        currentUser.setTempPhoneNumber(newPhone);
        currentUser.setPhoneVerificationCode(generateVerificationCode());
        currentUser.setPhoneVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));

        userRepository.save(currentUser);

        // Simulate SMS sending (replace with actual SMS provider later)
        System.out.println("SMS sent to " + newPhone + " with code: " + currentUser.getPhoneVerificationCode());

        redirectAttributes.addFlashAttribute("message", "Verification code sent to your phone.");
        return "redirect:/verify-phone";
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000)); // 6-digit code
    }

    @GetMapping("/verify-phone-form")
    public String showPhoneVerificationForm(Model model) {
        // Add it only if not already present (e.g. from FlashAttribute)
        if (!model.containsAttribute("verifyPhoneDto")) {
            model.addAttribute("verifyPhoneDto", new VerifyPhoneDTO());
        }
        return "verify-phone-form"; // this matches your .html file
    }


    @PostMapping("/verify-phone-logged")
    public String processPhoneVerification(@ModelAttribute VerifyPhoneDTO verifyPhoneDto,
                                           HttpSession session,
                                           RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) return "redirect:/login";

        User user = optionalUser.get();
        String submittedCode = verifyPhoneDto.getCode();

        if (user.getPhoneVerificationCode() == null ||
                !user.getPhoneVerificationCode().equals(submittedCode)) {
            redirectAttributes.addFlashAttribute("error", "Invalid verification code.");
            redirectAttributes.addFlashAttribute("verifyPhoneDto", verifyPhoneDto); // <--- ADD THIS
            return "redirect:/verify-phone-form";
        }

        if (user.getPhoneVerificationCodeExpiresAt() == null ||
                user.getPhoneVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "Verification code has expired.");
            redirectAttributes.addFlashAttribute("verifyPhoneDto", verifyPhoneDto); // <--- ADD THIS
            return "redirect:/verify-phone-form";
        }

        user.setPhoneNumber(user.getTempPhoneNumber());
        user.setTempPhoneNumber(null);
        user.setPhoneVerificationCode(null);
        user.setPhoneVerificationCodeExpiresAt(null);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("message", "Phone number verified successfully.");
        return "redirect:/profile";
    }

}
