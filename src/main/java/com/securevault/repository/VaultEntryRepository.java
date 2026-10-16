package com.securevault.repository;

import com.securevault.entity.Category;
import com.securevault.entity.VaultEntry;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VaultEntryRepository extends JpaRepository<VaultEntry, Long> {

    List<VaultEntry> findByUserId(Long userId, Sort sort);

    List<VaultEntry> findByUserIdAndCategory(Long userId, Category category, Sort sort);

    List<VaultEntry> findByUserIdAndFavoriteTrue(Long userId, Sort sort);

    @Query("SELECT v FROM VaultEntry v WHERE v.user.id = :userId AND (" +
           "LOWER(v.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(v.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(v.websiteUrl) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<VaultEntry> searchByUserAndQuery(@Param("userId") Long userId, @Param("query") String query, Sort sort);

    @Query("SELECT v FROM VaultEntry v WHERE v.user.id = :userId AND v.category = :category AND (" +
           "LOWER(v.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(v.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(v.websiteUrl) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<VaultEntry> searchByUserAndCategoryAndQuery(@Param("userId") Long userId,
                                                    @Param("category") Category category,
                                                    @Param("query") String query,
                                                    Sort sort);

    // Critical IDOR protection method: guarantees retrieval strictly scoped to the authenticated owner
    Optional<VaultEntry> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndCategory(Long userId, Category category);

    long countByUserIdAndPasswordStrengthScoreLessThanEqual(Long userId, int maxScore);

    List<VaultEntry> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
}
