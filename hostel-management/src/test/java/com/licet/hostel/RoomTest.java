package com.licet.hostel;

import com.licet.hostel.model.Room;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomTest {

    @Test
    void roomIsAvailableOnlyWhenActiveAndNotFull() {
        Room room = new Room();
        room.setCapacity(2);
        room.setOccupied(1);
        assertTrue(room.isAvailable());

        room.setOccupied(2);
        assertFalse(room.isAvailable());

        room.setOccupied(0);
        room.setActive(false);
        assertFalse(room.isAvailable());
    }
}
