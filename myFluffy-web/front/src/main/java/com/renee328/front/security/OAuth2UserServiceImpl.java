package com.renee328.front.security;

import com.renee328.dto.UserDto;
import com.renee328.mapper.UserMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OAuth2UserServiceImpl implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserMapper userMapper;

    public OAuth2UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String uuid = oAuth2User.getAttribute("sub"); // Google 고유 식별자

        Optional<UserDto> optionalUser = userMapper.findByEmail(email);

        UserDto userDto;
        if (optionalUser.isPresent()) {
            userDto = optionalUser.get();
        } else {
            // 신규 사용자 등록
            userDto = new UserDto();
            userDto.setEmail(email);
            userDto.setUserName(name);
            userDto.setLoginId(email); // Google 계정으로 대체
            userDto.setUserTypeCode("USER_GOOGLE");
            userDto.setUuid(uuid);
            userDto.setCreatedBy(0L); // 시스템 계정 또는 비회원 등록자
            userDto.setUpdatedBy(0L);
            userMapper.insertUser(userDto);
        }

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                oAuth2User.getAttributes(),
                "email"
        );
    }
}
