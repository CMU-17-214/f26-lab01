[1mdiff --git a/src/main/java/edu/cmu/cs214/booking/service/BookingService.java b/src/main/java/edu/cmu/cs214/booking/service/BookingService.java[m
[1mindex 3d1bf0e..e0f30b7 100644[m
[1m--- a/src/main/java/edu/cmu/cs214/booking/service/BookingService.java[m
[1m+++ b/src/main/java/edu/cmu/cs214/booking/service/BookingService.java[m
[36m@@ -42,6 +42,20 @@[m [mpublic class BookingService {[m
         return new BookingResult.Confirmed(booking);[m
     }[m
 [m
[32m+[m
[32m+[m[32m    /**[m
[32m+[m[32m     * Reports whether {@code room} is free over {@code interval}, so callers can[m
[32m+[m[32m     * check availability before attempting to book.[m
[32m+[m[32m     */[m
[32m+[m[32m    public boolean isAvailable(Room room, TimeInterval interval) {[m
[32m+[m[32m        for (Booking b : store.bookingsForRoom(room)) {[m
[32m+[m[32m            if (b.interval().start() >= interval.start() && b.interval().start() < interval.end()) {[m
[32m+[m[32m                return false;[m
[32m+[m[32m            }[m
[32m+[m[32m        }[m
[32m+[m[32m        return true;[m
[32m+[m[32m    }[m
[32m+[m
     /** Returns the confirmed bookings for {@code room}. */[m
     public List<Booking> listBookings(Room room) {[m
         return store.bookingsForRoom(room);[m
[1mdiff --git a/src/test/java/edu/cmu/cs214/booking/service/BookingServiceTest.java b/src/test/java/edu/cmu/cs214/booking/service/BookingServiceTest.java[m
[1mindex b05741a..16455f3 100644[m
[1m--- a/src/test/java/edu/cmu/cs214/booking/service/BookingServiceTest.java[m
[1m+++ b/src/test/java/edu/cmu/cs214/booking/service/BookingServiceTest.java[m
[36m@@ -58,4 +58,5 @@[m [mclass BookingServiceTest {[m
         svc.book(roomA, bob, new TimeInterval(660, 720));[m
         assertEquals(2, svc.listBookings(roomA).size());[m
     }[m
[32m+[m
 }[m
