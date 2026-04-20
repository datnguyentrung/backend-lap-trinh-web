package com.dat.backend_v2_2.service.Core;

import com.dat.backend_v2_2.dto.Operation.StudentAttendanceDTO; // Import class cha
import com.dat.backend_v2_2.domain.Operation.StudentAttendance;
import com.dat.backend_v2_2.domain.Operation.StudentEnrollment;
import com.dat.backend_v2_2.repository.Operation.StudentAttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final StudentAttendanceRepository attendanceRepository;

    @Transactional
    public void saveEvaluation(StudentAttendanceDTO.ManualLogRequest dto) {
        // 1. Dùng getStudentId() vì trong DTO bạn đặt tên là studentId
        UUID enrollmentUuid = dto.getStudentId(); // Sửa từ getEnrollmentId thành getStudentId

        StudentAttendance attendance = attendanceRepository
            .findByEnrollmentAndDate(enrollmentUuid, dto.getSessionDate())
            .orElseGet(() -> {
                StudentAttendance newEntity = new StudentAttendance();
                StudentEnrollment enrollment = new StudentEnrollment();
                enrollment.setEnrollmentId(enrollmentUuid);
                
                newEntity.setStudentEnrollment(enrollment);
                newEntity.setSessionDate(dto.getSessionDate());
                newEntity.setCreatedAt(Instant.now()); // Dùng Instant.now() thay vì LocalDateTime
                return newEntity;
            });

        attendance.setAttendanceStatus(dto.getAttendanceStatus());
        attendance.setNote(dto.getNote());
        attendance.setUpdatedAt(Instant.now());
    }
}