package com.renee328.service;

import com.renee328.dto.BbsCategoryDto;
import com.renee328.dto.BoardDto;
import java.util.List;

public interface BoardService {

    // 게시판 전체 목록 조회
    List<BoardDto> getBoardList();

    // 게시판 개수 조회
    int getBoardCount();

    // 카테고리별 게시판 목록 조회
    List<BoardDto> getBoardByCategoryId(Long boardCategoryId);

    // 게시판 세부정보 조회
    BoardDto getBoardDetails(Long boardId);

    // 게시판 추가하기
    void createBoard(BoardDto boardDto);

    // 게시판 수정하기
    void updateBoard(BoardDto boardDto);

    // 게시판 삭제하기
    void deleteBoard(Long boardId, String deleterLoginId, String rawPassword);

    // -------------------------- 게시판 카테고리 --------------------------

    // 모든 게시판 카테고리 조회
    List<BbsCategoryDto> getAllBoardCategories();

    // 게시판별 카테고리 목록 조회
    List<BbsCategoryDto> getCategoriesByBoardId(Long boardId);

    // 게시판 카테고리 추가
    void createBoardCategory(BbsCategoryDto bbsCategoryDto);

    // 게시판 카테고리 수정
    void updateBoardCategory(BbsCategoryDto bbsCategoryDto);

    // 게시판 카테고리 삭제
    void deleteBoardCategory(Long boardCategoryId, String deleterId, String boardCategoryName);
}
