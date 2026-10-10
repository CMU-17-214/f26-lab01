package edu.cmu.cs214.booking.service;

import edu.cmu.cs214.booking.domain.Booking;
import edu.cmu.cs214.booking.domain.Room;
import edu.cmu.cs214.booking.domain.TimeInterval;
import edu.cmu.cs214.booking.domain.User;
import edu.cmu.cs214.booking.domain.WaitlistEntry;
import edu.cmu.cs214.booking.repo.BookingStore;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Coordinates bookings and the waitlist. Enforces the core invariant: a room
 * never holds two confirmed bookings whose intervals overlap. Persistence is
 * delegated to a {@link BookingStore}.
 */
public class BookingService {

    private final BookingStore store;
    private int nextBookingSeq = 1;
    private int nextWaitlistSeq = 1;

    public BookingService(BookingStore store) {
        this.store = store;
    }

    /**
     * Attempts to book {@code room} for {@code user} over {@code interval}. If the
     * room is free over that interval the booking is confirmed; otherwise the user
     * is placed on the room's waitlist.
     */
    public BookingResult book(Room room, User user, TimeInterval interval) {
        for (Booking existing : store.bookingsForRoom(room)) {
            if (existing.interval().overlaps(interval)) {
                int position = store.waitlistForRoom(room).size() + 1;
                int seq = nextWaitlistSeq++;
                store.addWaitlistEntry(new WaitlistEntry("w" + seq, room, user, interval, seq));
                return new BookingResult.Waitlisted(position);
            }
        }
        Booking booking = new Booking("b" + nextBookingSeq++, room, user, interval);
        store.addBooking(booking);
        return new BookingResult.Confirmed(booking);
    }

    /** Returns the confirmed bookings for {@code room}. */
    public List<Booking> listBookings(Room room) {
        return store.bookingsForRoom(room);
    }

    /**
     * Cancels the confirmed booking with {@code bookingId}, freeing its slot.
     * Does nothing if no booking has that id. Afterwards, promotes at most one
     * waiter of the freed room, first-come-first-served by {@code seq}, whose
     * interval no longer overlaps any remaining confirmed booking.
     */
    public void cancelBooking(String bookingId) {
        Optional<Booking> cancelled = store.findBooking(bookingId);
        if (cancelled.isEmpty()) {
            return;
        }
        Room room = cancelled.get().room();
        store.removeBooking(bookingId);
        promoteFromWaitlist(room);
    }

    /**
     * Promotes at most one waiter of {@code room}: the earliest by {@code seq}
     * whose interval is now free. Waiters whose interval still overlaps a
     * confirmed booking are skipped and stay on the waitlist.
     */
    private void promoteFromWaitlist(Room room) {
        List<WaitlistEntry> waiters = store.waitlistForRoom(room).stream()
            .sorted(Comparator.comparingInt(WaitlistEntry::seq))
            .toList();
        for (WaitlistEntry waiter : waiters) {
            boolean stillBlocked = store.bookingsForRoom(room).stream()
                .anyMatch(b -> b.interval().overlaps(waiter.interval()));
            if (!stillBlocked) {
                store.removeWaitlistEntry(waiter.id());
                store.addBooking(new Booking("b" + nextBookingSeq++, room, waiter.user(), waiter.interval()));
                return;
            }
        }
    }
}
