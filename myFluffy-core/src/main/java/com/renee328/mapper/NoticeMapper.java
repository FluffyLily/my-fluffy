package com.renee328.mapper;

import com.renee328.dto.NoticeDto;
import com.renee328.dto.PostSearchCondition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoticeMapper {
    List<NoticeDto> findActiveNotices(PostSearchCondition searchCondition);
    int getNoticesCount(PostSearchCondition searchCondition);
    NoticeDto getRecentNotice();
    NoticeDto getNoticeDetails(@Param("noticeId") Long noticeId);
    void insertNotice(NoticeDto noticeDto);
    void updateNotice(NoticeDto noticeDto);
    void deleteNotice(@Param("noticeId") Long noticeId);
    void logNoticeDeletion(@Param("deleterId") String deleterId, @Param("title") String title);
}
