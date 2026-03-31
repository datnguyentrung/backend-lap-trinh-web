package com.dat.backend_v2_2.controller.Core;

import com.dat.backend_v2_2.dto.Core.StudentResDTO;
import com.dat.backend_v2_2.service.Core.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor

public class StudentController {

    private final StudentService studentService;

    @GetMapping("/search-autocomplete")
    public ResponseEntity<Page<StudentResDTO.StudentAutocomplete>> searchAutocomplete(@RequestParam(required = false,defaultValue = "") String keyword,
                                                                                      @RequestParam (defaultValue = "0") int page,
                                                                                      @RequestParam (defaultValue = "10") int page_sỉze
                                                                                      )
    {
        // Sắp xếp mặc định theo tên học viên
        Pageable pageable = PageRequest.of(page,page_sỉze, Sort.by("fullName").ascending());
        Page<StudentResDTO.StudentAutocomplete> result = studentService.searchStudentAutocomplete(keyword, pageable);
        return ResponseEntity.ok(result);
    }

}
