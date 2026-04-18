package com.dat.backend_v2_2.mapper.Operation;

import com.dat.backend_v2_2.domain.Operation.StudentEnrollment;
import com.dat.backend_v2_2.dto.Operation.StudentEnrollmentReqDTO;
import com.dat.backend_v2_2.dto.Operation.StudentEnrollmentResDTO;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface StudentEnrollmentMapper { // 1. Đổi thành interface

    // Mapping cho Create (Như cũ)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "classSchedule", ignore = true)
    @Mapping(target = "joinDate", source = "joinDate")
    @Mapping(target = "note", source = "note")
    StudentEnrollment toEntity(StudentEnrollmentReqDTO.CreateRequest request);

    // Mapping cho Update
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(StudentEnrollmentReqDTO.UpdateRequest request, @MappingTarget StudentEnrollment entity);

    // Mapping cho Response (Quan trọng)
    @Mapping(target = "student", source = "student") // Map entity Student sang StudentSummary
    @Mapping(target = "classSchedule", source = "classSchedule") // Map entity Class sang ClassSummary
    StudentEnrollmentResDTO.Response toResponse(StudentEnrollment entity);

    /**
     * Hậu xử lý để điền các nhãn Tiếng Việt an toàn (Tránh NullPointerException)
     */
    @AfterMapping
    default void fillScheduleLabels(@MappingTarget StudentEnrollmentResDTO.Response response, StudentEnrollment entity) {
        if (entity.getClassSchedule() == null || response.getClassSchedule() == null) {
            return;
        }

        var s = entity.getClassSchedule();
        var res = response.getClassSchedule();

        // 1. Prepare parts
        java.time.format.DateTimeFormatter tf = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
        String start = s.getStartTime() != null ? s.getStartTime().format(tf) : "";
        String end = s.getEndTime() != null ? s.getEndTime().format(tf) : "";
        String timeRange = (!start.isEmpty() && !end.isEmpty()) ? start + " - " + end : "";

        String weekdayLabel = s.getWeekday() != null ? s.getWeekday().getLabel() : "";
        String levelLabel = s.getLevel() != null ? s.getLevel().getDisplayName() : "";

        // 2. Build displayLabel: "Thứ Hai (17:30 - 19:00) - Lớp Cơ Bản"
        StringBuilder labelBuilder = new StringBuilder();
        if (!weekdayLabel.isEmpty()) labelBuilder.append(weekdayLabel);
        if (!timeRange.isEmpty()) {
            labelBuilder.append(" (").append(timeRange).append(")");
        }
        if (!levelLabel.isEmpty()) {
            if (labelBuilder.length() > 0) labelBuilder.append(" - ");
            labelBuilder.append(levelLabel);
        }

        // 3. Set values to DTO
        res.setWeekdayLabel(weekdayLabel);
        res.setLevelLabel(levelLabel);
        res.setTimeRange(timeRange);
        res.setDisplayLabel(labelBuilder.toString());
        res.setBranchName(s.getBranch() != null ? s.getBranch().getBranchName() : "");
    }

    // Mapping cho SimpleResponse
    @Mapping(target = "classScheduleSummary", source = "classSchedule")
    @Mapping(target = "classScheduleSummary.branchName", source = "classSchedule.branch.branchName")
    @Mapping(target = "studentSummary", source = "student")
    @Mapping(target = "joinDate", source = "joinDate")
    @Mapping(target = "status", source = "status")
    StudentEnrollmentResDTO.SimpleResponse toSimpleResponse(StudentEnrollment entity);

    List<StudentEnrollmentResDTO.SimpleResponse> toSimpleResponseList(List<StudentEnrollment> entities);
}