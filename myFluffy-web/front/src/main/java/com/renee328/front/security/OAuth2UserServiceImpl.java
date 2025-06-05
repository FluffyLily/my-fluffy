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
import java.util.Map;
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
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        String email = null;
        String name = null;
        String uuid = null;

        if ("google".equals(registrationId)) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
            uuid = oAuth2User.getAttribute("id");

        } else if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            email = (String) kakaoAccount.get("email");
            name = (String) profile.get("nickname");
            uuid = String.valueOf(oAuth2User.getAttribute("id"));
        }

        if (email == null || uuid == null) {
            throw new OAuth2AuthenticationException("필수 사용자 정보가 없습니다.");
        }

        Optional<UserDto> optionalUser = userMapper.findByEmail(email);
        UserDto userDto;

        if (optionalUser.isPresent()) {
            userDto = optionalUser.get();
            userDto.setLoginCount(userDto.getLoginCount() + 1);
            userDto.setUpdatedBy(0L);
            userMapper.updateUserLoginInfo(userDto); // 로그인 시간, 카운트 갱신
        } else {
            userDto = new UserDto();
            userDto.setEmail(email);
            userDto.setUserName(name);
            userDto.setLoginId(email);
            userDto.setUserTypeCode(registrationId);
            userDto.setUuid(uuid);
            userDto.setCreatedBy(0L);
            userDto.setUpdatedBy(0L);
            userDto.setLoginCount(1);
            userMapper.insertUser(userDto);
        }

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                oAuth2User.getAttributes(),
                "email"
        );
    }
}
