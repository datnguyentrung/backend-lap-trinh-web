package com.dat.backend_v2_2.repository.Core;

import com.dat.backend_v2_2.domain.Core.Student;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    boolean existsByPhoneNumber(@NotBlank(message = "Số điện thoại không được để trống") @Pattern(regexp = "^(0|\\+84)(\\s|\\.)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})$",
                message = "Số điện thoại không đúng định dạng Việt Nam") String phoneNumber);

    boolean existsByNationalCode(String nationalCode);

    boolean existsByStudentCode(String generatedCode);

    // Class assignment - Tiến
    @Query("SELECT s FROM Student s " +
            "WHERE s.studentStatus = com.dat.backend_v2_2.enums.Core.StudentStatus.ACTIVE " +
            "AND (:keyword IS NULL OR :keyword = '' " +
            "OR LOWER(s.fullName) LIKE CONCAT('%',LOWER(:keyword),'%') " +
            "OR s.nationalCode LIKE CONCAT('%',:keyword,'%') )"
    )
    Page<Student> searchAutoComplete(@Param("keyword") String keyword, Pageable pageable);
}
