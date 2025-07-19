package com.renee328.service.impl;

import com.renee328.dto.UserDto;
import com.renee328.dto.UserSearchCondition;
import com.renee328.mapper.UserMapper;
import com.renee328.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    // 회원 목록 조회하기 (필터, 검색, 정렬 조건 적용)
    @Override
    public List<UserDto> getUserList(UserSearchCondition searchCondition) {
        return userMapper.getUserList(searchCondition);
    }

    // 회원 수 조회하기
    @Override
    public int getUserCount(UserSearchCondition searchCondition) {
        return userMapper.getUserCount(searchCondition);
    }

    // 최근 7일 간의 새로운 회원 수 조회하기
    @Override
    public int getUserCountWeekly() {
        return userMapper.getUserCountWeekly();
    }
}
