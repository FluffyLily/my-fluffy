package com.renee328.mapper;

import com.renee328.dto.AdminDto;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AdminMapper {

    int countByLoginId(String loginId);

    AdminDto findByLoginId(String loginId);

    AdminDto findByUserId(Long userId);

    int getRolePriority(String roleId);

    void insertAdmin(AdminDto adminDto);

    List<AdminDto> getAdminList();

    int getAdminCount();

    void updateAdmin(@Param("userId") Long userId, @Param("adminDto") AdminDto adminDto);

    void updateMyPassword(@Param("userId") Long userId, @Param("adminDto") AdminDto adminDto);

    void deleteAdmin(Long userId);

    void logAdminDeletion(@Param("deletedLoginId") String deletedId, @Param("deleterLoginId") String deleterId);

}
