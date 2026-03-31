package com.dat.backend_v2_2.dto.Core;

import com.dat.backend_v2_2.enums.Core.ScheduleLevel;
import com.dat.backend_v2_2.enums.Core.ScheduleLocation;
import com.dat.backend_v2_2.enums.Core.ScheduleShift;
import com.dat.backend_v2_2.enums.Core.Weekday;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalTime;

@Data
public class ClassScheduleResDTO {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ClassScheduleSummary {
        String scheduleId;
        String branchName;
        ScheduleLocation scheduleLocation;
        ScheduleLevel scheduleLevel;
        ScheduleShift scheduleShift;

        // Format thời gian gọn gàng: "08:30" thay vì "08:30:00"
        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime;

        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime;

        Weekday weekday; // Enum (MONDAY, TUESDAY...)
    }

    /**
     * DTO chi tiết: Dùng khi xem chi tiết 1 lớp học
     * Chứa đầy đủ thông tin để hiển thị UI.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL) // Bỏ qua các field null
    public static class ClassScheduleDetail {
        String scheduleId;

        // --- Thông tin Chi nhánh (Flatten object thay vì trả về ID trơ trọi) ---
        Long branchId;
        String branchName;

        // --- Thông tin Lớp học ---
        ScheduleLevel scheduleLevel;      // Trình độ (BEGINNER, INTERMEDIATE...)
        ScheduleShift scheduleShift;      // Ca học (MORNING, EVENING...)
        ScheduleLocation scheduleLocation;              // Phòng học (Location)

        // --- Thời gian ---
        Weekday weekday;

        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime;

        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime;

        // --- Metadata (Thống kê) ---
        Integer totalStudents;    // Sĩ số hiện tại (Calculated field)
        Integer maxCapacity;      // Sĩ số tối đa
        String note;
    }

    /**
     * Tiến
     * Cascading Dropdown (chọn Chi nhánh -> ra Lớp học)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ClassScheduleDropdown {
        String scheduleId; // Value để FE đẩy lên khi submit form
        String displayLabel;   // Label hiển thị (VD: "Thứ 2 (08:30 - 10:00) - Cơ bản - Phòng tập A")
        // Thêm các field phụ để FE có thể tự custom hiển thị nếu muốn
        ScheduleLevel scheduleLevel;
        Weekday weekday;
    }
}
