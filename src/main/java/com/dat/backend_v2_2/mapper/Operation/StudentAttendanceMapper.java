package com.dat.backend_v2_2.mapper.Operation;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.dat.backend_v2_2.domain.Operation.StudentAttendance;
import com.dat.backend_v2_2.dto.Operation.StudentAttendanceDTO;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StudentAttendanceMapper {

    @Mapping(target = "attendanceId", source = "attendanceId")
    @Mapping(target = "attendanceStatus", source = "attendanceStatus")
    @Mapping(target = "enrollmentId", source = "studentEnrollment.enrollmentId") 
    
    @Mapping(target = "classScheduleId", source = "studentEnrollment.classSchedule.scheduleId")
    
    @Mapping(target = "studentId", source = "studentEnrollment.student.userId")
    @Mapping(target = "studentName", source = "studentEnrollment.student.fullName")    
    @Mapping(target = "recordedByCoachName", source = "recordedByCoach.fullName")
    @Mapping(target = "evaluatedByCoachName", source = "evaluatedByCoach.fullName")
    @Mapping(target = "sessionDate", source = "sessionDate")
    @Mapping(target = "checkInTime", source = "checkInTime")
    StudentAttendanceDTO.Response toResponse(StudentAttendance entity);

    List<StudentAttendanceDTO.Response> toResponseList(List<StudentAttendance> entities);
}