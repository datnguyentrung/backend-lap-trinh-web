package com.dat.backend_v2_2.controller.Core;

import com.dat.backend_v2_2.dto.Core.ClassScheduleResDTO;
import com.dat.backend_v2_2.service.Core.ClassScheduleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BranchController {

    ClassScheduleService classScheduleService;

    /**
     * API Lấy danh sách lớp học theo Chi nhánh (Dùng cho Cascading Dropdown)
     * Đường dẫn: GET /api/v1/branches/{id}/classes
     */
    @GetMapping("/branches/{id}/classes")
    public ResponseEntity<List<ClassScheduleResDTO.ClassScheduleDropdown>> getClassesByBranch(
            @PathVariable("id") Long branchId) { // Chú ý: Đổi sang UUID nếu ID của Branch trong entity là UUID

        List<ClassScheduleResDTO.ClassScheduleDropdown> result = classScheduleService.findByBranchId(branchId);

        return ResponseEntity.ok(result);
    }
}
