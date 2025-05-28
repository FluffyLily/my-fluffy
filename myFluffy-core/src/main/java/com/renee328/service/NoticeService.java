package com.renee328.service;

import com.renee328.dto.NoticeDto;
import com.renee328.dto.PostSearchCondition;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NoticeService {

    // 시스템 공지 목록 조회하기
    @Transactional
    List<NoticeDto> getAllNotices(PostSearchCondition searchCondition);

    // 시스템 공지 글 개수 조회
    @Transactional
    int getNoticesCount(PostSearchCondition searchCondition);

    // 대시보드 최신 공지 글 조회
    @Transactional
    NoticeDto getRecentNotice();

    // 공지 세부 내용 조회
    @Transactional
    NoticeDto getNoticeDetails(Long noticeId);

    // 시스템 공지 작성하기
    @Transactional
    void createNotice(NoticeDto noticeDto);

    // 시스템 공지 수정하기
    @Transactional
    void updateNotice(NoticeDto noticeDto);

    // 시스템 공지 삭제(비활성화)하기
    @Transactional
    void deleteNotice(Long noticeId, String deleterId, String title);
}
