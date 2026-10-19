package com.licet.hostel.controller;

import com.licet.hostel.model.User;
import com.licet.hostel.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }

    /** After login, send each user to the dashboard of their role. */
    @GetMapping("/home")
    public String home(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return isAdmin ? "redirect:/admin/dashboard" : "redirect:/student/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user, BindingResult result) {
        if (user.getEmail() != null && userService.emailExists(user.getEmail())) {
            result.rejectValue("email", "duplicate", "Email is already registered");
        }
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            result.rejectValue("password", "length", "Password must be at least 6 characters");
        }
        if (result.hasErrors()) {
            return "register";
        }
        userService.registerStudent(user);
        return "redirect:/login?registered";
    }
}
