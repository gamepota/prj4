package com.prj4.controller.board;

import com.prj4.domain.board.Board;
import com.prj4.service.board.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
public class BoardController {


    private final BoardService service;


    @PostMapping("add")
    public ResponseEntity add(@RequestBody Board board) {
        if (service.validate(board)) {
            service.add(board);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("list")
    public List<Board> list() {
        return service.list();
    }

    @GetMapping("{id}")
    public Board get(@PathVariable Integer id) {
        return service.get(id);
    }
}

