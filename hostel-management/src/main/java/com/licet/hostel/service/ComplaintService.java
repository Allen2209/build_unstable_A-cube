package com.licet.hostel.service;

import com.licet.hostel.model.Complaint;
import com.licet.hostel.model.ComplaintStatus;
import com.licet.hostel.model.User;
import com.licet.hostel.repository.ComplaintRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;

    public ComplaintService(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    public List<Complaint> findAll() {
        return complaintRepository.findAllByOrderByIdDesc();
    }

    public List<Complaint> findByStudent(User student) {
        return complaintRepository.findByStudentOrderByIdDesc(student);
    }

    public void submit(User student, String category, String description) {
        Complaint complaint = new Complaint();
        complaint.setStudent(student);
        complaint.setCategory(category);
        complaint.setDescription(description);
        complaintRepository.save(complaint);
    }

    public void updateStatus(Long id, ComplaintStatus status, String remarks) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Complaint not found"));
        complaint.setStatus(status);
        complaint.setRemarks(remarks);
        complaintRepository.save(complaint);
    }
}
