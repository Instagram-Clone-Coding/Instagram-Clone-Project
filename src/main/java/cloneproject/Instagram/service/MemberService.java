package cloneproject.Instagram.service;



import javax.transaction.Transactional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import cloneproject.Instagram.dto.JwtDto;
import cloneproject.Instagram.dto.LoginRequest;
import cloneproject.Instagram.dto.RegisterRequest;
import cloneproject.Instagram.dto.ReissueRequest;
import cloneproject.Instagram.entity.auth.RefreshToken;
import cloneproject.Instagram.entity.member.Member;
import cloneproject.Instagram.exception.UseridAlreadyExistException;
import cloneproject.Instagram.repository.MemberRepository;
import cloneproject.Instagram.repository.RefreshTokenRepository;
import cloneproject.Instagram.util.JwtUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public void register(RegisterRequest registerRequest){
        if(memberRepository.findByUsername(registerRequest.getUsername()).isPresent()){
            throw new UseridAlreadyExistException();
        }
        Member member = registerRequest.convert();
        String encryptedPassword = bCryptPasswordEncoder.encode(member.getPassword());
        member.setEncryptedPassword(encryptedPassword);
        memberRepository.save(member);
    }

    @Transactional
    public JwtDto login(LoginRequest loginRequest){
        UsernamePasswordAuthenticationToken authenticationToken = 
            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        JwtDto jwtDto = jwtUtil.generateTokenDto(authentication);
        RefreshToken refreshToken = RefreshToken.builder()
                                            .memberId(authentication.getName())
                                            .tokenValue(jwtDto.getRefreshToken())
                                            .build();
        refreshTokenRepository.save(refreshToken);
        
        return jwtDto;
    }

    @Transactional
    public JwtDto reisuue(ReissueRequest reissueRequest){
        String accessTokenString = reissueRequest.getAccessToken();
        String refreshTokenString = reissueRequest.getRefreshToken();
        if(!jwtUtil.validateRefeshJwt(refreshTokenString)){
            // TODO exception 만들기
            throw new RuntimeException("REFRESH TOKEN 이상함");
        }
        Authentication authentication = jwtUtil.getAuthentication(accessTokenString);
        // TODO 서버로 요청해야함, exception
        RefreshToken refreshToken = refreshTokenRepository.findByMemberId(authentication.getName())
                                        .orElseThrow(() -> new RuntimeException("로그아웃했네요"));

        if(!refreshToken.getTokenValue().equals(refreshTokenString)){
            //TODO exception
            throw new RuntimeException("잘못된토큰입니다");
        }
        
        JwtDto jwtDto = jwtUtil.generateTokenDto(authentication);

        refreshToken.updateTokenValue(jwtDto.getRefreshToken());
        refreshTokenRepository.save(refreshToken);

        return jwtDto;
    }

    // ! login 권한 테스트를 위해 임시로 만든 메서드입니다. 추후에 삭제
    public Member info(String memberId){
        return memberRepository.getById(Long.valueOf(memberId));
    }

}
