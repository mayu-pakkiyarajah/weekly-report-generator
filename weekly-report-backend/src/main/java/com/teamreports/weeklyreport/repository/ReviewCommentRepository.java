package com.teamreports.weeklyreport.repository;

import com.teamreports.weeklyreport.entity.ReviewComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {
    List<ReviewComment> findByReportIdOrderByCreatedAtDesc(Long reportId);

    @EntityGraph(attributePaths = {"report", "report.user", "reviewer"})
    @Query("select c from ReviewComment c order by c.createdAt desc")
    Page<ReviewComment> findLatestComments(Pageable pageable);
}
