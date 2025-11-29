package com.renee328.service.impl;

import com.renee328.dto.AdminDto;
import com.renee328.dto.BbsCategoryDto;
import com.renee328.dto.BoardDto;
import com.renee328.mapper.AdminMapper;
import com.renee328.mapper.BbsCategoryMapper;
import com.renee328.mapper.BoardMapper;
import com.renee328.mapper.PostMapper;
import com.renee328.service.BoardService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BoardServiceImpl implements BoardService {

    private final AdminMapper adminMapper;
    private final BoardMapper boardMapper;
    private final PostMapper postMapper;
    private final BbsCategoryMapper bbsCategoryMapper;
    private final PasswordEncoder passwordEncoder;

    public BoardServiceImpl(AdminMapper adminMapper,
                            BoardMapper boardMapper,
                            PostMapper postMapper,
                            BbsCategoryMapper bbsCategoryMapper,
                            PasswordEncoder passwordEncoder) {
        this.adminMapper = adminMapper;
        this.boardMapper = boardMapper;
        this.postMapper = postMapper;
        this.bbsCategoryMapper = bbsCategoryMapper;
        this.passwordEncoder = passwordEncoder;
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
        List<BbsCategoryDto> categories = bbsCategoryMapper.selectCategoriesByBoardId(boardId);
        board.setCategories(categories);
        if (categories != null && !categories.isEmpty()) {
            board.setBoardCategoryId(categories.get(0).getBoardCategoryId());
            board.setBoardCategoryName(categories.get(0).getBoardCategoryName());
        }
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
    public void deleteBoard(Long boardId, String deleterLoginId, String rawPassword) {
        AdminDto deleter = adminMapper.findByLoginId(deleterLoginId);

        if (deleter == null || !passwordEncoder.matches(rawPassword, deleter.getLoginPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다.");
        }

        String boardName = boardMapper.findBoardNameById(boardId);
        if (boardName == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시판을 찾을 수 없습니다.");
        }

        int postCount = postMapper.countPostsByBoardId(boardId);
        if (postCount > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "해당 게시판에 게시글이 " + postCount + "건 남아 있어 삭제할 수 없습니다."
            );
        }

        boardMapper.logBoardDeletion(deleterLoginId, boardName);
        boardMapper.deleteBoard(boardId);
    }

    // -------------------------- 게시판 카테고리 --------------------------

    // 모든 게시판 카테고리 조회
    @Override
    public List<BbsCategoryDto> getAllBoardCategories() {
        return bbsCategoryMapper.selectAllCategories();
    }

    // 카테고리 상세 조회
    @Override
    public BbsCategoryDto getCategoryByCategoryId(Long boardCategoryId) {
        return bbsCategoryMapper.selectCategoryByCategoryId(boardCategoryId);
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
    public void deleteBoardCategory(Long boardCategoryId, String deleterLoginId) {
        int count = bbsCategoryMapper.countBoardByCategoryId(boardCategoryId);
        if (count > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "해당 카테고리에 속한 게시판이 있어 삭제할 수 없습니다.");
        }
        String boardCategoryName = bbsCategoryMapper.findCategoryNameById(boardCategoryId);
        bbsCategoryMapper.logBbsCategoryDeletion(deleterLoginId, boardCategoryName);
        bbsCategoryMapper.deleteCategory(boardCategoryId);
    }
}
