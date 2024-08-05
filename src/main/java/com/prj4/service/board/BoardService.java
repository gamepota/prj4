package com.prj4.service.board;

import com.prj4.domain.board.Board;
import com.prj4.domain.member.Member;
import com.prj4.mapper.board.BoardMapper;
import com.prj4.mapper.member.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

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


    public Map<String, Object> list(Integer page, String searchType, String keyword) {
        Map pageInfo = new HashMap();
        Integer countAll = mapper.countAllWithSearch(searchType, keyword);


        Integer offset = (page - 1) * 10;
        Integer lastPageNumber = (countAll - 1) / 10 + 1;
        Integer leftPageNumber = (page - 1) / 10 * 10 + 1;
        Integer rightPageNumber = leftPageNumber + 9;
        Integer prevPageNumber = leftPageNumber - 1;
        Integer nextPageNumber = rightPageNumber + 1;


        rightPageNumber = Math.min(rightPageNumber, lastPageNumber);


        if (prevPageNumber > 0) {
            pageInfo.put("prevPageNumber", prevPageNumber);
        }
        if (nextPageNumber <= lastPageNumber) {
            pageInfo.put("nextPageNumber", nextPageNumber);
        }
        pageInfo.put("currentPageNumber", page);
        pageInfo.put("lastPageNumber", lastPageNumber);
        pageInfo.put("leftPageNumber", leftPageNumber);
        pageInfo.put("rightPageNumber", rightPageNumber);


        return Map.of("pageInfo", pageInfo,
                "boardList", mapper.selectAllPaging(offset, searchType, keyword));
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


    public boolean hasAccess(Integer id, Authentication authentication) {
        Board board = mapper.selectById(id);


        return board.getMemberId()
                .equals(Integer.valueOf(authentication.getName()));
    }
}


