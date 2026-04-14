package com.dat.backend_v2_2.controller.Core;

import com.dat.backend_v2_2.dto.Core.ClassScheduleResDTO;
import com.dat.backend_v2_2.service.Core.BranchService;
import com.dat.backend_v2_2.service.Core.ClassScheduleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller quản lý Chi nhánh (Branch)
 * Cung cấp API lấy danh sách Branch và Lớp học theo từng Branch
 * để phục vụ tính năng Cascading Dropdown trên trang Ghi Danh Võ Sinh
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BranchController {

    BranchService branchService;

    ClassScheduleService classScheduleService;

    /**
     * Lấy danh sách tất cả Chi nhánh (Dùng cho Dropdown chọn Branch)
     * GET /api/v1/branches
     *
     * FE sẽ gọi endpoint này khi Component mount để load toàn bộ danh sách Branch.
     * Sau khi user chọn Branch, FE tiếp tục gọi /branches/{id}/classes để load Lớp
     * học.
     */
    @GetMapping("/branches")
    public ResponseEntity<List<BranchDropdownResponse>> getAllBranches() {

        // Lấy danh sách Branch từ Service -> chuyển thành DTO đơn giản cho FE
        List<BranchDropdownResponse> result = branchService.findAllBranches().stream()
                .map(b -> new BranchDropdownResponse(b.getBranchId(), b.getBranchName(), b.getAddress()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * Lấy danh sách Lớp học theo Chi nhánh (Dùng cho Cascading Dropdown)
     * GET /api/v1/branches/{id}/classes
     *
     * Được gọi sau khi user đã chọn Branch, để load các Lớp học tương ứng.
     * Chỉ trả về danh sách lớp đang ACTIVE (do ClassScheduleService xử lý filter).
     *
     * @param branchId ID của Chi nhánh vừa được chọn
     */
    @GetMapping("/branches/{id}/classes")
    public ResponseEntity<List<ClassScheduleResDTO.ClassScheduleDropdown>> getClassesByBranch(
            @PathVariable("id") Long branchId) {

        List<ClassScheduleResDTO.ClassScheduleDropdown> result = classScheduleService.findByBranchId(branchId);

        return ResponseEntity.ok(result);
    }

    /**
     * DTO nội bộ (Inner class) - Chỉ dùng để trả về thông tin tối giản của Branch
     * cho Dropdown.
     * Không cần tạo file riêng vì chỉ dùng trong Controller này.
     */
    public record BranchDropdownResponse(Long branchId, String branchName, String address) {
    }
}
