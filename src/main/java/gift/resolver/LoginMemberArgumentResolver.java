package gift.resolver;

import gift.annotation.LoginMember;
import gift.exception.InvalidCredentialsException;
import gift.member.service.MemberService;
import gift.token.service.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    TokenProvider tokenProvider;
    MemberService memberService;

    public LoginMemberArgumentResolver(TokenProvider tokenProvider, MemberService memberService) {
        this.tokenProvider = tokenProvider;
        this.memberService = memberService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class);
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter,
            ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) throws Exception {
        String token = tokenProvider.resolveToken(
                (HttpServletRequest) webRequest.getNativeRequest());
        if (token == null) {
            throw new InvalidCredentialsException("인증이 필요합니다.");
        }

        return memberService.findByUuid(tokenProvider.getMemberUuidFromAccessToken(token));
    }
}
