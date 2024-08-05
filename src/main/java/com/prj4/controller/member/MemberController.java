package com.prj4.controller.member;

import com.prj4.domain.member.Member;
import com.prj4.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService service;

    @PostMapping("signup")
    public void signup(@RequestBody Member member) {
//        System.out.println("member = " + member);
        service.add(member);
    }
}

