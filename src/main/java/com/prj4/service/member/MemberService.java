package com.prj4.service.member;

import com.prj4.domain.member.Member;
import com.prj4.mapper.member.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class MemberService {
    final MemberMapper mapper;


    final BCryptPasswordEncoder passwordEncoder; // 단방향성으로만 인코딩 디코딩됨


    public void add(Member member) {
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        member.setEmail(member.getEmail().trim());
        member.setNickName(member.getNickName().trim());
        mapper.insert(member);
    }


    public Member getByEmail(String email) {
        return mapper.selectByEmail(email);
    }


    public Member getByNickName(String nickName) {
        return mapper.selectByNickName(nickName);
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
}



