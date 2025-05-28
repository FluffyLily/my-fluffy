package com.renee328.mapper;

import com.renee328.dto.AdminDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthMapper {
    AdminDto getByUsername(String loginId);

    void updateUserInitializationStatus(@Param("userId") Long userId, @Param("isInitialized") Boolean isInitialized);
}
