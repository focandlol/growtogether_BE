//package com.campfiredev.growtogether;
//
//import focandlol.domain.entity.MemberEntity;
//import focandlol.domain.repository.MemberRepository;
//import com.campfiredev.growtogether.point.service.PointService;
//import com.campfiredev.growtogether.study.dto.post.StudyDTO;
//import com.campfiredev.growtogether.study.dto.post.StudyScheduleDto;
//import com.campfiredev.growtogether.study.service.post.StudyService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.Rollback;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//@SpringBootTest
//@Rollback(false)
//class PointServiceConcurrencyTest {
//
//  @Autowired
//  private PointService pointService;
//
//  @Autowired
//  private StudyService studyService;
//
//  @Autowired
//  private MemberRepository memberRepository;
//
//
//  @Test
//  void lockTest() throws InterruptedException {
//    int numberOfThreads = 10;
//    int amountToUse = 10;
//    ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
//    CountDownLatch latch = new CountDownLatch(numberOfThreads);
//
//    for (int i = 0; i < numberOfThreads; i++) {
//      executorService.submit(() -> {
//        try {
//          StudyScheduleDto scheduleDto = StudyScheduleDto.builder()
//              .dates(List.of("2025-03-19"))
//              .time("14:30")
//              .total(60)
//              .build();
//
//          // 🔨 StudyDTO 생성 (builder 사용)
//          StudyDTO dto = StudyDTO.builder()
//              .title("Study Title")
//              .content("Study Description")
//              .maxParticipant(10)
//              .studyClosingDate(LocalDate.of(2025, 3, 31))
//              .mainScheduleList(scheduleDto)
//              .studyStartDate(LocalDateTime.of(2025, 3, 20, 0, 0))
//              .studyEndDate(LocalDateTime.of(2025, 4, 30, 0, 0))
//              .type("Online")
//              .skillNames(List.of("Spring Boot", "React", "Docker"))
//              .studyCount(1)
//              .build();
//
//          studyService.createStudy(dto, 1L);
//        } catch (Exception e) {
//          System.out.println("예외 발생: " + e.getMessage());
//        } finally {
//          latch.countDown();
//        }
//      });
//    }
//
//    latch.await();
//
//    MemberEntity updatedMember = memberRepository.findById(1L).orElseThrow();
//
//    System.out.println("최종 포인트: " + updatedMember.getPoints());
//
//  }
//
//
//}
