package edu.cmu.cs214.booking.service;

import edu.cmu.cs214.booking.domain.Room;
import edu.cmu.cs214.booking.domain.TimeInterval;
import edu.cmu.cs214.booking.domain.User;
import edu.cmu.cs214.booking.repo.InMemoryBookingStore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IsAvailableTest {

    private final Room roomA = new Room("A", "Alpha", 10);
    private final User alice = new User("u1", "Alice");
    private final User bob = new User("u2", "Bob");

    private BookingService newService() {
        return new BookingService(new InMemoryBookingStore());
    }

    // 场景1：已有预订完全包住了查询区间
    // 已有 600-720（10:00-12:00），查 630-690（10:30-11:30）
    // 正确：不可用 false
    // 补丁错误：可用 true（因为已有预订 start=600 不在 [630, 690) 里）
    @Test
    void existingBookingFullyContainsQueryInterval_shouldBeUnavailable() {
        BookingService svc = newService();
        svc.book(roomA, alice, new TimeInterval(600, 720));

        boolean result = svc.isAvailable(roomA, new TimeInterval(630, 690));
        assertFalse(result,
                "房间已被 600-720 的预订完全占用，查 630-690 应该不可用");
    }

    // 场景2：已有预订从查询区间之前开始，在查询区间中间结束
    // 已有 600-630（10:00-10:30），查 620-660（10:20-11:00）
    // 正确：不可用 false
    // 补丁错误：可用 true（因为已有预订 start=600 不在 [620, 660) 里）
    @Test
    void existingBookingStartsBeforeAndEndsWithinQuery_shouldBeUnavailable() {
        BookingService svc = newService();
        svc.book(roomA, bob, new TimeInterval(600, 630));

        boolean result = svc.isAvailable(roomA, new TimeInterval(620, 660));
        assertFalse(result,
                "房间已有 600-630 的预订，查 620-660 应该不可用");
    }

    // 场景3：完全不重叠，查询在已有预订之前
    // 已有 660-720（11:00-12:00），查 600-630（10:00-10:30）
    // 正确：可用 true
    // 补丁也返回 true
    @Test
    void queryBeforeExistingBooking_shouldBeAvailable() {
        BookingService svc = newService();
        svc.book(roomA, alice, new TimeInterval(660, 720));

        boolean result = svc.isAvailable(roomA, new TimeInterval(600, 630));
        assertTrue(result,
                "查询区间在已有预订之前，应该可用");
    }

    // 场景4：完全不重叠，查询在已有预订之后
    // 已有 600-630（10:00-10:30），查 660-720（11:00-12:00）
    // 正确：可用 true
    // 补丁也返回 true
    @Test
    void queryAfterExistingBooking_shouldBeAvailable() {
        BookingService svc = newService();
        svc.book(roomA, bob, new TimeInterval(600, 630));

        boolean result = svc.isAvailable(roomA, new TimeInterval(660, 720));
        assertTrue(result,
                "查询区间在已有预订之后，应该可用");
    }
}
