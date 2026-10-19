package com.licet.hostel.service;

import com.licet.hostel.model.Room;
import com.licet.hostel.repository.RoomRepository;
import com.licet.hostel.repository.RoomRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomRequestRepository requestRepository;

    public RoomService(RoomRepository roomRepository, RoomRequestRepository requestRepository) {
        this.roomRepository = roomRepository;
        this.requestRepository = requestRepository;
    }

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    /** Rooms that are active and still have a free bed. */
    public List<Room> findAvailable() {
        return roomRepository.findByActiveTrue().stream()
                .filter(Room::isAvailable)
                .toList();
    }

    public Room findById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Room not found"));
    }

    /** Create a new room or update an existing one (the number of occupants is never changed here). */
    public void save(Room form) {
        Optional<Room> duplicate = roomRepository.findByRoomNumber(form.getRoomNumber());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(form.getId())) {
            throw new IllegalStateException("Room number already exists");
        }

        Room room = (form.getId() == null) ? new Room() : findById(form.getId());
        room.setRoomNumber(form.getRoomNumber());
        room.setCapacity(form.getCapacity());
        room.setFacilities(form.getFacilities());
        room.setActive(form.isActive());

        if (room.getCapacity() < room.getOccupied()) {
            throw new IllegalStateException("Capacity cannot be less than the current number of occupants");
        }
        roomRepository.save(room);
    }

    @Transactional
    public void delete(Long id) {
        Room room = findById(id);
        if (room.getOccupied() > 0) {
            throw new IllegalStateException("Room has residents and cannot be deleted. Deactivate it instead.");
        }
        requestRepository.deleteAll(requestRepository.findByRoom(room));
        roomRepository.delete(room);
    }
}
