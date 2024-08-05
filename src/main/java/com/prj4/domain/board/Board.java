package com.prj4.domain.board;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Board {
    private Integer id;
    private String title;
    private String content;
    private String writer; // nickname
    private Integer memberId;
    //
    private LocalDateTime inserted;
}
