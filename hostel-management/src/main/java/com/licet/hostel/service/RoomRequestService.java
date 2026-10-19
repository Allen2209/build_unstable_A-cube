package com.licet.hostel.service;

import com.licet.hostel.model.RequestStatus;
import com.licet.hostel.model.Room;
import com.licet.hostel.model.RoomRequest;
import com.licet.hostel.model.User;
import com.licet.hostel.repository.RoomRepository;
import com.licet.hostel.repository.RoomRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoomRequestService {

    private final RoomRequestRepository requestRepository;
    private final RoomRepository roomRepository;

    public RoomRequestService(RoomRequestRepository requestRepository, RoomRepository roomRepository) {
        this.requestRepository = requestRepository;
        this.roomRepository = roomRepository;
    }

    public List<RoomRequest> findAll() {
        return requestRepository.findAllByOrderByIdDesc();
    }

    public List<RoomRequest> findByStudent(User student) {
        return requestRepository.findByStudentOrderByIdDesc(student);
    }

    /** Student asks for a room. The request is saved with PENDING status. */
    public void requestRoom(User student, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalStateException("Room not found"));

        if (!room.isAvailable()) {
            throw new IllegalStateException("The selected room is no longer available");
        }
        if (requestRepository.existsByStudentAndStatusIn(student,
                List.of(RequestStatus.PENDING, RequestStatus.APPROVED))) {
            throw new IllegalStateException("You already have a pending request or an active allocation");
        }

        RoomRequest request = new RoomRequest();
        request.setStudent(student);
        request.setRoom(room);
        requestRepository.save(request);
    }

    /** Admin approves: one bed of the room is taken. */
    @Transactional
    public void approve(Long requestId) {
        RoomRequest request = findPending(requestId);
        Room room = request.getRoom();

        if (!room.isAvailable()) {
            throw new IllegalStateException("Room is full or inactive. Request cannot be approved");
        }
        room.setOccupied(room.getOccupied() + 1);
        roomRepository.save(room);

        request.setStatus(RequestStatus.APPROVED);
        requestRepository.save(request);
    }

    @Transactional
    public void reject(Long requestId) {
        RoomRequest request = findPending(requestId);
        request.setStatus(RequestStatus.REJECTED);
        requestRepository.save(request);
    }

    private RoomRequest findPending(Long requestId) {
        RoomRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("Request not found"));
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("This request has already been processed");
        }
        return request;
    }
}
