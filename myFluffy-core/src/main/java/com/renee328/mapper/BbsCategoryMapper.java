package com.renee328.mapper;

import com.renee328.dto.BbsCategoryDto;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BbsCategoryMapper {
    // 모든 게시판 카테고리 조회
    List<BbsCategoryDto> selectAllCategories();

    // 카테고리 상세 조회
    BbsCategoryDto selectCategoryByCategoryId(Long boardCategoryId);

    // 게시판별 카테고리 목록 조회
    List<BbsCategoryDto> selectCategoriesByBoardId(Long boardId);

    // 게시판 카테고리명 조회
    String findCategoryNameById(Long boardCategoryId);

    // 게시판 카테고리에 속한 게시판 수 조회
    int countBoardByCategoryId(Long boardCategoryId);

    // 게시판 카테고리 추가
    void insertCategory(BbsCategoryDto bbsCategoryDto);

    // 게시판과 카테고리 매핑 추가
    void insertCategoryMapping(@Param("boardId") Long boardId, @Param("boardCategoryId") Long boardCategoryId);

    // 카테고리 수정
    void updateCategory(BbsCategoryDto bbsCategoryDto);

    // 카테고리 삭제
    void deleteCategory(Long boardCategoryId);

    // 게시판과 카테고리 매핑 삭제
    void deleteCategoryMapping(Long boardId);

    // 게시판 카테고리 삭제 로그
    void logBbsCategoryDeletion(@Param("deleterId") String deleterId, @Param("boardCategoryName") String boardCategoryName);
}
