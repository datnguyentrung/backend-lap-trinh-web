package com.dat.backend_v2_2.dto.Core;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * DTO dùng để hứng dữ liệu đánh giá học viên từ màn hình điểm danh.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttendanceDTO {

    private String attendanceStatus;
    String enrollmentId; 

    // Trạng thái điểm danh: PRESENT (Có mặt), ABSENT (Vắng mặt)
    String status;

    // Lời phê/Đánh giá của Huấn luyện viên (Task P2 - tối đa 255 ký tự)
    String note;
}
