package com.renee328.service;

import com.renee328.dto.UserDto;
import com.renee328.dto.UserSearchCondition;
import java.util.List;

public interface UserService {
    List<UserDto> getUserList(UserSearchCondition searchCondition);

    int getUserCount(UserSearchCondition searchCondition);

    int getUserCountWeekly();
}
