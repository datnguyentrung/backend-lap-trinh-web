package com.dat.backend_v2_2.controller.Operation;

import com.dat.backend_v2_2.dto.Operation.StudentAttendanceDTO;
import com.dat.backend_v2_2.service.Operation.StudentAttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/student-attendance")
@CrossOrigin("*")
public class StudentAttendanceController {

    private final StudentAttendanceService studentAttendanceService;

    @GetMapping("/filter")
    public ResponseEntity<List<StudentAttendanceDTO.Response>> filter(
            @RequestParam String classScheduleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sessionDate) {
        return ResponseEntity.ok(studentAttendanceService.filterAttendanceRecords(classScheduleId, sessionDate));
    }

    @PatchMapping("/{attendanceId}/evaluation")
    public ResponseEntity<Void> updateEvaluation(
            Authentication authentication,
            @PathVariable UUID attendanceId,
            @RequestBody @Valid StudentAttendanceDTO.UpdateEvaluationRequest request) {
        
        // Nếu có đăng nhập thì lấy tên (ID), nếu không thì để null
        String coachId = (authentication != null) ? authentication.getName() : "05824b7a-d636-48e2-8825-9342f4b94b93";
        
        // Nếu bạn muốn test luôn mà không cần đăng nhập, hãy lấy 1 ID có thật trong DB dán vào đây:
        // String coachId = "05824b7a-d636-48e2-8825-9342f4b94b93"; 

        studentAttendanceService.updateAttendanceEvaluation(coachId, request, attendanceId);
        return ResponseEntity.noContent().build();
    }

    
}