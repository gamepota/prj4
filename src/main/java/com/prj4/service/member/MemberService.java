package com.prj4.service.member;

import com.prj4.domain.member.Member;
import com.prj4.mapper.member.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class MemberService {
    final MemberMapper mapper;


    public void add(Member member) {
        mapper.insert(member);
    }
}

