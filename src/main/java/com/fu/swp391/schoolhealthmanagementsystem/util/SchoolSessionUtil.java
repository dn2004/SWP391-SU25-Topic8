package com.fu.swp391.schoolhealthmanagementsystem.util;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SchoolSession; // Import enum của bạn
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class SchoolSessionUtil {
    private static final LocalTime MORNING_END_TIME = LocalTime.of(11, 59); // Ví dụ: Sáng kết thúc lúc 11:59
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static SchoolSession getSessionFromTime(String timeSlotText) {
        if (timeSlotText == null || timeSlotText.isBlank()) {
            throw new IllegalArgumentException("Buổi học không được để trống hoặc null");
        }
        try {
            LocalTime time = LocalTime.parse(timeSlotText, TIME_FORMATTER);
            if (time.isAfter(MORNING_END_TIME)) {
                return SchoolSession.AFTERNOON;
            } else {
                return SchoolSession.MORNING;
            }
        } catch (DateTimeParseException e) {
            // Nếu không parse được, có thể là text như "Sau ăn trưa"
            // Cần logic phức tạp hơn để suy luận hoặc mặc định
            // Hiện tại, nếu không parse được, coi như không xác định được buổi
            throw new IllegalStateException("Không thể xác định buổi học từ chuỗi: " + timeSlotText, e);
        }
    }
}
