package com.dat.backend_v2_2.service.Operation;

import com.dat.backend_v2_2.domain.Core.ClassSchedule;
import com.dat.backend_v2_2.domain.Core.Student;
import com.dat.backend_v2_2.domain.Operation.StudentEnrollment;
import com.dat.backend_v2_2.dto.Operation.StudentEnrollmentReqDTO;
import com.dat.backend_v2_2.enums.ErrorCode;
import com.dat.backend_v2_2.enums.Operation.StudentEnrollmentStatus;
import com.dat.backend_v2_2.mapper.Operation.StudentEnrollmentMapper;
import com.dat.backend_v2_2.repository.Operation.StudentEnrollmentRepository;
import com.dat.backend_v2_2.service.Core.ClassScheduleService;
import com.dat.backend_v2_2.service.Core.StudentService;
import com.dat.backend_v2_2.util.error.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudentEnrollmentService {
    private final StudentEnrollmentRepository studentEnrollmentRepository;

    private final StudentService studentService;

    private final ClassScheduleService classScheduleService;

    private final StudentEnrollmentMapper studentEnrollmentMapper;

    /**
     * Ghi danh võ sinh vào một hoặc nhiều lớp học cùng lúc.
     * <p>
     * Luồng hoạt động (Basic Flow):
     * 1. Service nhận request từ Controller.
     * 2. Gọi DB lấy thông tin Võ sinh (getStudentById).
     * 3. Gọi DB lấy danh sách ClassSchedule theo list ID (findByScheduleIds - 1
     * query).
     * 4. Kiểm tra tính hợp lệ: Số lớp tìm được phải bằng số lớp yêu cầu.
     * 5. Duyệt vòng lặp từng lớp:
     * a. Kiểm tra trùng lặp (existsByStudent...) -> nếu đã tồn tại thì throw lỗi.
     * b. Dùng Mapper tạo Entity từ DTO.
     * c. Set các quan hệ (Student, ClassSchedule, Status).
     * d. Thêm vào danh sách chờ (enrollmentsToSave).
     * 6. Bulk Insert toàn bộ danh sách một lần (saveAll).
     * <p>
     * Exception Flow 1 (CLASS_NOT_FOUND): Số lớp tìm được < số lớp yêu cầu.
     * Exception Flow 2 (STUDENT_ALREADY_ENROLLED): Võ sinh đã học lớp này với trạng
     * thái ACTIVE.
     * -> Cả 2 trường hợp đều Rollback toàn bộ Transaction.
     *
     * @param request DTO chứa studentId, scheduleIds, joinDate, note
     * @throws AppException ErrorCode.CLASS_NOT_FOUND nếu có lớp ID sai
     * @throws AppException ErrorCode.STUDENT_ALREADY_ENROLLED nếu võ sinh đã trong
     *                      lớp
     */
    @Transactional(rollbackFor = Exception.class)
    public void createStudentEnrollment(StudentEnrollmentReqDTO.CreateRequest request) {

        // ========================================================================
        // STEP 1: LẤY THÔNG TIN VÕ SINH
        // Service gọi Database (getStudentById) để lấy Entity Võ sinh
        // Hàm này tự throw Exception nếu không tìm thấy
        // ========================================================================
        Student student = studentService.getStudentById(request.getStudentId());

        // ========================================================================
        // STEP 2: LẤY DANH SÁCH LỚP HỌC (1 QUERY THAY VÌ N QUERY)
        // Gọi DB (findByScheduleIds) dựa trên danh sách ID truyền vào
        // Dùng findAllById thay vì từng lần findById để tối ưu hiệu suất
        // ========================================================================
        List<ClassSchedule> schedules = classScheduleService.findByScheduleIds(request.getScheduleIds());

        // ========================================================================
        // STEP 3: VALIDATION - KIỂM TRA TÍNH HỢP LỆ CỦA DANH SÁCH LỚP
        // Nếu số lớp tìm thấy ít hơn số lớp được gửi lên -> có ID sai
        // -> Exception Flow 1: CLASS_NOT_FOUND (404)
        // ========================================================================
        if (schedules.size() != request.getScheduleIds().size()) {
            log.warn("Class schedule not found. Requested: {}, Found: {}",
                    request.getScheduleIds().size(), schedules.size());
            throw new AppException(ErrorCode.CLASS_NOT_FOUND);
        }

        // ========================================================================
        // STEP 4: DUYỆT VÒNG LẶP - XỬ LÝ TỪNG LỚP HỌC
        // ========================================================================
        List<StudentEnrollment> enrollmentsToSave = new ArrayList<>();

        for (ClassSchedule schedule : schedules) {

            // STEP 4a: Kiểm tra trùng lặp đăng ký
            // Hỏi DB: "Võ sinh này đã ACTIVE trong lớp này chưa?"
            // Lưu ý: Query trong vòng for vẫn chấp nhận được vì số lớp chọn thường nhỏ (<
            // 5)
            // -> Exception Flow 2: STUDENT_ALREADY_ENROLLED (409) -> Rollback toàn bộ
            // Transaction
            boolean exists = studentEnrollmentRepository
                    .existsByStudent_UserIdAndClassSchedule_ScheduleIdAndStatus(
                            UUID.fromString(request.getStudentId()),
                            schedule.getScheduleId(),
                            StudentEnrollmentStatus.ACTIVE);

            if (exists) {
                log.warn("Student {} already in class {}", student.getUserId(), schedule.getScheduleId());
                // Tùy chọn: Bỏ qua lớp này (continue) hoặc ném lỗi dừng tất cả (throw)
                // Ở đây mình chọn throw lỗi để báo FE biết
                throw new AppException(ErrorCode.STUDENT_ALREADY_ENROLLED);
            }

            // STEP 4b: Dùng Mapper chuyển đổi Request DTO -> Entity (có joinDate, note...)
            StudentEnrollment enrollment = studentEnrollmentMapper.toEntity(request);

            // STEP 4c: Set các quan hệ sau khi Mapper tạo object cơ bản
            enrollment.setStudent(student);
            enrollment.setClassSchedule(schedule);
            enrollment.setStatus(StudentEnrollmentStatus.ACTIVE); // Mặc định ghi danh là ACTIVE

            // STEP 4d: Thêm vào danh sách chờ lưu (batch)
            enrollmentsToSave.add(enrollment);
        }

        // ========================================================================
        // STEP 5: PERSISTENCE - LƯU ĐỒNG LOẠT (BULK INSERT)
        // Gọi saveAll 1 lần thay vì save() từng phần tử -> Hiệu quả hơn
        // Database xác nhận lưu thành công -> Transaction được Commit
        // ========================================================================
        studentEnrollmentRepository.saveAll(enrollmentsToSave);

        log.info("Successfully enrolled student {} to {} classes", student.getUserId(), enrollmentsToSave.size());
    }

    /**
     * Lấy danh sách học viên theo ID lịch học lớp
     * 
     * @param classScheduleId ID của lịch học lớp
     * @return Danh sách học viên
     */
    public List<StudentEnrollment> getStudentEnrollmentsByClassScheduleId(String classScheduleId) {
        return studentEnrollmentRepository.findByScheduleIdAndStatusWithStudent(
                classScheduleId,
                StudentEnrollmentStatus.ACTIVE);
    }

    public StudentEnrollment getEnrollmentByStudentUserIdAndClassScheduleId(UUID studentUserId,
            String classScheduleId) {
        return studentEnrollmentRepository.findByStudent_UserIdAndClassSchedule_ScheduleIdAndStatus(
                studentUserId,
                classScheduleId,
                StudentEnrollmentStatus.ACTIVE).orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));
    }
}
