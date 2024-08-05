package com.prj4.service.board;

import com.prj4.domain.board.Board;
import com.prj4.domain.board.BoardFile;
import com.prj4.mapper.board.BoardMapper;
import com.prj4.mapper.member.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class BoardService {
    private final BoardMapper mapper;
    private final MemberMapper memberMapper;
    final S3Client s3Client;
    @Value("${aws.s3.bucket.name}")
    String bucketName;


    @Value("${image.src.prefix}")
    String srcPrefix;


    public void add(Board board, MultipartFile[] files, Authentication authentication) throws IOException {
        board.setMemberId(Integer.valueOf(authentication.getName()));
        // 게시물 저장
        mapper.insert(board);


        // db에 해당 게시물의 파일 목록 저장
        if (files != null) {
            for (MultipartFile file : files) {
                mapper.insertFileName(board.getId(), file.getOriginalFilename());
                // 실제 파일 저장
                // 부모 디렉토기 만들기
//                String dir = STR."C:/Temp/prj2/\{board.getId()}";
//                File dirFile = new File(dir);
//                if (!dirFile.exists()) {
//                    dirFile.mkdirs();
//                }
//
//                //파일경로
//                String path = STR."C:/Temp/prj2/\{board.getId()}/\{file.getOriginalFilename()}";
//                File destination = new File(path);
//                file.transferTo(destination);


                // 실제 파일 저장(new)
                String key = STR."prj4/\{board.getId()}/\{file.getOriginalFilename()}";
                PutObjectRequest objectRequest = PutObjectRequest
                        .builder()
                        .bucket(bucketName)
                        .key(key)
                        .acl(ObjectCannedACL.PUBLIC_READ)
                        .build();


                s3Client.putObject(objectRequest,
                        RequestBody.fromInputStream(file.getInputStream(), file.getSize()));


            }
        }


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


    public Map<String, Object> get(Integer id, Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        Board board = mapper.selectById(id);
        List<String> fileNames = mapper.selectFileNameByBoardId(id);
        //  http://172.30.1.24:8888/{id}/{name}
        List<BoardFile> files = fileNames.stream()
                .map(name -> new BoardFile(name, STR."\{srcPrefix}\{id}/\{name}"))
                .toList();


        board.setFileList(files);
        Map<String, Object> like = new HashMap<>();
        if (authentication == null) {
            like.put("like", false);


        } else {
            int c = mapper.selectLikeByBoardIdAndMemberId(id, authentication.getName());
            like.put("like", c == 1);
        }
        like.put("count", mapper.countTotal(id));
        result.put("board", board);
        result.put("like", like);


        return result;
    }


    public void delete(Integer id) {
        // file 명 조회
        List<String> fileNames = mapper.selectFileNameByBoardId(id);


        // disk 에 있는 file
//        String dir = STR."C:/Temp/prj2/\{id}/";
//        for (String fileName : fileNames) {
//            File file = new File(dir + fileName);
//            file.delete();
//        }
//        File dirFile = new File(dir);
//        if (dirFile.exists()) {
//            dirFile.delete();
//        }


        //s3 에 있는  file
        for (String fileName : fileNames) {
            String key = STR."prj4/\{id}/\{fileName}";
            DeleteObjectRequest requestObject = DeleteObjectRequest.builder()
                    .bucket(bucketName).key(key).build();
            s3Client.deleteObject(requestObject);
        }


        // board_file
        mapper.deleteFileByBoardId(id);

        //board_like
        mapper.deleteLikeByBoardId(id);


        // board
        mapper.deleteById(id);
    }


    public void edit(Board board, List<String> removeFileList, MultipartFile[] addFileList) throws IOException {
        if (removeFileList != null && removeFileList.size() > 0) {
            for (String fileName : removeFileList) {
//                // disk의 파일 삭제
//                String path = STR."C:/Temp/prj2/\{board.getId()}/\{fileName}";
//                File file = new File(path);
//                file.delete();
                String key = STR."prj4/\{board.getId()}\{fileName}";
                DeleteObjectRequest requestObject = DeleteObjectRequest.builder().bucket(bucketName).key(key).build();
                s3Client.deleteObject(requestObject);
                // db records 삭제
                mapper.deleteFileByBoardIdAndName(board.getId(), fileName);
            }
        }


        if (addFileList != null && addFileList.length > 0) {
            List<String> fileNameList = mapper.selectFileNameByBoardId(board.getId());
            for (MultipartFile file : addFileList) {
                String fileName = file.getOriginalFilename();
                if (!fileNameList.contains(fileName)) {
                    // 새 파일이 기존에 없을 때만 db에 추가
                    mapper.insertFileName(board.getId(), fileName);
                }
                // disk 에 쓰기
//                File dir = new File(STR."C:/Temp/prj2/\{board.getId()}");
//                if (!dir.exists()) {
//                    dir.mkdirs();
//                }
//                String path = STR."C:/Temp/prj2/\{board.getId()}/\{fileName}";
//                File destination = new File(path);
//                file.transferTo(destination);
                String key = STR."prj2/\{board.getId()}/\{fileName}";
                PutObjectRequest objectRequest = PutObjectRequest.builder().bucket(bucketName).key(key).acl(ObjectCannedACL.PUBLIC_READ).build();
                s3Client.putObject(objectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            }
        }


        mapper.update(board);
    }


    public boolean hasAccess(Integer id, Authentication authentication) {
        Board board = mapper.selectById(id);


        return board.getMemberId()
                .equals(Integer.valueOf(authentication.getName()));
    }

    public Map<String, Object> like(Map<String, Object> req, Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        result.put("like", false);
        Integer boardId = (Integer) req.get("boardId");
        Integer memberId = Integer.valueOf(authentication.getName());


        // 이미 했으면
        int count = mapper.deleteLikeByBoardIdAndMemberId(boardId, memberId);


        //안했으면
        if (count == 0) {
            mapper.insertLikeByBoardIdAndMemberId(boardId, memberId);
            result.put("like", true);
        }


        result.put("count", mapper.countTotal(boardId));


        return result;
    }
}




