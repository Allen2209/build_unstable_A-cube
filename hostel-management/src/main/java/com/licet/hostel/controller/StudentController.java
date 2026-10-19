package com.licet.hostel.controller;

import com.licet.hostel.model.RequestStatus;
import com.licet.hostel.model.RoomRequest;
import com.licet.hostel.model.User;
import com.licet.hostel.service.ComplaintService;
import com.licet.hostel.service.RoomRequestService;
import com.licet.hostel.service.RoomService;
import com.licet.hostel.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final UserService userService;
    private final RoomService roomService;
    private final RoomRequestService requestService;
    private final ComplaintService complaintService;

    public StudentController(UserService userService, RoomService roomService,
                             RoomRequestService requestService, ComplaintService complaintService) {
        this.userService = userService;
        this.roomService = roomService;
        this.requestService = requestService;
        this.complaintService = complaintService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        User student = userService.findByEmail(principal.getName());
        List<RoomRequest> requests = requestService.findByStudent(student);

        RoomRequest allocation = requests.stream()
                .filter(r -> r.getStatus() == RequestStatus.APPROVED)
                .findFirst().orElse(null);
        long pending = requests.stream()
                .filter(r -> r.getStatus() == RequestStatus.PENDING).count();

        model.addAttribute("student", student);
        model.addAttribute("allocation", allocation);
        model.addAttribute("pendingCount", pending);
        model.addAttribute("complaintCount", complaintService.findByStudent(student).size());
        return "student/dashboard";
    }

    // ---------- Rooms and requests ----------
    @GetMapping("/rooms")
    public String availableRooms(Model model) {
        model.addAttribute("rooms", roomService.findAvailable());
        return "student/rooms";
    }

    @PostMapping("/rooms/{id}/request")
    public String requestRoom(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        User student = userService.findByEmail(principal.getName());
        try {
            requestService.requestRoom(student, id);
            ra.addFlashAttribute("success", "Room request submitted. Status: PENDING");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/student/requests";
    }

    @GetMapping("/requests")
    public String myRequests(Principal principal, Model model) {
        User student = userService.findByEmail(principal.getName());
        model.addAttribute("requests", requestService.findByStudent(student));
        return "student/requests";
    }

    // ---------- Complaints ----------
    @GetMapping("/complaints")
    public String complaints(Principal principal, Model model) {
        User student = userService.findByEmail(principal.getName());
        model.addAttribute("complaints", complaintService.findByStudent(student));
        return "student/complaints";
    }

    @PostMapping("/complaints")
    public String submitComplaint(@RequestParam String category, @RequestParam String description,
                                  Principal principal, RedirectAttributes ra) {
        if (description.isBlank()) {
            ra.addFlashAttribute("error", "Please describe your complaint");
            return "redirect:/student/complaints";
        }
        User student = userService.findByEmail(principal.getName());
        complaintService.submit(student, category, description.trim());
        ra.addFlashAttribute("success", "Complaint submitted successfully");
        return "redirect:/student/complaints";
    }

    // ---------- Profile ----------
    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        model.addAttribute("student", userService.findByEmail(principal.getName()));
        return "student/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam String name, @RequestParam String email,
                                @RequestParam String phone,
                                @RequestParam(defaultValue = "") String department,
                                Principal principal, HttpServletRequest request,
                                RedirectAttributes ra) throws ServletException {
        boolean valid = !name.isBlank()
                && email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
                && phone.matches("\\d{10}");
        if (!valid) {
            ra.addFlashAttribute("error", "Enter a valid name, email and 10-digit contact number");
            return "redirect:/student/profile";
        }

        User student = userService.findByEmail(principal.getName());
        try {
            boolean emailChanged = userService.updateProfile(student, name.trim(), email.trim(), phone, department);
            if (emailChanged) {
                request.logout();   // email is the login id, so the user must log in again
                return "redirect:/login?emailChanged";
            }
            ra.addFlashAttribute("success", "Profile updated successfully");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/student/profile";
    }
}
