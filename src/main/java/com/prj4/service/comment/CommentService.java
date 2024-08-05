package com.prj4.service.comment;

import com.prj4.domain.comment.Comment;
import com.prj4.mapper.comment.CommentMapper;
import com.prj4.mapper.member.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class CommentService {


    final CommentMapper mapper;
    private final MemberMapper memberMapper;


    public void add(Comment comment, Authentication authentication) {
        comment.setMemberId(Integer.valueOf(authentication.getName()));


        mapper.insert(comment);
    }


    public List<Comment> list(Integer boardId) {


        return mapper.selectAllByBoardId(boardId);
    }


    public boolean validate(Comment comment) {
        if (comment == null) {
            return false;
        }
        if (comment.getComment().isBlank()) {
            return false;
        }
        return true;
    }


    public void remove(Comment comment) {
        mapper.deleteById(comment.getId());
    }


    public boolean hasAccess(Comment comment, Authentication authentication) {
        Comment db = mapper.selectbyId(comment.getId());
        if (db == null) {
            return false;
        }


        if (!authentication.getName().equals(db.getMemberId().toString())) {
            return false;
        }


        return true;
    }


    public void edit(Comment comment) {
        mapper.update(comment);
    }
}
