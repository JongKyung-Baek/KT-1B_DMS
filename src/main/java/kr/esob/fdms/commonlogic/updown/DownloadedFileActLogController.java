package kr.esob.fdms.commonlogic.updown;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.io.*;
import java.nio.file.Paths;
import java.util.List;

@RestController
public class DownloadedFileActLogController {

    @Autowired
    private CommonUpdownDao dao;

    @PostMapping("/log/downloadedFileActLog")
    public String receiveData(@RequestBody List<DownloadedFileActLogDto> logs){

        // @RequestBody가 JSON을 자동으로 java 객체로 변환 시켜줌 ( 이때 java 객체로 저장되기 위해서 Dto가 있는거임 )


        for(DownloadedFileActLogDto log : logs){

            System.out.println("받은 데이터 act_time: " + log.getAct_time());

            dao.saveLog(log); // db에 저장 CommonUpdownDao로 이동
        }



        return "데이터 받고 처리하고 DB에 저장 완료 ";
    }



}
