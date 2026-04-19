package com.dat.backend_v2_2.repository.Operation;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dat.backend_v2_2.domain.Operation.StudentAttendance;

import jakarta.validation.constraints.NotNull;

@Repository
public interface StudentAttendanceRepository extends JpaRepository<StudentAttendance, UUID> {

    /**
     * Optimized query using EntityGraph to eagerly fetch related entities.
     * This approach is better than JOIN FETCH because:
     * 1. Avoids cartesian product when fetching multiple collections
     * 2. Uses subselect strategy for better performance
     * 3. Cleaner separation of concerns
     */
    @EntityGraph(attributePaths = {
        "studentEnrollment.student",
        "studentEnrollment.classSchedule",
        "recordedByCoach",
        "evaluatedByCoach"
    })
    @Query("""
        SELECT DISTINCT sa FROM StudentAttendance sa
        JOIN FETCH sa.studentEnrollment se
        JOIN FETCH se.student s
        JOIN FETCH se.classSchedule cs
        LEFT JOIN FETCH sa.recordedByCoach rbc
        LEFT JOIN FETCH sa.evaluatedByCoach ebc
        WHERE cs.scheduleId = :scheduleId
        AND sa.sessionDate = :sessionDate
        ORDER BY s.fullName
        """)
    List<StudentAttendance> findByScheduleIdAndSessionDateWithDetails(
        @Param("scheduleId") String scheduleId,
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
            @Param("scheduleId") @NotNull(message = "Schedule ID không được để trống") String classScheduleId,
            @Param("sessionDate") @NotNull(message = "Ngày học không được để trống") LocalDate sessionDate
    );
}
