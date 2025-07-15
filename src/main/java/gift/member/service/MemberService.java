package gift.member.service;

import gift.exception.EntityAlreadyExistsException;
import gift.exception.EntityNotFoundException;
import gift.exception.InvalidCredentialsException;
import gift.member.dto.AccessTokenRefreshResponseDto;
import gift.member.dto.MemberDto;
import gift.member.dto.MemberLoginRequestDto;
import gift.member.dto.MemberLoginResponseDto;
import gift.member.dto.MemberRegisterRequestDto;
import gift.member.dto.MemberTokenDto;
import gift.member.dto.AccessTokenRefreshRequestDto;
import gift.member.entity.Member;
import gift.member.repository.MemberRepository;
import gift.token.service.TokenProvider;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    public MemberService(MemberRepository memberRepository, TokenProvider tokenProvider) {
        this.memberRepository = memberRepository;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public MemberDto register(MemberRegisterRequestDto requestDto) {
        if (memberRepository.existsByEmail(requestDto.email())) {
            throw new EntityAlreadyExistsException("이미 가입된 계정입니다.");
        }

        Member member = new Member(requestDto);
        memberRepository.save(member);
        return new MemberDto(member);
    }

    @Transactional
    public MemberLoginResponseDto login(MemberLoginRequestDto requestDto) {
        Member member = memberRepository.findByEmail(requestDto.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!member.verifyPassword(requestDto.password())) {
            throw new InvalidCredentialsException();
        }

        MemberTokenDto memberTokenDto = new MemberTokenDto(
                tokenProvider.generateAccessToken(member),
                tokenProvider.generateRefreshToken(member));
        return new MemberLoginResponseDto(new MemberDto(member), memberTokenDto);
    }

    public AccessTokenRefreshResponseDto refreshAccessToken(
            AccessTokenRefreshRequestDto requestDto) {
        UUID memberUuid = tokenProvider.getMemberUuidFromRefreshToken(requestDto.refreshToken());

        return new AccessTokenRefreshResponseDto(tokenProvider.generateAccessToken(findByUuid(memberUuid)));
    }


    public Member findByUuid(UUID uuid) throws EntityNotFoundException {
        return memberRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
    }
}
