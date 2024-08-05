package com.prj4.service.board;

import com.prj4.domain.board.Board;
import com.prj4.domain.member.Member;
import com.prj4.mapper.board.BoardMapper;
import com.prj4.mapper.member.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class BoardService {
    private final BoardMapper mapper;
    private final MemberMapper memberMapper;


    public void add(Board board, Authentication authentication) {
        Member member = memberMapper.selectByEmail(authentication.getName());
        board.setMemberId(Integer.valueOf(authentication.getName()));
        mapper.insert(board);
    }


    public boolean validate(Board board) {
        if (board.getTitle() == null || board.getTitle().isBlank()) {
            return false;
        }


        if (board.getContent() == null || board.getContent().isBlank()) {
            return false;
        }

        return true;
    }

    public List<Board> list() {
        return mapper.selectAll();
    }

    public Board get(Integer id) {
        return mapper.selectById(id);
    }

    public void delete(Integer id) {
        mapper.deleteById(id);
    }

    public void edit(Board board) {
        mapper.update(board);
    }
}

