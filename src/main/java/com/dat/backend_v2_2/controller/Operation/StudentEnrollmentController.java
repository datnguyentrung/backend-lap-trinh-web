package com.dat.backend_v2_2.controller.Operation;

import com.dat.backend_v2_2.dto.Operation.StudentEnrollmentReqDTO;
import com.dat.backend_v2_2.dto.Operation.StudentEnrollmentResDTO;
import com.dat.backend_v2_2.mapper.Operation.StudentEnrollmentMapper;
import com.dat.backend_v2_2.service.Operation.StudentEnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý đăng ký học viên vào lớp (Student Enrollment)
 * Xử lý các thao tác CRUD và tra cứu thông tin enrollment
 */
@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/student-enrollments")
public class StudentEnrollmentController {

        private final StudentEnrollmentService studentEnrollmentService;

        private final StudentEnrollmentMapper studentEnrollmentMapper;

        /**
         * Đăng ký học viên vào lớp học
         * POST /api/v1/student-enrollments
         *
         * Cho phép đăng ký một học viên vào một hoặc nhiều lớp học cùng lúc.
         * Hệ thống sẽ kiểm tra trùng lặp và validate thông tin trước khi tạo
         * enrollment.
         *
         * @param request Thông tin đăng ký (studentId, scheduleIds, joinDate, note)
         * @return 201 Created - Đăng ký thành công
         *         400 Bad Request - Dữ liệu không hợp lệ
         *         404 Not Found - Không tìm thấy học viên hoặc lớp học
         *         409 Conflict - Học viên đã được đăng ký vào lớp này
         */
        @PostMapping
        public ResponseEntity<String> createStudentEnrollment(
                        @RequestBody @Valid StudentEnrollmentReqDTO.CreateRequest request) {
                log.info("Request create enrollment for student: {} to {} classes",
                                request.getStudentId(), request.getScheduleIds().size());

                studentEnrollmentService.createStudentEnrollment(request);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body("Đăng ký học viên thành công");
        }

        /**
         * Lấy danh sách học viên trong một lớp học
         * GET /api/v1/student-enrollments/class-schedule/{classScheduleId}
         *
         * Trả về danh sách các học viên đã đăng ký trong một lớp học cụ thể.
         * Response dạng đơn giản, phù hợp cho dropdown hoặc danh sách tóm tắt.
         *
         * @param classScheduleId ID của lớp học
         * @return 200 OK - Danh sách học viên trong lớp
         *         404 Not Found - Không tìm thấy lớp học
         */
        @GetMapping("/class-schedule/{classScheduleId}")
        public ResponseEntity<List<StudentEnrollmentResDTO.SimpleResponse>> getStudentEnrollmentsByClassScheduleId(
                        @PathVariable String classScheduleId) {
                log.info("Request get enrollments for class schedule: {}", classScheduleId);

                List<StudentEnrollmentResDTO.SimpleResponse> enrollments = studentEnrollmentMapper.toSimpleResponseList(
                                studentEnrollmentService.getStudentEnrollmentsByClassScheduleId(classScheduleId));

                return ResponseEntity.ok(enrollments);
        }
}
