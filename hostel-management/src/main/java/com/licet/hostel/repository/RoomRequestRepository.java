package com.licet.hostel.repository;

import com.licet.hostel.model.RequestStatus;
import com.licet.hostel.model.Room;
import com.licet.hostel.model.RoomRequest;
import com.licet.hostel.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RoomRequestRepository extends JpaRepository<RoomRequest, Long> {

    List<RoomRequest> findAllByOrderByIdDesc();

    List<RoomRequest> findByStudentOrderByIdDesc(User student);

    List<RoomRequest> findByStudent(User student);

    List<RoomRequest> findByRoom(Room room);

    boolean existsByStudentAndStatusIn(User student, Collection<RequestStatus> statuses);
}
