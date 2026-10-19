package com.licet.hostel.config;

import com.licet.hostel.model.Role;
import com.licet.hostel.model.Room;
import com.licet.hostel.model.User;
import com.licet.hostel.repository.RoomRepository;
import com.licet.hostel.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Adds the sample admin, student and rooms on the first start (when the database is empty). */
@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, RoomRepository roomRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        userRepository.save(newUser("Hostel Admin", "admin@hostel.com", "admin123", "9999999999", "Administration", Role.ADMIN));
        userRepository.save(newUser("Aksheen Jane A", "student@hostel.com", "student123", "9876543210", "Computer Science", Role.STUDENT));

        roomRepository.save(newRoom("101", 2, "Fan, Cot, Study Table"));
        roomRepository.save(newRoom("102", 3, "Fan, Cot, Wi-Fi"));
        roomRepository.save(newRoom("201", 2, "Fan, Cot, Attached Bathroom"));
        roomRepository.save(newRoom("202", 4, "Fan, Cot, Wi-Fi, Study Table"));
    }

    private User newUser(String name, String email, String password, String phone, String dept, Role role) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(password));
        u.setPhone(phone);
        u.setDepartment(dept);
        u.setRole(role);
        return u;
    }

    private Room newRoom(String number, int capacity, String facilities) {
        Room r = new Room();
        r.setRoomNumber(number);
        r.setCapacity(capacity);
        r.setFacilities(facilities);
        return r;
    }
}
