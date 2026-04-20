package com.dat.backend_v2_2.repository.Operation;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dat.backend_v2_2.domain.Operation.StudentAttendance;

@Repository
public interface StudentAttendanceRepository extends JpaRepository<StudentAttendance, UUID> {

    // Giải quyết lỗi ORDER BY expressions must appear in select list bằng cách bỏ DISTINCT
    @EntityGraph(attributePaths = {
        "studentEnrollment.student",
        "studentEnrollment.classSchedule",
        "recordedByCoach",
        "evaluatedByCoach"
    })
    @Query("""
        SELECT sa FROM StudentAttendance sa
        JOIN sa.studentEnrollment se
        JOIN se.student s
        WHERE se.classSchedule.scheduleId = :scheduleId
        AND sa.sessionDate = :sessionDate
        ORDER BY s.fullName ASC
        """)
    List<StudentAttendance> findByScheduleIdAndSessionDateWithDetails(
        @Param("scheduleId") String scheduleId,
        @Param("sessionDate") LocalDate sessionDate
    );

    @Query("SELECT s FROM StudentAttendance s WHERE s.studentEnrollment.enrollmentId = :enrollmentId AND s.sessionDate = :sessionDate")
    Optional<StudentAttendance> findByEnrollmentAndDate(
        @Param("enrollmentId") UUID enrollmentId, 
        @Param("sessionDate") LocalDate sessionDate
    );

    @Query(value = """
        SELECT se.student_user_id
        FROM operation.student_attendance sa
        INNER JOIN operation.student_enrollment se
            ON sa.student_enrollment_id = se.enrollment_id
        WHERE se.schedule_id = :scheduleId
        AND sa.session_date = :sessionDate
        """, nativeQuery = true)
    List<UUID> findStudentIdsByScheduleAndSessionDate(
            @Param("scheduleId") String classScheduleId,
            @Param("sessionDate") LocalDate sessionDate
    );
}