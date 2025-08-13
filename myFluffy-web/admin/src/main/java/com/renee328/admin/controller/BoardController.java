package com.renee328.admin.controller;

import com.renee328.dto.BbsCategoryDto;
import com.renee328.dto.BoardDto;
import com.renee328.dto.LoginRequest;
import com.renee328.service.BoardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;

import static com.renee328.admin.util.ApiConstants.BOARD_API_URL;

@RestController
@RequestMapping(BOARD_API_URL)
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    // 게시판 추가하기
    @PostMapping("/create")
    public ResponseEntity<?> createBoard(@RequestBody BoardDto boardDto) {
        boardService.createBoard(boardDto);
        return ResponseEntity.ok(Map.of("message", "게시판이 생성되었습니다."));
    }

    // 게시판 목록 조회하기
    @GetMapping("/list")
    public ResponseEntity<List<BoardDto>> getBoardList() {
        List<BoardDto> boardList = boardService.getBoardList();
        return ResponseEntity.ok(boardList);
    }

    // 게시판 개수 조회하기
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getBoardCount() {
        int totalCount = boardService.getBoardCount();
        Map<String, Object> response = Map.of("totalCount", totalCount);
        return ResponseEntity.ok(response);
    }

    // 카테고리별 게시판 목록 조회
    @GetMapping("/list/{boardCategoryId}")
    public ResponseEntity<List<BoardDto>> getBoardByCategoryId(@PathVariable Long boardCategoryId) {
        List<BoardDto> boardList = boardService.getBoardByCategoryId(boardCategoryId);
        return ResponseEntity.ok(boardList);
    }

    // 게시판 목록 세부정보
    @GetMapping("/detail/{boardId}")
    public ResponseEntity<BoardDto> getBoardDetails(@PathVariable Long boardId) {
        BoardDto boardDetails = boardService.getBoardDetails(boardId);
        return ResponseEntity.ok(boardDetails);
    }

    // 게시판 수정하기
    @PutMapping("/{boardId}")
    public ResponseEntity<?> updateBoard(@PathVariable Long boardId,
                                         @RequestBody BoardDto boardDto) {
        boardDto.setBoardId(boardId);
        boardService.updateBoard(boardDto);
        return ResponseEntity.ok(Map.of("message", "게시판이 업데이트되었습니다."));
    }

    // 게시판 삭제하기
    @DeleteMapping("/{boardId}")
    public ResponseEntity<?> deleteBoard(@PathVariable Long boardId,
                                         @RequestBody LoginRequest passwordRequest,
                                         Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        if (passwordRequest == null || passwordRequest.getPassword() == null || passwordRequest.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호를 입력하세요.");
        }
        String deleterLoginId = authentication.getName();
        boardService.deleteBoard(boardId, deleterLoginId, passwordRequest.getPassword());
        return ResponseEntity.ok(Map.of("message", "게시판이 삭제되었습니다."));
    }

    // -------------------------- 게시판 카테고리 --------------------------

    // 카테고리 목록 조회
    @GetMapping("/category")
    public ResponseEntity<List<BbsCategoryDto>> getCategories() {
        List<BbsCategoryDto> categories = boardService.getAllBoardCategories();
        return ResponseEntity.ok(categories);
    }

    // 게시판별 카테고리 목록 조회
    @GetMapping("/category/{boardId}")
    public ResponseEntity<List<BbsCategoryDto>> getCategoriesByBoardId(@PathVariable Long boardId) {
        List<BbsCategoryDto> categories = boardService.getCategoriesByBoardId(boardId);
        return ResponseEntity.ok(categories);
    }

    // 게시판 카테고리 추가
    @PostMapping("/category")
    public ResponseEntity<?> createBoardCategory(@RequestBody BbsCategoryDto bbsCategoryDto) {
        boardService.createBoardCategory(bbsCategoryDto);
        return ResponseEntity.ok(Map.of("message", "게시판 카테고리가 생성되었습니다."));
    }

    // 게시판 카테고리 수정
    @PutMapping("/category/{boardCategoryId}")
    public ResponseEntity<?> updateBoardCategory(@PathVariable Long boardCategoryId, @RequestBody BbsCategoryDto bbsCategoryDto) {
        bbsCategoryDto.setBoardCategoryId(boardCategoryId);
        boardService.updateBoardCategory(bbsCategoryDto);
        return ResponseEntity.ok(Map.of("message", "게시판 카테고리가 수정되었습니다."));
    }

    // 게시판 카테고리 삭제
    @DeleteMapping("/category/{boardCategoryId}")
    public ResponseEntity<?> deleteBoardCategory(@PathVariable Long boardCategoryId, @RequestParam String deleterId, @RequestParam String boardCategoryName) {
        boardService.deleteBoardCategory(boardCategoryId, deleterId, boardCategoryName);
        return ResponseEntity.ok(Map.of("message", "게시판 카테고리가 삭제되었습니다."));
    }
}
