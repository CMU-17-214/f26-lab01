package edu.cmu.cs214.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import edu.cmu.cs214.booking.domain.Booking;
import edu.cmu.cs214.booking.domain.Room;
import edu.cmu.cs214.booking.domain.TimeInterval;
import edu.cmu.cs214.booking.domain.User;
import edu.cmu.cs214.booking.repo.InMemoryBookingStore;
import org.junit.jupiter.api.Test;

class BookingServiceTest {

    private final Room roomA = new Room("A", "Alpha", 10);
    private final Room roomB = new Room("B", "Beta", 4);
    private final User alice = new User("u1", "Alice");
    private final User bob = new User("u2", "Bob");

    private BookingService newService() {
        return new BookingService(new InMemoryBookingStore());
    }

    @Test
    void bookConfirmsWhenRoomIsFree() {
        BookingService svc = newService();
        BookingResult r = svc.book(roomA, alice, new TimeInterval(600, 660));
        assertInstanceOf(BookingResult.Confirmed.class, r);
    }

    @Test
    void bookWaitlistsWhenSlotIsTaken() {
        BookingService svc = newService();
        svc.book(roomA, alice, new TimeInterval(600, 660));
        BookingResult r = svc.book(roomA, bob, new TimeInterval(600, 660));
        assertInstanceOf(BookingResult.Waitlisted.class, r);
        BookingResult.Waitlisted w = (BookingResult.Waitlisted) r;
        assertEquals(1, w.position());
    }

    @Test
    void backToBackBookingsAreConfirmed() {
        BookingService svc = newService();
        svc.book(roomA, alice, new TimeInterval(600, 660));
        BookingResult r = svc.book(roomA, bob, new TimeInterval(660, 720));
        assertInstanceOf(BookingResult.Confirmed.class, r);
    }

    @Test
    void sameSlotInDifferentRoomsAreBothConfirmed() {
        BookingService svc = newService();
        svc.book(roomA, alice, new TimeInterval(600, 660));
        BookingResult r = svc.book(roomB, bob, new TimeInterval(600, 660));
        assertInstanceOf(BookingResult.Confirmed.class, r);
    }

    @Test
    void listBookingsReturnsConfirmedBookings() {
        BookingService svc = newService();
        svc.book(roomA, alice, new TimeInterval(600, 660));
        svc.book(roomA, bob, new TimeInterval(660, 720));
        assertEquals(2, svc.listBookings(roomA).size());
    }

    // ========= Milestone2 新增：cancelBooking 取消预约相关测试 =========
    /**
     * 取消不存在的预约ID：系统不做任何修改
     */
    @Test
    void cancelNonExistentBooking_NoChanges() {
        BookingService svc = newService();
        svc.cancelBooking("b999");
        assertEquals(0, svc.listBookings(roomA).size());
    }

    /**
     * 取消预约，候补队列存在可升级用户：成功升级第一个符合条件候补
     */
    @Test
    void cancelBooking_WithMatchingWaitlist_PromotesFirstEntry() {
        BookingService svc = newService();
        TimeInterval slot = new TimeInterval(600, 660);

        BookingResult resAlice = svc.book(roomA, alice, slot);
        assertInstanceOf(BookingResult.Confirmed.class, resAlice);
        Booking confirmedAlice = ((BookingResult.Confirmed) resAlice).booking();

        BookingResult resBob = svc.book(roomA, bob, slot);
        assertInstanceOf(BookingResult.Waitlisted.class, resBob);

        svc.cancelBooking(confirmedAlice.id());

        var bookings = svc.listBookings(roomA);
        assertEquals(1, bookings.size());
        assertEquals(bob, bookings.get(0).user());
    }

    /**
     * 取消预约，但房间仍保留其他正式预约，候补时间段冲突，不升级候补
     */
    @Test
    void cancelBooking_NoPromoteBecauseOtherBookingRemains() {
        BookingService svc = newService();
        TimeInterval t1 = new TimeInterval(600, 660);
        TimeInterval t2 = new TimeInterval(700, 760);

        BookingResult resAlice = svc.book(roomA, alice, t1);
        Booking confirmedAlice = ((BookingResult.Confirmed) resAlice).booking();

        svc.book(roomA, bob, t2);
        svc.cancelBooking(confirmedAlice.id());

        // Alice被取消，Bob本身是正式预约，保留，数量为1
        assertEquals(1, svc.listBookings(roomA).size());
    }
}
