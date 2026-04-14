package com.dat.backend_v2_2.service.Core;

import com.dat.backend_v2_2.domain.Core.Branch;
import com.dat.backend_v2_2.repository.Core.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BranchService{
    private final BranchRepository branchRepository;

    public Branch getBranchById(Long idBranch) {
        return branchRepository.findById(idBranch)
                .orElseThrow(() -> new IllegalArgumentException("Branch with id " + idBranch + " not found"));
    }

    /**
     * Lấy toàn bộ danh sách Chi nhánh đang hoạt động để hiển thị Dropdown trên Frontend.
     * <p>
     * Sử dụng cho Cascading Dropdown: FE load tất cả Branch trước,
     * sau đó user chọn Branch -> FE gọi tiếp API lấy các Lớp học của Branch đó.
     *
     * @return Danh sách toàn bộ Branch trong hệ thống
     */
    @Transactional(readOnly = true)
    public List<Branch> findAllBranches() {
        log.info("Fetching all branches for dropdown");
        return branchRepository.findAll();
    }
}
