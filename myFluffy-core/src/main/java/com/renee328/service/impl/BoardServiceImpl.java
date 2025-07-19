package com.renee328.service.impl;

import com.renee328.dto.AdminDto;
import com.renee328.dto.BbsCategoryDto;
import com.renee328.dto.BoardDto;
import com.renee328.mapper.AdminMapper;
import com.renee328.mapper.BbsCategoryMapper;
import com.renee328.mapper.BoardMapper;
import com.renee328.service.BoardService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BoardServiceImpl implements BoardService {

    private final AdminMapper adminMapper;
    private final BoardMapper boardMapper;
    private final BbsCategoryMapper bbsCategoryMapper;

    public BoardServiceImpl(AdminMapper adminMapper, BoardMapper boardMapper, BbsCategoryMapper bbsCategoryMapper) {
        this.adminMapper = adminMapper;
        this.boardMapper = boardMapper;
        this.bbsCategoryMapper = bbsCategoryMapper;
    }

    // 게시판 목록 조회하기
    @Override
    public List<BoardDto> getBoardList() {
        return boardMapper.getBoardList();
    }

    // 게시판 개수 조회하기
    @Override
    public int getBoardCount() {
        return boardMapper.getBoardCount();
    }

    // 카테고리별 게시판 목록 조회
    @Override
    public List<BoardDto> getBoardByCategoryId(Long boardCategoryId) {
        return boardMapper.getBbsListByCategoryId(boardCategoryId);
    }

    // 게시판 세부정보 조회하기
    @Override
    public BoardDto getBoardDetails(Long boardId) {
        BoardDto board = boardMapper.getBoardDetails(boardId);
        board.setCategories(bbsCategoryMapper.selectCategoriesByBoardId(boardId));
        return board;
    }

    // 게시판 메뉴 추가하기
    @Transactional
    @Override
    public void createBoard(BoardDto boardDto) {
        boardMapper.insertBoard(boardDto);
        Long boardId = boardDto.getBoardId();
        insertCategory(boardDto, boardId);
    }

    // 게시판 메뉴 수정하기
    @Transactional
    @Override
    public void updateBoard(BoardDto boardDto) {
        Long boardId = boardDto.getBoardId();
        boardMapper.updateBoard(boardDto);
        bbsCategoryMapper.deleteCategoryMapping(boardId);
        insertCategory(boardDto, boardId);
    }

    // 클래스 공통 메서드 - 게시판 카테고리 매핑하기
    private void insertCategory(BoardDto boardDto, Long boardId) {
        if (boardDto.getBoardCategoryId() != null) {
            Long boardCategoryId = boardDto.getBoardCategoryId();
            bbsCategoryMapper.insertCategoryMapping(boardId, boardCategoryId);
        }
    }

    // 게시판 메뉴 삭제하기
    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
    @Override
    public void deleteBoard(Long boardId, String deleterId, String boardName) {
        AdminDto deleter = adminMapper.findByLoginId(deleterId);

        if (!"ROLE_SUPER_ADMIN".equals(deleter.getRoleId()) && !"ROLE_ADMIN".equals(deleter.getRoleId())) {
            throw new AccessDeniedException("운영자는 삭제할 권한이 없습니다.");
        }

        boardMapper.logBoardDeletion(deleterId, boardName);
        boardMapper.deleteBoard(boardId);
    }

    // -------------------------- 게시판 카테고리 --------------------------

    // 모든 게시판 카테고리 조회
    @Override
    public List<BbsCategoryDto> getAllBoardCategories() {
        return bbsCategoryMapper.selectAllCategories();
    }

    // 게시판별 카테고리 목록 조회
    @Override
    public List<BbsCategoryDto> getCategoriesByBoardId(Long boardId) {
        return bbsCategoryMapper.selectCategoriesByBoardId(boardId);
    }

    // 게시판 카테고리 추가
    @Transactional
    @Override
    public void createBoardCategory(BbsCategoryDto bbsCategoryDto) {
        bbsCategoryMapper.insertCategory(bbsCategoryDto);
    }

    // 게시판 카테고리 수정
    @Transactional
    @Override
    public void updateBoardCategory(BbsCategoryDto bbsCategoryDto) {
        bbsCategoryMapper.updateCategory(bbsCategoryDto);
    }

    // 게시판 카테고리 삭제
    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
    @Override
    public void deleteBoardCategory(Long boardCategoryId, String deleterId, String boardCategoryName) {
        AdminDto deleter = adminMapper.findByLoginId(deleterId);

        if (!"ROLE_SUPER_ADMIN".equals(deleter.getRoleId()) && !"ROLE_ADMIN".equals(deleter.getRoleId())) {
            throw new AccessDeniedException("운영자는 삭제할 권한이 없습니다.");
        }

        bbsCategoryMapper.logBbsCategoryDeletion(deleterId, boardCategoryName);
        bbsCategoryMapper.deleteCategory(boardCategoryId);
    }
}
