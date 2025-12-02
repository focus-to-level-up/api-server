package com.studioedge.focus_to_levelup_server.domain.guild.dao;

import com.studioedge.focus_to_levelup_server.domain.guild.entity.Guild;
import com.studioedge.focus_to_levelup_server.domain.guild.enums.GuildCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GuildRepository extends JpaRepository<Guild, Long> {

    // 길드원 JOIN FETCH
    @Query("SELECT g FROM Guild g LEFT JOIN FETCH g.members WHERE g.id = :id")
    Optional<Guild> findByIdWithMembers(@Param("id") Long id);

    // 공개 + 여유 있는 길드
    List<Guild> findAllByIsPublicTrueAndCurrentMembersLessThan(Integer maxMembers);

    // 카테고리별 정렬
    List<Guild> findAllByCategoryOrderByAverageFocusTimeDesc(GuildCategory category, Pageable pageable);

    // 키워드 검색
    @Query("SELECT g FROM Guild g WHERE g.name LIKE %:keyword%")
    Page<Guild> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 키워드 + 카테고리 검색
    @Query("SELECT g FROM Guild g WHERE g.name LIKE %:keyword% AND g.category = :category")
    Page<Guild> searchByKeywordAndCategory(
            @Param("keyword") String keyword,
            @Param("category") GuildCategory category,
            Pageable pageable
    );

    // 전체 길드 조회 (페이징)
    Page<Guild> findAll(Pageable pageable);

    // 전체 길드 조회 - 정원 미달 길드만 (페이징)
    @Query("SELECT g FROM Guild g WHERE g.currentMembers < g.maxMembers")
    Page<Guild> findAllAvailable(Pageable pageable);

    // 카테고리별 조회 (페이징)
    Page<Guild> findAllByCategory(GuildCategory category, Pageable pageable);

    // 카테고리별 조회 - 정원 미달 길드만 (페이징)
    @Query("SELECT g FROM Guild g WHERE g.category = :category AND g.currentMembers < g.maxMembers")
    Page<Guild> findAllByCategoryAvailable(@Param("category") GuildCategory category, Pageable pageable);
}
