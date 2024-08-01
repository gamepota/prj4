package com.prj4.controller.board;

import com.prj4.domain.board.Board;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/board")
public class BoardController {

    @PostMapping("add")
    public void add(@RequestBody Board board) {

    }
}
