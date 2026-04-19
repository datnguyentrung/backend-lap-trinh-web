package com.dat.backend_v2_2.domain.Core;

import java.time.LocalDate;

import com.dat.backend_v2_2.domain.Security.User;
import com.dat.backend_v2_2.enums.Core.StudentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder 
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "student", schema = "core")
@PrimaryKeyJoinColumn(name = "user_id")
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Student extends User {

    @NotBlank(message = "Mã học viên không được để trống")
    @Size(max = 50, message = "Mã học viên tối đa 20 ký tự")
    @Column(name = "student_code", nullable = false, unique = true, length = 50)
    String studentCode;

    @NotNull(message = "Ngày bắt đầu tập không được để trống")
    @PastOrPresent(message = "Ngày bắt đầu không được ở tương lai")
    @Column(name = "start_date", nullable = false)
    LocalDate startDate = LocalDate.now();

    @NotNull(message = "Trạng thái học viên không được để trống")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "student_status", nullable = false, length = 20)
    StudentStatus studentStatus = StudentStatus.ACTIVE;

    @NotNull(message = "Chi nhánh không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false) // FK trỏ sang bảng Branch
    @ToString.Exclude
    Branch branch;
}
