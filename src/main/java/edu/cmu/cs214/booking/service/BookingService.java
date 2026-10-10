package edu.cmu.cs214.booking.service;

import edu.cmu.cs214.booking.domain.Booking;
import edu.cmu.cs214.booking.domain.Room;
import edu.cmu.cs214.booking.domain.TimeInterval;
import edu.cmu.cs214.booking.domain.User;
import edu.cmu.cs214.booking.domain.WaitlistEntry;
import edu.cmu.cs214.booking.repo.BookingStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Coordinates booking and waitlist logic.
 */
public class BookingService {
    private final BookingStore store;
    private int nextBookingId;
    private int nextWaitlistSeq;

    public BookingService(BookingStore store) {
        this.store = store;
        this.nextBookingId = 1;
        this.nextWaitlistSeq = 1;
    }

    /**
     * Attempt to book a room for a user.
     * If the interval overlaps existing confirmed bookings for that room, add to waitlist.
     */
    public BookingResult book(Room room, User user, TimeInterval interval) {
        List<Booking> roomBookings = store.bookingsForRoom(room);
        boolean overlaps = roomBookings.stream()
                .anyMatch(b -> b.interval().overlaps(interval));
        if (!overlaps) {
            Booking booking = new Booking(Integer.toString(nextBookingId++), room, user, interval);
            store.addBooking(booking);
            return new BookingResult.Confirmed(booking);
        } else {
            int position = nextWaitlistSeq;
            WaitlistEntry entry = new WaitlistEntry(Integer.toString(nextWaitlistSeq++), room, user, interval, position);
            store.addWaitlistEntry(entry);
            return new BookingResult.Waitlisted(position);
        }
    }

    /**
     * Get confirmed bookings for a room.
     */
    public List<Booking> listBookings(Room room) {
        return store.bookingsForRoom(room);
    }

    /**
     * Cancel a booking by bookingId.
     * Remove the booking. Then scan waitlist (in seq order) and promote the FIRST waitlist entry
     * whose interval does not overlap any remaining confirmed bookings of this room.
     * Promote at most one entry.
     */
    public void cancelBooking(String bookingId) {
        Optional<Booking> bookingOpt = store.findBooking(bookingId);
        if (bookingOpt.isEmpty()) {
            return;
        }
        Booking canceledBooking = bookingOpt.get();
        Room targetRoom = canceledBooking.room();

        // 步骤1：从存储中移除被取消的这条预约
        store.removeBooking(bookingId);

        // 步骤2：获取删除完成之后，该房间剩下的全部已确认预约
        List<Booking> remainingBookings = store.bookingsForRoom(targetRoom);

        // 获取当前房间全部候补队列（拷贝副本，防止遍历中修改原集合抛并发异常）
        List<WaitlistEntry> waitlist = new ArrayList<>(store.waitlistForRoom(targetRoom));

        // 步骤3：按候补原有顺序遍历，寻找第一个可以升级的候补条目
        for (WaitlistEntry entry : waitlist) {
            TimeInterval candidateInterval = entry.interval();
            boolean noOverlap = true;
            // 判断该候补的时间段，是否和剩余任意正式预约发生时间重叠
            for (Booking b : remainingBookings) {
                if (b.interval().overlaps(candidateInterval)) {
                    noOverlap = false;
                    break;
                }
            }
            // 时间段完全不冲突，可以升级该候补用户
            if (noOverlap) {
                // 创建新的正式预约并保存
                store.removeWaitlistEntry(entry.id());
                Booking newBooking = new Booking(
                        Integer.toString(nextBookingId++),
                        targetRoom,
                        entry.user(),
                        candidateInterval
                );
                store.addBooking(newBooking);
                break; // 最多只升级1人，完成后跳出循环
            }
        }
    }
}
