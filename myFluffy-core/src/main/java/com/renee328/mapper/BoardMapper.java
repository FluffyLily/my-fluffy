package com.renee328.mapper;

import com.renee328.dto.BoardDto;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BoardMapper {

    // 게시판 목록 조회
    List<BoardDto> getBoardList();

    // 게시판 개수 조회
    int getBoardCount();

    // 카테고리별 게시판 목록 조회
    List<BoardDto> getBbsListByCategoryId(Long boardCategoryId);

    // 게시판 아이디 조회
    Long findBoardIdByName(String boardName);

    // 게시판 세부정보 조회
    BoardDto getBoardDetails(Long boardId);

    // 게시판 추가
    void insertBoard(BoardDto boardDto);

    // 게시판 수정하기
    void updateBoard(BoardDto boardDto);

    // 게시판 삭제하기
    void deleteBoard(Long boardId);

    // 게시판 삭제 로그
    void logBoardDeletion(@Param("deleterId") String deleterId, @Param("boardName") String boardName);

}
