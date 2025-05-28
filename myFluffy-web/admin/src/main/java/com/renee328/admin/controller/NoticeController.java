package com.renee328.admin.controller;

import com.renee328.dto.NoticeDto;
import com.renee328.dto.PostSearchCondition;
import com.renee328.service.NoticeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.renee328.admin.util.Constants.NOTICE_API_URL;

@RestController
@RequestMapping(NOTICE_API_URL)
public class NoticeController {
    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    // 공지 목록 & 개수 조회 (운영자 이상)
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_OPERATOR', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public Map<String, Object> getAllNotices(@RequestBody PostSearchCondition searchCondition) {
        List<NoticeDto> noticeList = noticeService.getAllNotices(searchCondition);
        int totalCount = noticeService.getNoticesCount(searchCondition);
        Map<String, Object> response = new HashMap<>();
        response.put("notices", noticeList);
        response.put("totalCount", totalCount);
        return response;
    }

    // 대시보드 최근 공지 조회
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('ROLE_OPERATOR', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<NoticeDto> getRecentNotice() {
        NoticeDto recentNotice = noticeService.getRecentNotice();
        return ResponseEntity.ok(recentNotice);
    }

    // 공지 세부 내용 조회
    @GetMapping("/detail/{noticeId}")
    @PreAuthorize("hasAnyAuthority('ROLE_OPERATOR', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<NoticeDto> getNoticeDetails(@PathVariable Long noticeId) {
        NoticeDto noticeDetails = noticeService.getNoticeDetails(noticeId);
        return ResponseEntity.ok(noticeDetails);
    }

    // 공지 생성 (일반 관리자 / 슈퍼 관리자)
    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> createNotice(@RequestBody NoticeDto noticeDto) {
        noticeService.createNotice(noticeDto);
        return ResponseEntity.ok(Map.of("message", "공지가 생성되었습니다."));
    }

    // 공지 수정 (일반 관리자 / 슈퍼 관리자)
    @PutMapping("/{noticeId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> updateNotice(@PathVariable Long noticeId, @RequestBody NoticeDto noticeDto) {
        noticeDto.setNoticeId(noticeId);
        noticeService.updateNotice(noticeDto);
        return ResponseEntity.ok(Map.of("message", "공지가 수정되었습니다."));
    }

    // 공지 삭제 (일반 관리자 / 슈퍼 관리자)
    @DeleteMapping("/{noticeId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> deleteNotice(@PathVariable Long noticeId, @RequestParam String deleterId, @RequestParam String title) {
        noticeService.deleteNotice(noticeId, deleterId, title);
        return ResponseEntity.ok(Map.of("message", "공지가 삭제되었습니다."));
    }
}