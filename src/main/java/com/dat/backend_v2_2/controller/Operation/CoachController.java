package com.dat.backend_v2_2.controller.Operation;

import com.dat.backend_v2_2.dto.Operation.CoachAssignmentResDTO;
import com.dat.backend_v2_2.dto.RestResponse;
import com.dat.backend_v2_2.service.Operation.CoachAssignmentService;
import com.dat.backend_v2_2.util.FormatRestResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/operation/coaches", "/api/v1/coaches"})
@RequiredArgsConstructor
@Slf4j
public class CoachController {

    private final CoachAssignmentService coachAssignmentService;

    /**
     * API lấy lịch dạy của HLV trong ngày hôm nay
     * @param coachId ID của HLV
     * @return Danh sách lịch dạy hôm nay
     */
    @GetMapping("/{coachId}/schedule/today")
    public ResponseEntity<RestResponse<List<CoachAssignmentResDTO.Response>>> getTodaySchedule(
            @PathVariable UUID coachId) {

        List<CoachAssignmentResDTO.Response> schedule = coachAssignmentService.getTodaySchedule(coachId);

        return ResponseEntity.ok(FormatRestResponse.success("Lấy lịch dạy hôm nay thành công", schedule));
    }
}
