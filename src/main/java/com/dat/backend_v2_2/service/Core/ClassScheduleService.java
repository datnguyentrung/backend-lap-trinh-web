package com.dat.backend_v2_2.service.Core;

import com.dat.backend_v2_2.domain.Core.ClassSchedule;
import com.dat.backend_v2_2.dto.Core.ClassScheduleResDTO;
import com.dat.backend_v2_2.repository.Core.ClassScheduleRepository;
import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassScheduleService {
    private final ClassScheduleRepository classScheduleRepository;

    public ClassSchedule getClassScheduleById(String scheduleId) {
        return classScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> {
                    log.error("Class schedule not found with id: {}", scheduleId);
                    return new RuntimeException("Class schedule not found");
                });
    }

    public List<ClassSchedule> findByScheduleIds(List<String> scheduleIds) {
        if (CollectionUtils.isEmpty(scheduleIds)) {
            return Collections.emptyList();
        }
        return classScheduleRepository.findAllById(scheduleIds);
    }

    /**
     * Lấy danh sách Lớp học theo Chi nhánh dùng cho Dropdown
     *
     * @param branchId ID của chi nhánh
     * @return Danh sách ClassScheduleDropdown DTO
     */
    @Transactional(readOnly = true)
    public List<ClassScheduleResDTO.ClassScheduleDropdown> findByBranchId(Long branchId) {
        log.info("Fetching class schedules for dropdown by branchId: {}", branchId);

        List<ClassSchedule> classes = classScheduleRepository.findClassesByBranchIdForDropdown(branchId);


        return classes.stream().map(
            c ->{

                // Chuẩn hóa giờ
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                String startTime = c.getStartTime() == null ? "": c.getStartTime().format(timeFormatter);
                String endTime = c.getEndTime() == null ? "": c.getEndTime().format(timeFormatter);

                // Lấy nhãn Tiếng Việt từ Enum
                String weekdayLabel = c.getWeekday() == null ? "" : c.getWeekday().getLabel();
                String levelLabel = c.getLevel() == null ? "" : c.getLevel().getDisplayName();
                String timeRange = startTime + " - " + endTime;

                // Tạo chuỗi hiển thị chuẩn: "Thứ Hai (08:30 - 10:00) - Lớp Cơ Bản"
                String label = String.format("%s (%s) - %s",
                        weekdayLabel,
                        timeRange,
                        levelLabel);

                return ClassScheduleResDTO.ClassScheduleDropdown.builder()
                        .scheduleId(c.getScheduleId())
                        .displayLabel(label)
                        .weekdayLabel(weekdayLabel)
                        .levelLabel(levelLabel)
                        .timeRange(timeRange)
                        .scheduleLevel(c.getLevel())
                        .weekday(c.getWeekday())
                        .build();
            }).collect(Collectors.toList());
    }
}
