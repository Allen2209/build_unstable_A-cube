package com.licet.hostel.repository;

import com.licet.hostel.model.Complaint;
import com.licet.hostel.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findAllByOrderByIdDesc();

    List<Complaint> findByStudentOrderByIdDesc(User student);
}
