package com.renee328.admin.controller;

import com.renee328.dto.UserDto;
import com.renee328.dto.UserSearchCondition;
import com.renee328.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.renee328.admin.util.Constants.USER_API_URL;

@RestController
@RequestMapping(USER_API_URL)
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 전체 회원 목록 & 수 조회하기
    @PostMapping("/list")
    public Map<String, Object> getUser(@RequestBody UserSearchCondition searchCondition) {
        List<UserDto> userList = userService.getUserList(searchCondition);
        int totalCount = userService.getUserCount(searchCondition);

        Map<String, Object> response = new HashMap<>();
        response.put("users", userList);
        response.put("totalCount", totalCount);
        return response;
    }

    // 대시보드 전체 회원 수 / 최근 7일 간의 새로운 회원 수 조회
    @GetMapping("/count-summary")
    public ResponseEntity<Map<String, Object>> getUserCountSummary() {
        int totalCount = userService.getUserCount(new UserSearchCondition());
        int weeklyCount = userService.getUserCountWeekly();

        Map<String, Object> response = new HashMap<>();
        response.put("totalCount", totalCount);
        response.put("weeklyCount", weeklyCount);
        return ResponseEntity.ok(response);
    }

}
