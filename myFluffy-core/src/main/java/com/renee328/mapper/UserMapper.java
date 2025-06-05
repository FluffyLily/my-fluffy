package com.renee328.mapper;

import com.renee328.dto.UserDto;
import com.renee328.dto.UserSearchCondition;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {
    // 회원 등록
    void insertUser(UserDto userDto);
    // 이메일로 회원 조회
    Optional<UserDto> findByEmail(String email);
    // 회원 목록 조회
    List<UserDto> getUserList(UserSearchCondition searchCondition);
    // 회원 수 조회
    int getUserCount(UserSearchCondition searchCondition);
    // 최근 7일 간의 새로운 회원 수 조회
    int getUserCountWeekly();
    // 사용자 로그인 카운트 & 시간 업데이트
    void updateUserLoginInfo(UserDto userDto);

}
