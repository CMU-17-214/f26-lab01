package edu.cmu.cs214.booking.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.cmu.cs214.booking.domain.Booking;
import edu.cmu.cs214.booking.domain.Room;
import edu.cmu.cs214.booking.domain.TimeInterval;
import edu.cmu.cs214.booking.domain.User;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryBookingStoreTest {

    private final Room roomA = new Room("A", "Alpha", 10);
    private final Room roomB = new Room("B", "Beta", 4);
    private final Room roomC = new Room("C", "Gamma", 6);
    private final User alice = new User("u1", "Alice");
    private final User bob = new User("u2", "Bob");
    private final Booking bookingA =
        new Booking("b1", roomA, alice, new TimeInterval(600, 660));
    private final Booking bookingB =
        new Booking("b2", roomB, bob, new TimeInterval(600, 660));

    private InMemoryBookingStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryBookingStore();
        store.addBooking(bookingA);
        store.addBooking(bookingB);
    }

    @Test
    void bookingsForRoomReturnsOnlyRoomABookings() {
        assertEquals(List.of(bookingA), store.bookingsForRoom(roomA));
    }

    @Test
    void bookingsForRoomReturnsOnlyRoomBBookings() {
        assertEquals(List.of(bookingB), store.bookingsForRoom(roomB));
    }

    @Test
    void bookingsForRoomReturnsEmptyListForRoomWithoutBookings() {
        assertTrue(store.bookingsForRoom(roomC).isEmpty());
    }
}
