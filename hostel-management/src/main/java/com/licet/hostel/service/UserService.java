package com.licet.hostel.service;

import com.licet.hostel.model.RequestStatus;
import com.licet.hostel.model.Role;
import com.licet.hostel.model.Room;
import com.licet.hostel.model.RoomRequest;
import com.licet.hostel.model.User;
import com.licet.hostel.repository.ComplaintRepository;
import com.licet.hostel.repository.RoomRepository;
import com.licet.hostel.repository.RoomRequestRepository;
import com.licet.hostel.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final RoomRequestRepository requestRepository;
    private final ComplaintRepository complaintRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoomRepository roomRepository,
                       RoomRequestRepository requestRepository, ComplaintRepository complaintRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.requestRepository = requestRepository;
        this.complaintRepository = complaintRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Student not found"));
    }

    public List<User> findAllStudents() {
        return userRepository.findByRole(Role.STUDENT);
    }

    /** Used by the registration page and by "Add Student" in the admin panel. */
    public void registerStudent(User user) {
        user.setId(null);
        user.setRole(Role.STUDENT);                       // nobody can register as ADMIN
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    /** Admin: create a new student or update an existing one. */
    public void saveStudentByAdmin(User form) {
        if (form.getId() == null) {
            if (emailExists(form.getEmail())) {
                throw new IllegalStateException("Email is already registered");
            }
            registerStudent(form);
            return;
        }
        User student = findById(form.getId());
        checkEmailNotUsedByOthers(form.getEmail(), student.getId());
        student.setName(form.getName());
        student.setEmail(form.getEmail());
        student.setPhone(form.getPhone());
        student.setDepartment(form.getDepartment());
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            student.setPassword(passwordEncoder.encode(form.getPassword()));
        }
        userRepository.save(student);
    }

    /** Student: update own profile. Returns true if the email (login id) was changed. */
    public boolean updateProfile(User student, String name, String email, String phone, String department) {
        checkEmailNotUsedByOthers(email, student.getId());
        boolean emailChanged = !student.getEmail().equals(email);
        student.setName(name);
        student.setEmail(email);
        student.setPhone(phone);
        student.setDepartment(department);
        userRepository.save(student);
        return emailChanged;
    }

    /** Deleting a student also frees the bed they were using and removes their requests and complaints. */
    @Transactional
    public void deleteStudent(Long id) {
        User student = findById(id);
        List<RoomRequest> requests = requestRepository.findByStudent(student);
        for (RoomRequest r : requests) {
            if (r.getStatus() == RequestStatus.APPROVED) {
                Room room = r.getRoom();
                room.setOccupied(Math.max(0, room.getOccupied() - 1));
                roomRepository.save(room);
            }
        }
        requestRepository.deleteAll(requests);
        complaintRepository.deleteAll(complaintRepository.findByStudentOrderByIdDesc(student));
        userRepository.delete(student);
    }

    private void checkEmailNotUsedByOthers(String email, Long ownId) {
        userRepository.findByEmail(email).ifPresent(other -> {
            if (!other.getId().equals(ownId)) {
                throw new IllegalStateException("Email is already used by another account");
            }
        });
    }
}
