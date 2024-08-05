package com.prj4.service.member;

import com.prj4.domain.member.Member;
import com.prj4.mapper.board.BoardMapper;
import com.prj4.mapper.member.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class MemberService {
    final MemberMapper mapper;
    private final BoardMapper boardMapper;


    final BCryptPasswordEncoder passwordEncoder; // 단방향성으로만 인코딩 디코딩됨
    final JwtEncoder jwtEncoder;

    public void add(Member member) {
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        member.setEmail(member.getEmail().trim());
        member.setNickName(member.getNickName().trim());
        mapper.insert(member);
    }


    public Member getByEmail(String email) {
        return mapper.selectByEmail(email.trim());
    }


    public Member getByNickName(String nickName) {
        return mapper.selectByNickName(nickName.trim());
    }


    public boolean validate(Member member) {
        if (member.getEmail() == null || member.getEmail().isBlank()) {
            return false;
        }


        if (member.getNickName() == null || member.getNickName().isBlank()) {
            return false;
        }


        if (member.getPassword() == null || member.getPassword().isBlank()) {
            return false;
        }


        String emailPattern = "[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*";


        if (!member.getEmail().trim().matches(emailPattern)) {
            return false;
        }


        return true;
    }


    public List<Member> list() {
        return mapper.selectAll();
    }

    public Member getById(Integer id) {
        return mapper.selectById(id);
    }

    public void remove(Integer id) {

        boardMapper.deleteByMemberId(id);
        mapper.deleteById(id);
    }


    public boolean hasAccess(Member member, Authentication authentication) {
        if (!member.getId().toString().equals(authentication.getName())) {
            return false;
        }
        Member dbMember = mapper.selectById(member.getId());


        if (dbMember == null) {
            return false;
        }
        return passwordEncoder.matches(member.getPassword(), dbMember.getPassword());
    }

    public Map<String, Object> modify(Member member, Authentication authentication) {
        if (member.getPassword() != null && member.getPassword().length() > 0) {
            // 패스워드가 입려괴었으니 바꾸기
            member.setPassword(passwordEncoder.encode(member.getPassword()));
        } else {
            Member dbmember = mapper.selectById(member.getId());
            member.setPassword(dbmember.getPassword());
        }
        mapper.update(member);
        String token = "";


        Jwt jwt = (Jwt) authentication.getPrincipal();
        Map<String, Object> claims = jwt.getClaims();
        JwtClaimsSet.Builder jwtClaimsSetBuilder = JwtClaimsSet.builder();
        claims.forEach(jwtClaimsSetBuilder::claim);
        jwtClaimsSetBuilder.claim("nickName", member.getNickName());


        JwtClaimsSet jwtClaimsSet = jwtClaimsSetBuilder.build();
        token = jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSet)).getTokenValue();
        return Map.of("token", token);
    }


    public boolean hasAccessModify(Member member, Authentication authentication) {
        if (!authentication.getName().equals(member.getId().toString())) {
            return false;
        }

        Member dbMember = mapper.selectById(member.getId());
        if (dbMember == null) {
            return false;
        }


        if (!passwordEncoder.matches(member.getOldPassword(), dbMember.getPassword())) {
            return false;
        }


        return true;
    }

    public Map<String, Object> getToken(Member member) {


        Map<String, Object> result = null;


        Member db = mapper.selectByEmail(member.getEmail());


        if (db != null) {
            if (passwordEncoder.matches(member.getPassword(), db.getPassword())) {
                result = new HashMap<>();
                String token = "";
                Instant now = Instant.now();
                List<String> authority = mapper.selectAuthorityByMemberId(db.getId());


                String authorityString = authority.stream()
                        .collect(Collectors.joining(" "));


                // https://github.com/spring-projects/spring-security-samples/blob/main/servlet/spring-boot/java/jwt/login/src/main/java/example/web/TokenController.java
                JwtClaimsSet claims = JwtClaimsSet.builder()
                        .issuer("self")
                        .issuedAt(now)
                        .expiresAt(now.plusSeconds(60 * 60 * 24 * 7))
                        .subject(db.getId().toString())
                        .claim("scope", authorityString) // 권한
                        .claim("nickName", db.getNickName())
                        .build();


                token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();


                result.put("token", token);
            }
        }


        return result;
    }


    public boolean hasAccess(Integer id, Authentication authentication) {
        boolean self = authentication.getName().equals(id.toString());


        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("SCOPE_admin"));


        return self || isAdmin;
    }
}















