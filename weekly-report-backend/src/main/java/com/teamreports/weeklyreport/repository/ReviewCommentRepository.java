package com.teamreports.weeklyreport.repository;

import com.teamreports.weeklyreport.entity.ReviewComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {
    List<ReviewComment> findByReportIdOrderByCreatedAtDesc(Long reportId);
}
