package com.spring.ai.joseonannalapi.api.support;

import com.spring.ai.joseonannalapi.config.JwtTokenProvider;
import com.spring.ai.joseonannalapi.domain.user.User;
import com.spring.ai.joseonannalapi.domain.user.UserFinder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserFinder userFinder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserArgumentResolver(UserFinder userFinder, JwtTokenProvider jwtTokenProvider) {
        this.userFinder = userFinder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginUser.class)
                && parameter.getParameterType().equals(User.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization 헤더가 없거나 형식이 올바르지 않습니다.");
        }
        String token = authorization.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
        Long userId = jwtTokenProvider.getUserId(token);
        return userFinder.getById(userId);
    }
}
