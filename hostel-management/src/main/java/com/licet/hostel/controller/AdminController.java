package com.licet.hostel.controller;

import com.licet.hostel.model.ComplaintStatus;
import com.licet.hostel.model.RequestStatus;
import com.licet.hostel.model.Room;
import com.licet.hostel.model.User;
import com.licet.hostel.service.ComplaintService;
import com.licet.hostel.service.RoomRequestService;
import com.licet.hostel.service.RoomService;
import com.licet.hostel.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final RoomService roomService;
    private final RoomRequestService requestService;
    private final ComplaintService complaintService;

    public AdminController(UserService userService, RoomService roomService,
                           RoomRequestService requestService, ComplaintService complaintService) {
        this.userService = userService;
        this.roomService = roomService;
        this.requestService = requestService;
        this.complaintService = complaintService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalRooms", roomService.findAll().size());
        model.addAttribute("availableRooms", roomService.findAvailable().size());
        model.addAttribute("totalStudents", userService.findAllStudents().size());
        model.addAttribute("pendingRequests", requestService.findAll().stream()
                .filter(r -> r.getStatus() == RequestStatus.PENDING).count());
        model.addAttribute("openComplaints", complaintService.findAll().stream()
                .filter(c -> c.getStatus() != ComplaintStatus.RESOLVED).count());
        return "admin/dashboard";
    }

    // ---------- Rooms (CRUD) ----------
    @GetMapping("/rooms")
    public String rooms(Model model) {
        model.addAttribute("rooms", roomService.findAll());
        return "admin/rooms";
    }

    @GetMapping("/rooms/new")
    public String newRoom(Model model) {
        model.addAttribute("room", new Room());
        return "admin/room-form";
    }

    @GetMapping("/rooms/edit/{id}")
    public String editRoom(@PathVariable Long id, Model model) {
        model.addAttribute("room", roomService.findById(id));
        return "admin/room-form";
    }

    @PostMapping("/rooms/save")
    public String saveRoom(@Valid @ModelAttribute("room") Room room, BindingResult result,
                           Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "admin/room-form";
        }
        try {
            roomService.save(room);
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "admin/room-form";
        }
        ra.addFlashAttribute("success", "Room saved successfully");
        return "redirect:/admin/rooms";
    }

    @PostMapping("/rooms/delete/{id}")
    public String deleteRoom(@PathVariable Long id, RedirectAttributes ra) {
        try {
            roomService.delete(id);
            ra.addFlashAttribute("success", "Room deleted");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/rooms";
    }

    // ---------- Students (CRUD) ----------
    @GetMapping("/students")
    public String students(Model model) {
        model.addAttribute("students", userService.findAllStudents());
        return "admin/students";
    }

    @GetMapping("/students/new")
    public String newStudent(Model model) {
        model.addAttribute("student", new User());
        return "admin/student-form";
    }

    @GetMapping("/students/edit/{id}")
    public String editStudent(@PathVariable Long id, Model model) {
        model.addAttribute("student", userService.findById(id));
        return "admin/student-form";
    }

    @PostMapping("/students/save")
    public String saveStudent(@Valid @ModelAttribute("student") User student, BindingResult result,
                              Model model, RedirectAttributes ra) {
        boolean isNew = student.getId() == null;
        if (isNew && (student.getPassword() == null || student.getPassword().length() < 6)) {
            result.rejectValue("password", "length", "Password must be at least 6 characters");
        }
        if (result.hasErrors()) {
            return "admin/student-form";
        }
        try {
            userService.saveStudentByAdmin(student);
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "admin/student-form";
        }
        ra.addFlashAttribute("success", "Student saved successfully");
        return "redirect:/admin/students";
    }

    @PostMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable Long id, RedirectAttributes ra) {
        userService.deleteStudent(id);
        ra.addFlashAttribute("success", "Student record deleted");
        return "redirect:/admin/students";
    }

    // ---------- Room requests ----------
    @GetMapping("/requests")
    public String requests(Model model) {
        model.addAttribute("requests", requestService.findAll());
        return "admin/requests";
    }

    @PostMapping("/requests/approve/{id}")
    public String approve(@PathVariable Long id, RedirectAttributes ra) {
        try {
            requestService.approve(id);
            ra.addFlashAttribute("success", "Request approved and room allocated");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/requests";
    }

    @PostMapping("/requests/reject/{id}")
    public String reject(@PathVariable Long id, RedirectAttributes ra) {
        try {
            requestService.reject(id);
            ra.addFlashAttribute("success", "Request rejected");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/requests";
    }

    // ---------- Complaints ----------
    @GetMapping("/complaints")
    public String complaints(Model model) {
        model.addAttribute("complaints", complaintService.findAll());
        model.addAttribute("statuses", ComplaintStatus.values());
        return "admin/complaints";
    }

    @PostMapping("/complaints/update/{id}")
    public String updateComplaint(@PathVariable Long id, @RequestParam ComplaintStatus status,
                                  @RequestParam(defaultValue = "") String remarks, RedirectAttributes ra) {
        try {
            complaintService.updateStatus(id, status, remarks.trim());
            ra.addFlashAttribute("success", "Complaint updated");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/complaints";
    }
}
