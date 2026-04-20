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

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
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
import com.dat.backend_v2_2.repository.Operation.StudentAttendanceRepository; // Nhớ thêm repo này
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


    @Transactional(rollbackFor = Exception.class)
    public List<StudentAttendanceDTO.Response> filterAttendanceRecords(String classScheduleId, LocalDate sessionDate) {
        List<StudentAttendance> attendances = studentAttendanceRepository
                .findByScheduleIdAndSessionDateWithDetails(classScheduleId, sessionDate);

        if (attendances.isEmpty()) {
            log.info("No attendance records found for {} on {}. Initializing...", classScheduleId, sessionDate);
            StudentAttendanceDTO.BatchCreateRequest batchRequest = new StudentAttendanceDTO.BatchCreateRequest();
            batchRequest.setClassScheduleId(classScheduleId);
            batchRequest.setSessionDate(sessionDate);
            
            // Hardcode một ID coach có thật trong DB để khởi tạo bản ghi
            return this.markAsAbsentByScheduleId(batchRequest, "05824b7a-d636-48e2-8825-9342f4b94b93"); 
        }

        return studentAttendanceMapper.toResponseList(attendances);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateAttendanceEvaluation(String coachId, StudentAttendanceDTO.UpdateEvaluationRequest request, UUID attendanceId) {
        StudentAttendance attendance = studentAttendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy bản ghi"));

        // 🛡️ XỬ LÝ CHỈ LƯU LỜI PHÊ & TRÁNH LỖI UUID "ANONYMOUS"
        if (coachId != null && !coachId.equals("Anonymous")) {
            try {
                // Chỉ set Coach nếu ID là định dạng UUID hợp lệ
                attendance.setEvaluatedByCoach(coachService.getCoachById(coachId));
            } catch (Exception e) {
                log.warn("Could not set coach for evaluation: {}", coachId);
            }
        }

        // Cập nhật lời phê (Note)
        attendance.setNote(request.getNote());
        
        // Chỉ cập nhật status đánh giá nếu có gửi lên (Không bắt buộc)
        if (request.getEvaluationStatus() != null) {
            attendance.setEvaluationStatus(request.getEvaluationStatus());
        }
        
        studentAttendanceRepository.save(attendance);
    }

    // --- Các hàm khác giữ nguyên nhưng phải đảm bảo nằm trong class ---

    @Transactional(rollbackFor = Exception.class)
    public List<StudentAttendanceDTO.Response> markAsAbsentByScheduleId(
            StudentAttendanceDTO.BatchCreateRequest request, String coachId) {

        Coach currentCoach = coachService.getCoachById(coachId);
        if (currentCoach.getCoachStatus() != CoachStatus.ACTIVE) {
            throw new AccessDeniedException("Tài khoản Coach không hoạt động.");
        }

        List<StudentEnrollment> activeEnrollments = studentEnrollmentService
                .getStudentEnrollmentsByClassScheduleId(request.getClassScheduleId());

        if (activeEnrollments.isEmpty()) return Collections.emptyList();

        List<UUID> existingStudentIds = studentAttendanceRepository
                .findStudentIdsByScheduleAndSessionDate(request.getClassScheduleId(), request.getSessionDate());
        Set<UUID> existingIdsSet = new HashSet<>(existingStudentIds);

        List<StudentAttendance> newAttendances = new ArrayList<>();
        for (StudentEnrollment enrollment : activeEnrollments) {
            if (!existingIdsSet.contains(enrollment.getStudent().getUserId())) {
                StudentAttendance attendance = StudentAttendance.builder()
                        .studentEnrollment(enrollment)
                        .sessionDate(request.getSessionDate())
                        .attendanceStatus(AttendanceStatus.ABSENT) 
                        .build();
                newAttendances.add(attendance);
            }
        }

        if (!newAttendances.isEmpty()) {
            studentAttendanceRepository.saveAll(newAttendances);
        }

        List<StudentAttendance> allRecords = studentAttendanceRepository
                .findByScheduleIdAndSessionDateWithDetails(request.getClassScheduleId(), request.getSessionDate());

        return studentAttendanceMapper.toResponseList(allRecords);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateAttendanceStatus(String coachId, StudentAttendanceDTO.UpdateStatusRequest request, UUID attendanceId) {
        Coach currentCoach = coachService.validateCoachAndGetActive(coachId);
        StudentAttendance attendance = studentAttendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy bản ghi điểm danh"));

        attendance.setAttendanceStatus(AttendanceStatus.valueOf(request.getAttendanceStatus().toUpperCase()));
        attendance.setCheckInTime(request.getCheckInTime());
        attendance.setRecordedByCoach(currentCoach);
        studentAttendanceRepository.save(attendance);
    }
}