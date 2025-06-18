package com.fu.swp391.schoolhealthmanagementsystem.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
public class DateUtils {
    public static boolean isWorkday(LocalDate date) {
        if (date == null) return false;
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    public static LocalDate getNextWorkday(LocalDate currentDate) {
        if (currentDate == null) {
            throw new IllegalArgumentException("Ngày hiện tại không được null để tìm ngày làm việc tiếp theo.");
        }

        LocalDate nextDay = currentDate.plusDays(1);
        while (!isWorkday(nextDay)) { // Lặp cho đến khi tìm thấy ngày làm việc
            nextDay = nextDay.plusDays(1);
        }
        return nextDay;
    }
}
