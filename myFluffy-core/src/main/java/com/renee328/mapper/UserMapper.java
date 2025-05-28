package com.renee328.mapper;

import com.renee328.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface UserMapper {
    Optional<UserDto> findByEmail(String email);
    void insertUser(UserDto userDto);
}
