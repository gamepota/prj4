package com.prj4.domain.comment;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Data
public class Comment {
    private Integer id;
    private Integer boardId;
    private Integer memberId;
    private String comment;
    private LocalDateTime inserted;


    private String nickName;


    public String getInserted() {
        LocalDateTime beforeOneDay = LocalDateTime.now().minusDays(1);


        if (inserted.isBefore(beforeOneDay)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return inserted.format(formatter).toString();
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            return inserted.format(formatter).toString();
        }
    }
}
