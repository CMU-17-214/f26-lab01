package edu.cmu.cs214.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.cmu.cs214.booking.domain.Booking;
import edu.cmu.cs214.booking.domain.Room;
import edu.cmu.cs214.booking.domain.TimeInterval;
import edu.cmu.cs214.booking.domain.User;
import edu.cmu.cs214.booking.repo.InMemoryBookingStore;
import org.junit.jupiter.api.Test;

class CancelBookingTest {

    private final Room roomA = new Room("A", "Alpha", 10);
    private final Room roomB = new Room("B", "Beta", 10);
    private final User alice = new User("u1", "Alice");
    private final User bob = new User("u2", "Bob");
    private final User carol = new User("u3", "Carol");
    private final User dave = new User("u4", "Dave");
    private final User erin = new User("u5", "Erin");

    private BookingService newService() {
        return new BookingService(new InMemoryBookingStore());
    }

    /** S1: cancelling a confirmed booking removes exactly that booking. */
    @Test
    void cancelRemovesOnlyThatBooking() {
        BookingService svc = newService();
        svc.book(roomA, alice, new TimeInterval(600, 660));
        svc.book(roomA, bob, new TimeInterval(660, 720));
        String id = svc.listBookings(roomA).get(0).id();

        svc.cancelBooking(id);

        assertEquals(1, svc.listBookings(roomA).size());
        assertEquals(bob.id(), svc.listBookings(roomA).get(0).user().id());
    }

    /** S2: cancelling an unknown id changes nothing. */
    @Test
    void cancelUnknownIdDoesNothing() {
        BookingService svc = newService();
        svc.book(roomA, alice, new TimeInterval(600, 660));

        svc.cancelBooking("no-such-booking");

        assertEquals(1, svc.listBookings(roomA).size());
        assertEquals(alice.id(), svc.listBookings(roomA).get(0).user().id());
    }

    /** S3: cancelling promotes the freed slot's waiter into a confirmed booking. */
    @Test
    void cancelPromotesWaitlistedUser() {
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService svc = new BookingService(store);
        svc.book(roomA, alice, new TimeInterval(600, 660));
        svc.book(roomA, bob, new TimeInterval(600, 660)); // waitlisted: slot taken
        String aliceBookingId = svc.listBookings(roomA).get(0).id();

        svc.cancelBooking(aliceBookingId);

        assertEquals(1, svc.listBookings(roomA).size());
        Booking promoted = svc.listBookings(roomA).get(0);
        assertEquals(bob.id(), promoted.user().id());
        assertEquals(new TimeInterval(600, 660), promoted.interval());
        assertEquals(0, store.waitlistForRoom(roomA).size());
    }

    /** S4: a waiter whose interval is still blocked is skipped for a later one that fits. */
    @Test
    void cancelSkipsBlockedWaiterForNextOne() {
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService svc = new BookingService(store);
        svc.book(roomA, alice, new TimeInterval(600, 660));
        svc.book(roomA, carol, new TimeInterval(700, 760));
        svc.book(roomA, bob, new TimeInterval(640, 720)); // waitlisted: overlaps both bookings
        svc.book(roomA, dave, new TimeInterval(600, 640)); // waitlisted: overlaps alice only
        String aliceBookingId = svc.listBookings(roomA).get(0).id();

        svc.cancelBooking(aliceBookingId);

        assertEquals(2, svc.listBookings(roomA).size());
        assertEquals(carol.id(), svc.listBookings(roomA).get(0).user().id());
        assertEquals(dave.id(), svc.listBookings(roomA).get(1).user().id());
        assertEquals(1, store.waitlistForRoom(roomA).size());
        assertEquals(bob.id(), store.waitlistForRoom(roomA).get(0).user().id());
    }

    /** S5: at most one waiter is promoted per cancellation. */
    @Test
    void cancelPromotesAtMostOneWaiter() {
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService svc = new BookingService(store);
        svc.book(roomA, alice, new TimeInterval(600, 760));
        svc.book(roomA, bob, new TimeInterval(600, 660)); // waitlisted
        svc.book(roomA, carol, new TimeInterval(660, 720)); // waitlisted
        String aliceBookingId = svc.listBookings(roomA).get(0).id();

        svc.cancelBooking(aliceBookingId);

        assertEquals(1, svc.listBookings(roomA).size());
        assertEquals(bob.id(), svc.listBookings(roomA).get(0).user().id());
        assertEquals(1, store.waitlistForRoom(roomA).size());
        assertEquals(carol.id(), store.waitlistForRoom(roomA).get(0).user().id());
    }

    /**
     * S4a: a skipped waiter is considered free even when its interval only
     * touches a remaining booking (half-open boundary: touching does not overlap).
     */
    @Test
    void cancelPromotesWaiterWhoseIntervalTouchesRemainingBooking() {
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService svc = new BookingService(store);
        svc.book(roomA, alice, new TimeInterval(600, 660));
        svc.book(roomA, carol, new TimeInterval(660, 720));
        svc.book(roomA, bob, new TimeInterval(640, 700)); // waitlisted: overlaps both bookings
        svc.book(roomA, dave, new TimeInterval(600, 660)); // waitlisted: overlaps alice only
        String aliceBookingId = svc.listBookings(roomA).get(0).id();

        svc.cancelBooking(aliceBookingId);

        assertEquals(2, svc.listBookings(roomA).size());
        Booking carolBooking = svc.listBookings(roomA).get(0);
        Booking promoted = svc.listBookings(roomA).get(1);
        assertEquals(carol.id(), carolBooking.user().id());
        assertEquals(dave.id(), promoted.user().id());
        assertEquals(new TimeInterval(600, 660), promoted.interval());
        // Dave's interval ends exactly where Carol's begins: touching, not overlapping.
        assertEquals(carolBooking.interval().start(), promoted.interval().end());
        assertEquals(1, store.waitlistForRoom(roomA).size());
        assertEquals(bob.id(), store.waitlistForRoom(roomA).get(0).user().id());
    }

    /** S4b: several blocked waiters are skipped before the first one that fits. */
    @Test
    void cancelSkipsSeveralBlockedWaiters() {
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService svc = new BookingService(store);
        svc.book(roomA, alice, new TimeInterval(600, 660));
        svc.book(roomA, carol, new TimeInterval(700, 760));
        svc.book(roomA, bob, new TimeInterval(640, 720)); // waitlisted: overlaps both bookings
        svc.book(roomA, dave, new TimeInterval(600, 760)); // waitlisted: overlaps both bookings
        svc.book(roomA, erin, new TimeInterval(600, 640)); // waitlisted: overlaps alice only
        String aliceBookingId = svc.listBookings(roomA).get(0).id();

        svc.cancelBooking(aliceBookingId);

        assertEquals(2, svc.listBookings(roomA).size());
        assertEquals(carol.id(), svc.listBookings(roomA).get(0).user().id());
        assertEquals(erin.id(), svc.listBookings(roomA).get(1).user().id());
        assertEquals(2, store.waitlistForRoom(roomA).size());
        assertEquals(bob.id(), store.waitlistForRoom(roomA).get(0).user().id());
        assertEquals(dave.id(), store.waitlistForRoom(roomA).get(1).user().id());
    }

    /**
     * S5a: two waiters whose intervals overlap each other both fit after the
     * cancel, yet only the earliest is promoted; the other stays on the waitlist.
     */
    @Test
    void cancelPromotesOnlyOneOfOverlappingWaiters() {
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService svc = new BookingService(store);
        svc.book(roomA, alice, new TimeInterval(600, 720));
        svc.book(roomA, bob, new TimeInterval(600, 660)); // waitlisted
        svc.book(roomA, carol, new TimeInterval(600, 640)); // waitlisted, overlaps bob
        String aliceBookingId = svc.listBookings(roomA).get(0).id();

        svc.cancelBooking(aliceBookingId);

        assertEquals(1, svc.listBookings(roomA).size());
        assertEquals(bob.id(), svc.listBookings(roomA).get(0).user().id());
        assertEquals(1, store.waitlistForRoom(roomA).size());
        assertEquals(carol.id(), store.waitlistForRoom(roomA).get(0).user().id());
    }

    /** S6: when every waiter is still blocked by a remaining booking, promote no one. */
    @Test
    void cancelPromotesNoOneWhenNoWaiterFits() {
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService svc = new BookingService(store);
        svc.book(roomA, alice, new TimeInterval(600, 660));
        svc.book(roomA, carol, new TimeInterval(700, 760));
        svc.book(roomA, bob, new TimeInterval(600, 760)); // waitlisted: overlaps both bookings
        svc.book(roomA, dave, new TimeInterval(640, 720)); // waitlisted: overlaps both bookings
        String aliceBookingId = svc.listBookings(roomA).get(0).id();

        svc.cancelBooking(aliceBookingId);

        assertEquals(1, svc.listBookings(roomA).size());
        assertEquals(carol.id(), svc.listBookings(roomA).get(0).user().id());
        assertEquals(2, store.waitlistForRoom(roomA).size());
        assertEquals(bob.id(), store.waitlistForRoom(roomA).get(0).user().id());
        assertEquals(dave.id(), store.waitlistForRoom(roomA).get(1).user().id());
    }

    /** S6a: a room with no waiters promotes nobody when a booking is cancelled. */
    @Test
    void cancelWithEmptyWaitlistPromotesNoOne() {
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService svc = new BookingService(store);
        svc.book(roomA, alice, new TimeInterval(600, 660));
        String aliceBookingId = svc.listBookings(roomA).get(0).id();

        svc.cancelBooking(aliceBookingId);

        assertEquals(0, svc.listBookings(roomA).size());
        assertEquals(0, store.waitlistForRoom(roomA).size());
    }

    /** S6b: promotion only touches the freed room's waitlist, never another room's. */
    @Test
    void cancelPromotesOnlyWaitersOfFreedRoom() {
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService svc = new BookingService(store);
        svc.book(roomA, alice, new TimeInterval(600, 660));
        svc.book(roomA, bob, new TimeInterval(600, 660)); // waitlisted on A
        svc.book(roomB, carol, new TimeInterval(600, 660));
        svc.book(roomB, dave, new TimeInterval(600, 660)); // waitlisted on B
        String aliceBookingId = svc.listBookings(roomA).get(0).id();

        svc.cancelBooking(aliceBookingId);

        assertEquals(1, svc.listBookings(roomA).size());
        assertEquals(bob.id(), svc.listBookings(roomA).get(0).user().id());
        assertEquals(0, store.waitlistForRoom(roomA).size());
        // Room B is untouched: carol keeps the booking, dave stays on the waitlist.
        assertEquals(1, svc.listBookings(roomB).size());
        assertEquals(carol.id(), svc.listBookings(roomB).get(0).user().id());
        assertEquals(1, store.waitlistForRoom(roomB).size());
        assertEquals(dave.id(), store.waitlistForRoom(roomB).get(0).user().id());
    }
}
