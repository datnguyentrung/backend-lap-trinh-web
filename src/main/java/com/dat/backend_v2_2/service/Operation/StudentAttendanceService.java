package com.dat.backend_v2_2.service.Operation;

import com.dat.backend_v2_2.domain.Core.ClassSchedule;
import com.dat.backend_v2_2.domain.Core.Coach;
import com.dat.backend_v2_2.domain.Core.Student;
import com.dat.backend_v2_2.domain.Operation.StudentAttendance;
import com.dat.backend_v2_2.domain.Operation.StudentEnrollment;
import com.dat.backend_v2_2.dto.Operation.StudentAttendanceDTO;
import com.dat.backend_v2_2.enums.Core.CoachStatus;
import com.dat.backend_v2_2.enums.Core.ScheduleStatus;
import com.dat.backend_v2_2.enums.Core.StudentStatus;
import com.dat.backend_v2_2.enums.Operation.AttendanceStatus;
import com.dat.backend_v2_2.enums.Operation.StudentEnrollmentStatus;
import com.dat.backend_v2_2.mapper.Operation.StudentAttendanceMapper;
import com.dat.backend_v2_2.repository.Operation.StudentAttendanceRepository;
import com.dat.backend_v2_2.service.Core.CoachService;
import com.dat.backend_v2_2.service.Security.AuthTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudentAttendanceService {
    private final StudentAttendanceRepository studentAttendanceRepository;
    private final CoachService coachService;
    private final StudentAttendanceMapper studentAttendanceMapper;
    private final StudentEnrollmentService studentEnrollmentService;
    private final AuthTokenService authTokenService;

    /**
     * Cập nhật trạng thái điểm danh của học viên.
     * <p>
     * Chức năng: Cho phép HLV cập nhật trạng thái điểm danh (PRESENT, ABSENT, LATE, EXCUSED)
     * và thời gian check-in của một học viên cụ thể.
     * <p>
     * Business Rules:
     * - Chỉ HLV có trạng thái ACTIVE mới được phép thực hiện
     * - Tự động ghi nhận HLV đã thực hiện điểm danh
     * - Cập nhật timestamp để audit trail
     *
     * @param coachId ID của HLV thực hiện thao tác
     * @param request Thông tin cập nhật (trạng thái và thời gian check-in)
     * @param attendanceId ID của bản ghi điểm danh cần cập nhật
     * @throws NoSuchElementException nếu không tìm thấy bản ghi điểm danh
     * @throws AccessDeniedException nếu HLV không ở trạng thái ACTIVE
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateAttendanceStatus(
            String coachId,
            StudentAttendanceDTO.UpdateStatusRequest request,
            UUID attendanceId
    ){
        // 1. Validate Coach (Helper method đã tách ra)
        Coach currentCoach = coachService.validateCoachAndGetActive(coachId);

        // 2. Lấy dữ liệu
        StudentAttendance attendance = studentAttendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Không tìm thấy bản ghi điểm danh với ID: " + attendanceId
                ));

        // Business Rule: Không được phép thay đổi bản ghi đã được xin phép (EXCUSED)
        if (attendance.getAttendanceStatus() == AttendanceStatus.EXCUSED || request.getAttendanceStatus() == AttendanceStatus.MAKEUP) {
            log.warn("Coach {} attempted to modify excused attendance record {}",
                    currentCoach.getFullName(), attendanceId);
            throw new IllegalStateException("Không thể thay đổi bản ghi điểm danh đã được xin phép.");
        }

        // 3. Update logic
        attendance.setAttendanceStatus(request.getAttendanceStatus());
        attendance.setCheckInTime(request.getCheckInTime());
        attendance.setRecordedByCoach(currentCoach);

        if (request.getAttendanceStatus() == AttendanceStatus.ABSENT){
            attendance.setCheckInTime(null); // Nếu vắng thì không có check-in time
            attendance.setRecordedByCoach(null);
            attendance.setEvaluationStatus(null);
            attendance.setEvaluatedByCoach(null);
        }

//        // Gửi thông báo cho học viên (trừ trạng thái ABSENT không gửi)
//        if (request.getAttendanceStatus() != AttendanceStatus.ABSENT) {
//            sendAttendanceNotification(attendance);
//        }

        log.info("Coach {} updated attendance {} status to {}",
                currentCoach.getFullName(), attendanceId, request.getAttendanceStatus());
    }

    /**
     * Cập nhật đánh giá (evaluation) của học viên sau buổi học.
     * <p>
     * Chức năng: Cho phép HLV đánh giá học viên sau buổi học (PASSED, FAILED, GOOD, EXCELLENT)
     * và thêm ghi chú về hiệu suất học tập.
     * <p>
     * Business Rules:
     * - Chỉ HLV có trạng thái ACTIVE mới được phép thực hiện
     * - Tự động ghi nhận HLV đã thực hiện đánh giá
     * - Có thể đánh giá sau khi đã điểm danh
     * - Ghi chú không được vượt quá 500 ký tự (validation ở DTO)
     *
     * @param coachId ID của HLV thực hiện thao tác
     * @param request Thông tin đánh giá (trạng thái đánh giá và ghi chú)
     * @param attendanceId ID của bản ghi điểm danh cần cập nhật
     * @throws NoSuchElementException nếu không tìm thấy bản ghi điểm danh
     * @throws AccessDeniedException nếu HLV không ở trạng thái ACTIVE
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateAttendanceEvaluation(
            String coachId,
            StudentAttendanceDTO.UpdateEvaluationRequest request,
            UUID attendanceId
    ){
        // Validate coach status
        Coach currentCoach = coachService.validateCoachAndGetActive(coachId);

        // Fetch and validate attendance record
        StudentAttendance attendance = studentAttendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new NoSuchElementException(
                        String.format("Không tìm thấy bản ghi điểm danh với ID: %s", attendanceId)
                ));

        // Update evaluation
        attendance.setEvaluationStatus(request.getEvaluationStatus());
        attendance.setEvaluatedByCoach(currentCoach);
        attendance.setNote(request.getNote());

//        // Gửi thông báo đánh giá cho học viên (trừ trạng thái PENDING)
//        if (request.getEvaluationStatus() != null &&
//            request.getEvaluationStatus() != com.dat.backend_v2_2.enums.Operation.EvaluationStatus.PENDING) {
//            sendEvaluationNotification(attendance);
//        }

        log.info("Coach {} updated evaluation for attendance record {} to status {}",
                currentCoach.getFullName(), attendanceId, request.getEvaluationStatus());
    }

    /**
     * Lọc và lấy danh sách điểm danh cho một buổi học cụ thể.
     *
     */
    @Transactional(readOnly = true)
    public List<StudentAttendanceDTO.Response> filterAttendanceRecords(
            String classScheduleId,
            LocalDate sessionDate
    ) {
        List<StudentAttendance> attendances = studentAttendanceRepository
                .findByScheduleIdAndSessionDateWithDetails(classScheduleId, sessionDate);
        return studentAttendanceMapper.toResponseList(attendances);
    }

    /**
     * Khởi tạo dữ liệu điểm danh cho một buổi học cụ thể.
     * <p>
     * Logic xử lý:
     * 1. Validate trạng thái của HLV và Lớp học.
     * 2. Lấy danh sách học viên đang active trong lớp.
     * 3. Kiểm tra dữ liệu cũ: Nếu Admin đã tạo trước (ví dụ: Xin nghỉ phép, Học bù), hệ thống sẽ GIỮ NGUYÊN.
     * 4. Chỉ tạo mới (status = ABSENT) cho những học viên chưa có dữ liệu.
     * 5. Trả về danh sách đầy đủ (Cũ + Mới) để Frontend hiển thị ngay lập tức (UX Optimization).
     *
     * @param request Chứa classScheduleId và sessionDate.
     * @param coachId ID của HLV thực hiện thao tác.
     * @return Danh sách điểm danh đầy đủ (Full state) để update UI.
     */
    @Transactional(rollbackFor = Exception.class)
    public List<StudentAttendanceDTO.Response> markAsAbsentByScheduleId(
            StudentAttendanceDTO.BatchCreateRequest request,
            String coachId) {

        // ========================================================================
        // STEP 1: BUSINESS VALIDATION
        // Đảm bảo các thực thể liên quan đều đang ở trạng thái ACTIVE
        // ========================================================================

        Coach currentCoach = coachService.getCoachById(coachId); // Hàm này tự throw Exception nếu không tìm thấy

        if (currentCoach.getCoachStatus() != CoachStatus.ACTIVE) {
            log.warn("Security Alert: Coach {} (Status: {}) tried to perform action but is inactive.",
                    currentCoach.getFullName(), currentCoach.getCoachStatus());
            throw new AccessDeniedException("Tài khoản của bạn đã bị khóa hoặc không hoạt động.");
        }

        // ========================================================================
        // STEP 2: DATA PREPARATION (ROSTER & EXISTING RECORDS)
        // ========================================================================

        // Lấy danh sách "sĩ số lớp" hiện tại (Chỉ lấy học viên đang Active)
        List<StudentEnrollment> activeStudents = studentEnrollmentService
                .getStudentEnrollmentsByClassScheduleId(request.getClassScheduleId());

        if (activeStudents.isEmpty()) return Collections.emptyList();

        // Lấy danh sách điểm danh ĐÃ TỒN TẠI trong DB (Partial Data)
        // Mục đích: Tránh ghi đè dữ liệu Admin đã nhập trước đó (Ví dụ: Trạng thái EXCUSED/PRESENT)
        List<UUID> existingStudentIds = studentAttendanceRepository
                .findStudentIdsByScheduleAndSessionDate(
                        request.getClassScheduleId(),
                        request.getSessionDate()
                );

        // Tạo Set ID của những người đã có record để check cho nhanh (O(1))
        Set<UUID> existingStudentIdsSet = new HashSet<>(existingStudentIds);

        // ========================================================================
        // STEP 3: BATCH PROCESSING (IDENTIFY MISSING & CREATE)
        // ========================================================================

        List<StudentAttendance> newAttendances = new ArrayList<>();
        for (StudentEnrollment enrollment : activeStudents) {
            if (!existingStudentIdsSet.contains(enrollment.getStudent().getUserId())) {
                StudentAttendance attendance = StudentAttendance.builder()
                        .studentEnrollment(enrollment)
                        .sessionDate(request.getSessionDate())
                        .attendanceStatus(AttendanceStatus.ABSENT) // Mặc định là VẮNG
                        .checkInTime(null)                         // Chưa có check-in
                        .recordedByCoach(null)                     // Chưa có người ghi nhận (System init)
                        .note(null)
                        .build();
                newAttendances.add(attendance);
            } else {
                log.info("Attendance record already exists for student {}, skipping...",
                        enrollment.getStudent().getFullName());
            }
        }

        // ========================================================================
        // STEP 4: PERSISTENCE & RETURN
        // ========================================================================

        if (!newAttendances.isEmpty()) {
            studentAttendanceRepository.saveAll(newAttendances);
            log.info("Initialized {} new attendance records for schedule {}",
                    newAttendances.size(), request.getClassScheduleId());
        } else {
            log.info("All students already have attendance records. No new records created.");
        }

        // ========================================================================
        // FETCH FULL STATE: Query all attendance records for this schedule & session
        // This includes both existing and newly created records with full details
        // ========================================================================
        List<StudentAttendance> allAttendances = studentAttendanceRepository
                .findByScheduleIdAndSessionDateWithDetails(
                        request.getClassScheduleId(),
                        request.getSessionDate()
                );

        return studentAttendanceMapper.toResponseList(allAttendances);
    }

    @Transactional(rollbackFor = Exception.class)
    public StudentAttendanceDTO.Response createAttendanceRecord(
            StudentAttendanceDTO.ManualLogRequest request,
            String coachId){
        // 1. GỘP: Validate Student, ClassSchedule và Enrollment trong 1 lần gọi
        StudentEnrollment enrollment = studentEnrollmentService
                .getEnrollmentByStudentUserIdAndClassScheduleId(request.getStudentId(), request.getClassScheduleId());

        // 2. Validate Logic nghiệp vụ (Check status ngay trên object đã fetch về)
        Student student = enrollment.getStudent();
        ClassSchedule classSchedule = enrollment.getClassSchedule();

        if (student.getStudentStatus() != StudentStatus.ACTIVE) {
            throw new IllegalStateException("Học viên không ở trạng thái ACTIVE");
        }
        if (classSchedule.getScheduleStatus() != ScheduleStatus.ACTIVE) {
            throw new IllegalStateException("Lớp học đã bị hủy hoặc không hoạt động");
        }
        if (enrollment.getStatus() != StudentEnrollmentStatus.ACTIVE) {
            throw new IllegalStateException("Học viên đã nghỉ hoặc bảo lưu lớp này");
        }

        // 3. Validate Coach (Vẫn cần check riêng vì Coach độc lập với Enrollment)
        Coach coach = coachService.getCoachById(coachId);
        if (coach.getCoachStatus() != CoachStatus.ACTIVE) {
            throw new IllegalStateException("Coach is not ACTIVE");
        }

        // 4. Create & Save
        StudentAttendance attendance = StudentAttendance.builder()
                .studentEnrollment(enrollment) // Đã có sẵn Student và Schedule bên trong
                .recordedByCoach(coach)
                .sessionDate(request.getSessionDate())
                .attendanceStatus(request.getAttendanceStatus())
                .checkInTime(request.getCheckInTime() != null ? request.getCheckInTime() : Instant.now())
                .note(request.getNote())
                .build();

        StudentAttendance savedAttendance = studentAttendanceRepository.save(attendance);

        return studentAttendanceMapper.toResponse(savedAttendance);
    }
}
