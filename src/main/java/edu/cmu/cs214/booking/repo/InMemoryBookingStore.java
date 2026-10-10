package edu.cmu.cs214.booking.repo;

import edu.cmu.cs214.booking.domain.Booking;
import edu.cmu.cs214.booking.domain.Room;
import edu.cmu.cs214.booking.domain.WaitlistEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** An in-memory {@link BookingStore}. */
public class InMemoryBookingStore implements BookingStore {

    private final List<Booking> bookings = new ArrayList<>();
    private final List<WaitlistEntry> waitlist = new ArrayList<>();

    @Override
    public void addBooking(Booking booking) {
        bookings.add(booking);
    }

    @Override
    public Optional<Booking> findBooking(String bookingId) {
        return bookings.stream().filter(b -> b.id().equals(bookingId)).findFirst();
    }

    @Override
    public List<Booking> bookingsForRoom(Room room) {
        // The bookings held for a single room.
        return bookings.stream().filter(b -> b.room().id().equals(room.id())).toList();
    }

    @Override
    public List<Booking> allBookings() {
        return List.copyOf(bookings);
    }

    @Override
    public void removeBooking(String bookingId) {
        bookings.removeIf(b -> b.id().equals(bookingId));
    }

    @Override
    public void addWaitlistEntry(WaitlistEntry entry) {
        waitlist.add(entry);
    }

    @Override
    public List<WaitlistEntry> waitlistForRoom(Room room) {
        return waitlist.stream().filter(w -> w.room().id().equals(room.id())).toList();
    }

    @Override
    public void removeWaitlistEntry(String waitlistId) {
        waitlist.removeIf(w -> w.id().equals(waitlistId));
    }
}
