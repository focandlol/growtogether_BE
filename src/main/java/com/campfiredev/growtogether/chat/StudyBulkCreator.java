package com.campfiredev.growtogether.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

public class StudyBulkCreator {

  private static final String JWT_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6ImZvY2FuZGxvbEBuYXZlci5jb20iLCJtZW1iZXJJZCI6Mywibmlja05hbWUiOiJra20iLCJpYXQiOjE3NDI5NTMwOTMsImV4cCI6MTc0Mjk4OTA5M30.OaVyyY2RYgKl7ZYF5jn-1QZ4WtxiGdDJX2VTG5UY3Vo"; // 🔐 토큰 여기에 넣기
  private static final String API_URL = "http://localhost:8080/api/study";
  private static final RestTemplate restTemplate = new RestTemplate();
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private static final List<String> skills = Arrays.asList("Spring Boot", "React","Redis","MySQL");

  public static void main(String[] args) throws Exception {
    for (int i = 1; i <= 10_000; i++) {
      StudyDTO study = generateStudy(i);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("Authorization", JWT_TOKEN);

      HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(study), headers);
      ResponseEntity<String> response = restTemplate.postForEntity(API_URL, entity, String.class);

      if (response.getStatusCode().is2xxSuccessful()) {
        System.out.println("Study " + i + " 생성 성공");
      } else {
        System.out.println(" Study " + i + " 실패: " + response.getStatusCode() + " - " + response.getBody());
      }

      Thread.sleep(10); // 서버 과부하 방지용
    }
  }

  private static StudyDTO generateStudy(int i) {
    return new StudyDTO(
        "Spring Boot 스터디 모집 #" + i,
        "수요일 7시에 8회 진행하는 스터디입니다. (no." + i + ")",
        10,
        "2025-03-31",
        new MainScheduleList(
            Arrays.asList("2025-04-01", "2025-04-02", "2025-04-03"),
            "18:00",
            60
        ),
        "STUDY",
        getRandomSkills(3)
    );
  }

  private static List<String> getRandomSkills(int count) {
    Collections.shuffle(skills);
    return skills.subList(0, count);
  }

  // StudyDTO 클래스 정의
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  static class StudyDTO {
    private String title;
    private String content;
    private int maxParticipant;
    private String studyClosingDate;
    private MainScheduleList mainScheduleList;
    private String type;
    private List<String> skillNames;
  }

  // MainScheduleList 클래스 정의
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  static class MainScheduleList {
    private List<String> dates;
    private String time;
    private int total;
  }
}
