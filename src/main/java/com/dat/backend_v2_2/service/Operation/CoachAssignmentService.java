package com.dat.backend_v2_2.service.Operation;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.backend_v2_2.domain.Operation.CoachAssignment;
import com.dat.backend_v2_2.dto.Operation.CoachAssignmentResDTO;
import com.dat.backend_v2_2.enums.Core.Weekday;
import com.dat.backend_v2_2.enums.Operation.CoachAssignmentStatus;
import com.dat.backend_v2_2.mapper.Operation.CoachAssignmentMapper;
import com.dat.backend_v2_2.repository.Operation.CoachAssignmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CoachAssignmentService {

    private final CoachAssignmentRepository coachAssignmentRepository;
    private final CoachAssignmentMapper coachAssignmentMapper;
    private final ZoneId defaultZoneId;

    /**
     * Lấy danh sách lịch dạy của HLV trong ngày hôm nay
     * @param coachId ID của HLV
     * @return Danh sách các phân công lớp học hôm nay
     */
    public List<CoachAssignmentResDTO.Response> getTodaySchedule(UUID coachId) {
    try {
        // Lấy ngày hiện tại theo giờ Việt Nam
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        Weekday todayWeekday = Weekday.fromJavaDayOfWeek(today.getDayOfWeek());

        log.info("HLV: {} | Hôm nay: {} | Thứ: {}", coachId, today, todayWeekday);

        List<CoachAssignment> assignments = coachAssignmentRepository.findTodayAssignmentsByCoachId(
                coachId,
                CoachAssignmentStatus.ACTIVE,
                todayWeekday,
                today
        );

        if (assignments == null || assignments.isEmpty()) return List.of();

        return assignments.stream()
                .map(coachAssignmentMapper::toResponse)
                .toList();
    } catch (Exception e) {
        log.error("Lỗi: ", e);
        return List.of();
    }
}
}
