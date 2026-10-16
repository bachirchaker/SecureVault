package com.securevault.repository;

import com.securevault.entity.SecurityEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long> {

    List<SecurityEvent> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<SecurityEvent> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<SecurityEvent> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM SecurityEvent s WHERE s.createdAt < :cutoffDate")
    int deleteEventsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
}
