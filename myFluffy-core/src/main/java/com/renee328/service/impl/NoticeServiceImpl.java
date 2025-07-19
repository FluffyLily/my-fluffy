package com.renee328.service.impl;

import com.renee328.dto.NoticeDto;
import com.renee328.dto.PostSearchCondition;
import com.renee328.mapper.NoticeMapper;
import com.renee328.service.NoticeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;

    public NoticeServiceImpl(NoticeMapper noticeMapper) {
        this.noticeMapper = noticeMapper;
    }

    @Override
    public List<NoticeDto> getAllNotices(PostSearchCondition searchCondition) {
        return noticeMapper.findActiveNotices(searchCondition);
    }

    @Override
    public int getNoticesCount(PostSearchCondition searchCondition) {
        return noticeMapper.getNoticesCount(searchCondition);
    }

    @Override
    public NoticeDto getRecentNotice() {
        return noticeMapper.getRecentNotice();
    }

    @Override
    public NoticeDto getNoticeDetails(Long noticeId) {
        return noticeMapper.getNoticeDetails(noticeId);
    }

    @Transactional
    @Override
    public void createNotice(NoticeDto noticeDto) {
        noticeMapper.insertNotice(noticeDto);
    }

    @Transactional
    @Override
    public void updateNotice(NoticeDto noticeDto) {
        noticeMapper.updateNotice(noticeDto);
    }

    @Transactional
    @Override
    public void deleteNotice(Long noticeId, String deleterId, String title) {
        noticeMapper.deleteNotice(noticeId);
        noticeMapper.logNoticeDeletion(deleterId, title);
    }
}