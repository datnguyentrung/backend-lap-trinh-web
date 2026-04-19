package com.dat.backend_v2_2.service.Operation;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.backend_v2_2.domain.Core.Coach;
import com.dat.backend_v2_2.domain.Operation.StudentAttendance;
import com.dat.backend_v2_2.domain.Operation.StudentEnrollment;
import com.dat.backend_v2_2.dto.Operation.StudentAttendanceDTO;
import com.dat.backend_v2_2.enums.Core.CoachStatus;
import com.dat.backend_v2_2.enums.Operation.AttendanceStatus;
import com.dat.backend_v2_2.mapper.Operation.StudentAttendanceMapper;
import com.dat.backend_v2_2.repository.Operation.StudentAttendanceRepository;
import com.dat.backend_v2_2.service.Core.CoachService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudentAttendanceService {
    private final StudentAttendanceRepository studentAttendanceRepository;
    private final CoachService coachService;
    private final StudentAttendanceMapper studentAttendanceMapper;
    private final StudentEnrollmentService studentEnrollmentService;

    /**
     * Lọc và lấy danh sách điểm danh.
     * Tự động khởi tạo nếu chưa có dữ liệu cho ngày hôm đó.
     */
    @Transactional(rollbackFor = Exception.class)
    public List<StudentAttendanceDTO.Response> filterAttendanceRecords(String classScheduleId, LocalDate sessionDate) {
        List<StudentAttendance> attendances = studentAttendanceRepository
                .findByScheduleIdAndSessionDateWithDetails(classScheduleId, sessionDate);

        if (attendances.isEmpty()) {
            log.info("No attendance records found for {} on {}. Initializing...", classScheduleId, sessionDate);
            StudentAttendanceDTO.BatchCreateRequest batchRequest = new StudentAttendanceDTO.BatchCreateRequest();
            batchRequest.setClassScheduleId(classScheduleId);
            batchRequest.setSessionDate(sessionDate);
            
            // Thay ID này bằng ID Coach thực tế của ông hoặc lấy từ Security Context
            return this.markAsAbsentByScheduleId(batchRequest, "05824b7a-d636-48e2-8825-9342f4b94b93"); 
        }

        return studentAttendanceMapper.toResponseList(attendances);
    }

    /**
     * Khởi tạo bản ghi điểm danh mặc định là VẮNG (ABSENT)
     */
    @Transactional(rollbackFor = Exception.class)
    public List<StudentAttendanceDTO.Response> markAsAbsentByScheduleId(
            StudentAttendanceDTO.BatchCreateRequest request,
            String coachId) {

        Coach currentCoach = coachService.getCoachById(coachId);
        if (currentCoach.getCoachStatus() != CoachStatus.ACTIVE) {
            throw new AccessDeniedException("Tài khoản Coach không hoạt động.");
        }

        // Lấy danh sách học viên ACTIVE trong lớp
        List<StudentEnrollment> activeEnrollments = studentEnrollmentService
                .getStudentEnrollmentsByClassScheduleId(request.getClassScheduleId());

        if (activeEnrollments.isEmpty()) return Collections.emptyList();

        // Lấy danh sách ID đã có record để tránh tạo trùng
        List<UUID> existingStudentIds = studentAttendanceRepository
                .findStudentIdsByScheduleAndSessionDate(request.getClassScheduleId(), request.getSessionDate());
        Set<UUID> existingIdsSet = new HashSet<>(existingStudentIds);

        List<StudentAttendance> newAttendances = new ArrayList<>();
        for (StudentEnrollment enrollment : activeEnrollments) {
            if (!existingIdsSet.contains(enrollment.getStudent().getUserId())) {
                // 🛡️ ĐẢM BẢO SET TRẠNG THÁI LÀ ABSENT
                StudentAttendance attendance = StudentAttendance.builder()
                        .studentEnrollment(enrollment)
                        .sessionDate(request.getSessionDate())
                        .attendanceStatus(AttendanceStatus.ABSENT) 
                        .checkInTime(null)
                        .recordedByCoach(null)
                        .build();
                newAttendances.add(attendance);
            }
        }

        if (!newAttendances.isEmpty()) {
            studentAttendanceRepository.saveAll(newAttendances);
            log.info("Successfully initialized {} students as ABSENT", newAttendances.size());
        }

        // Truy vấn lại toàn bộ để trả về đầy đủ DTO
        List<StudentAttendance> allRecords = studentAttendanceRepository
                .findByScheduleIdAndSessionDateWithDetails(request.getClassScheduleId(), request.getSessionDate());

        return studentAttendanceMapper.toResponseList(allRecords);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateAttendanceStatus(String coachId, StudentAttendanceDTO.UpdateStatusRequest request, UUID attendanceId) {
        Coach currentCoach = coachService.validateCoachAndGetActive(coachId);
        StudentAttendance attendance = studentAttendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy bản ghi điểm danh"));

        // Rule: EXCUSED không được sửa bằng tay, MAKEUP là trạng thái đặc biệt
        if (attendance.getAttendanceStatus() == AttendanceStatus.EXCUSED) {
            throw new IllegalStateException("Học viên đã xin phép nghỉ, không thể sửa trạng thái.");
        }

        attendance.setAttendanceStatus(AttendanceStatus.valueOf(request.getAttendanceStatus().toUpperCase()));
        attendance.setCheckInTime(request.getCheckInTime());
        attendance.setRecordedByCoach(currentCoach);

        if ("ABSENT".equalsIgnoreCase(request.getAttendanceStatus())) {
            attendance.setCheckInTime(null);
            attendance.setRecordedByCoach(null);
        }
        studentAttendanceRepository.save(attendance);
        log.info("Attendance {} updated to {}", attendanceId, request.getAttendanceStatus());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateAttendanceEvaluation(String coachId, StudentAttendanceDTO.UpdateEvaluationRequest request, UUID attendanceId) {
        Coach currentCoach = coachService.validateCoachAndGetActive(coachId);
        StudentAttendance attendance = studentAttendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy bản ghi"));

        attendance.setEvaluationStatus(request.getEvaluationStatus());
        attendance.setEvaluatedByCoach(currentCoach);
        attendance.setNote(request.getNote());
        
        studentAttendanceRepository.save(attendance);
    }

    @Transactional(rollbackFor = Exception.class)
    public StudentAttendanceDTO.Response createAttendanceRecord(StudentAttendanceDTO.ManualLogRequest request, String coachId) {
        StudentEnrollment enrollment = studentEnrollmentService
                .getEnrollmentByStudentUserIdAndClassScheduleId(request.getStudentId(), request.getClassScheduleId());

        Coach coach = coachService.validateCoachAndGetActive(coachId);

        StudentAttendance attendance = StudentAttendance.builder()
                .studentEnrollment(enrollment)
                .recordedByCoach(coach)
                .sessionDate(request.getSessionDate())
                .attendanceStatus(request.getAttendanceStatus())
                .checkInTime(request.getCheckInTime() != null ? request.getCheckInTime() : Instant.now())
                .note(request.getNote())
                .build();

        return studentAttendanceMapper.toResponse(studentAttendanceRepository.save(attendance));
    }
}