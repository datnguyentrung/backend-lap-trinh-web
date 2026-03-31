package com.dat.backend_v2_2.repository.Core;

import com.dat.backend_v2_2.domain.Core.ClassSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, String> {

    // Lấy danh sách lớp theo branchId
    @Query(
            "SELECT c FROM ClassSchedule c "+
            "WHERE branch.id = :branchId"
    )
    List<ClassSchedule> findClassesByBranchIdForDropdown(@Param("branchId") Long branchId);
}
