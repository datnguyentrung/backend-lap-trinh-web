package com.dat.backend_v2_2.dto.Operation;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.dat.backend_v2_2.enums.Operation.AttendanceStatus;
import com.dat.backend_v2_2.enums.Operation.EvaluationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
public class StudentAttendanceDTO {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Response {
        UUID attendanceId;
        UUID enrollmentId;
        UUID studentId;
        String studentName;
        String classScheduleId;
        
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate sessionDate;

        String attendanceStatus; 
        
        Instant checkInTime;
        String recordedByCoachName;
        EvaluationStatus evaluationStatus;
        String note;
        String evaluatedByCoachName;
        Instant updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SimpleResponse {
        UUID attendanceId;
        UUID enrollmentId;
        UUID studentId;
        AttendanceStatus attendanceStatus;
        Instant checkInTime;
        String recordedByCoachName;

        EvaluationStatus evaluationStatus;
        String evaluatedByCoachName;
        String note;
    }

    // ========================================================================
    // REQUEST DTOs (Dữ liệu Frontend gửi lên)
    // ========================================================================

    /**
     * DTO dùng cho API "Điểm danh nhanh" (Mark All Present)
     * Hoặc tạo mới một danh sách điểm danh
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class BatchCreateRequest {
        @NotNull(message = "Schedule ID không được để trống")
        String classScheduleId;

        @NotNull(message = "Ngày học không được để trống")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate sessionDate;
    }

    /**
     * DTO: ManualLogRequest
     * Tiếng Việt: Yêu cầu điểm danh thủ công (cho 1 học viên)
     * * Mục đích: Dùng khi Admin muốn tạo trước dữ liệu xin nghỉ (EXCUSED),
     * hoặc HLV muốn sửa trạng thái của riêng 1 học viên cụ thể.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ManualLogRequest {
        @NotNull(message = "Phải chọn học viên")
        UUID studentId; 

        @NotNull(message = "Phải chọn lớp")
        String classScheduleId;

        @NotNull(message = "Ngày xin phép/đi học")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate sessionDate;

        @NotNull
        AttendanceStatus attendanceStatus; 

        Instant checkInTime; 

        @Size(max = 500)
        String note; 
    }

    /**
     * DTO dùng để cập nhật trạng thái điểm danh của 1 học viên (PATCH)
     * Ví dụ: Sửa từ Vắng -> Có mặt, hoặc Đi muộn
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UpdateStatusRequest {
        @NotNull(message = "Trạng thái điểm danh không được để trống")
        String attendanceStatus; 

        Instant checkInTime;
    }
    /**
     * DTO dùng để cập nhật đánh giá/nhận xét (PATCH)
     * Tách riêng vì đôi khi HLV chấm điểm sau giờ học
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UpdateEvaluationRequest {
        EvaluationStatus evaluationStatus;

        @Size(max = 500, message = "Ghi chú không được quá 500 ký tự")
        String note;
    }

    /**
     * DTO tổng hợp nếu muốn update cả 2 cùng lúc (ít dùng hơn nhưng nên có)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class FullUpdateRequest {
        AttendanceStatus attendanceStatus;
        EvaluationStatus evaluationStatus;
        String note;
    }
}
