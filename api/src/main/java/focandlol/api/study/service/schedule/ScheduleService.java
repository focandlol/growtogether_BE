package focandlol.api.study.service.schedule;


import static focandlol.common.exception.response.ErrorCode.CANNOT_DELETE_MAIN_SCHEDULE;
import static focandlol.common.exception.response.ErrorCode.CANNOT_OVERLAP_WITH_MAIN_SCHEDULE_TIME;
import static focandlol.common.exception.response.ErrorCode.MAIN_SCHEDULE_CONFLICT;
import static focandlol.common.exception.response.ErrorCode.NOT_AUTHOR;
import static focandlol.common.exception.response.ErrorCode.NOT_A_STUDY_MEMBER;
import static focandlol.common.exception.response.ErrorCode.SCHEDULE_NOT_FOUND;
import static focandlol.domain.type.ScheduleType.MAIN;
import static focandlol.domain.type.StudyMemberType.LEADER;
import static focandlol.domain.type.StudyMemberType.NORMAL;

import focandlol.api.point.service.PointService;
import focandlol.api.study.service.vote.VoteService;
import focandlol.common.exception.custom.CustomException;
import focandlol.domain.dto.study.schedule.MainScheduleDto;
import focandlol.domain.dto.study.schedule.ScheduleAttendeeDto;
import focandlol.domain.dto.study.schedule.ScheduleAttendeeMonthDto;
import focandlol.domain.dto.study.schedule.ScheduleCreateDto;
import focandlol.domain.dto.study.schedule.ScheduleDto;
import focandlol.domain.dto.study.schedule.ScheduleMonthDto;
import focandlol.domain.dto.study.schedule.ScheduleUpdateDto;
import focandlol.domain.entity.PointTransaction.TransactionType;
import focandlol.domain.entity.Study;
import focandlol.domain.entity.join.StudyMemberEntity;
import focandlol.domain.entity.schedule.ScheduleEntity;
import focandlol.domain.repository.attendance.AttendanceRepository;
import focandlol.domain.repository.join.JoinRepository;
import focandlol.domain.repository.post.StudyRepository;
import focandlol.domain.repository.schedule.ScheduleRepository;
import focandlol.domain.type.StudyStatus;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final JoinRepository joinRepository;
    private final VoteService voteService;
    private final AttendanceRepository attendanceRepository;
    private final StudyRepository studyRepository;
    private final Scheduler scheduler;
    private final PointService pointService;


    public void createMainSchedule(Study study, Long memberId, List<MainScheduleDto> mainList) {
        mainList.sort(Comparator.comparing(s -> s.getStartTime()));

        for (int i = 1; i < mainList.size(); i++) {
            MainScheduleDto prev = mainList.get(i - 1);
            MainScheduleDto current = mainList.get(i);

            if (!current.getStartTime().isAfter(prev.getEndTime())) {
                throw new CustomException(MAIN_SCHEDULE_CONFLICT);
            }
        }

        StudyMemberEntity studyMemberEntity = getStudyMemberEntity(study.getStudyId(), memberId);

        List<ScheduleEntity> schedules = mainList.stream()
                .map(main -> ScheduleEntity.builder()
                        .title("메인 일정입니다.")
                        .studyMember(studyMemberEntity)
                        .study(study)
                        .start(main.getStartTime())
                        .end(main.getEndTime())
                        .totalTime(main.getTotal())
                        .type(MAIN)
                        .build())
                .collect(Collectors.toList());

        scheduleRepository.saveAll(schedules);
    }

    public void createSchedule(Long studyId, Long memberId, ScheduleCreateDto scheduleCreateDto) {
        StudyMemberEntity studyMemberEntity = getStudyMemberEntity(studyId, memberId);

        ScheduleEntity save = scheduleRepository.save(
                ScheduleEntity.create(studyMemberEntity, scheduleCreateDto));

        LocalDateTime start = LocalDateTime.of(scheduleCreateDto.getStartDate(),
                scheduleCreateDto.getStartTime());
        LocalDateTime end = start.plusMinutes(scheduleCreateDto.getTotalTime());

        checkMainConflict(studyMemberEntity.getStudy(), start, end, save.getId());
    }

    public void updateSchedule(Long memberId, Long scheduleId, ScheduleUpdateDto updateDto) {
        ScheduleEntity scheduleEntity = getScheduleEntity(scheduleId);

        LocalDateTime start = LocalDateTime.of(updateDto.getStartDate(), updateDto.getStartTime());
        LocalDateTime end = start.plusMinutes(updateDto.getTotalTime());

        checkMainConflict(scheduleEntity.getStudy(), start, end, scheduleEntity.getId());

        if (validateCreateVote(memberId, updateDto, scheduleEntity, start, end)) {
            return;
        }

        validateSameUser(memberId, scheduleEntity);

        scheduleEntity.setTitle(updateDto.getTitle());
        scheduleEntity.setStart(start);
        scheduleEntity.setEnd(end);
        scheduleEntity.setTotalTime(updateDto.getTotalTime());
    }

    public void deleteSchedule(Long memberId, Long scheduleId) {
        ScheduleEntity scheduleEntity = getScheduleEntity(scheduleId);

        if (MAIN.equals(scheduleEntity.getType())) {
            throw new CustomException(CANNOT_DELETE_MAIN_SCHEDULE);
        }

        validateSameUser(memberId, scheduleEntity);

        scheduleRepository.delete(scheduleEntity);
    }

    public List<ScheduleDto> getSchedules(Long studyId, LocalDate date) {
        return scheduleRepository.findWithMemberByStudyIdAndDate(studyId, date.atStartOfDay(),
                        date.atTime(LocalTime.MAX)).stream()
                .map(entity -> ScheduleDto.fromEntity(entity))
                .collect(Collectors.toList());
    }

    public ScheduleMonthDto getMonthSchedules(Long studyId, String date) {
        YearMonth yearMonth = YearMonth.parse(date);

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Map<LocalDate, List<ScheduleDto>> collect = scheduleRepository.findWithMemberByStudyIdAndDateBetween(
                        studyId, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX))
                .stream()
                .map(scheduleEntity -> ScheduleDto.fromEntity(scheduleEntity))
                .collect(Collectors.groupingBy(scheduleDto -> scheduleDto.getStart().toLocalDate()));

        return ScheduleMonthDto.from(collect);
    }

    public ScheduleAttendeeMonthDto getMonthSchedulesAttendees(Long studyId, String date) {
        YearMonth yearMonth = YearMonth.parse(date);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<ScheduleEntity> schedules = scheduleRepository.findSchedulesWithAttendee(studyId,
                startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));

        Map<Long, List<String>> attendanceMap = schedules.stream()
                .flatMap(schedule -> schedule.getAttendance().stream()
                        .map(attendance -> Map.entry(schedule.getId(),
                                attendance.getStudyMember().getMember().getNickName()))
                )
                .collect(Collectors.groupingBy(
                        id -> id.getKey(),
                        Collectors.mapping(attend -> attend.getValue(), Collectors.toList())
                ));

        Map<LocalDate, List<ScheduleAttendeeDto>> groupedSchedules = schedules.stream()
                .map(schedule -> ScheduleAttendeeDto.create(
                        schedule,
                        schedule.getStudyMember().getMember().getNickName(),
                        attendanceMap.getOrDefault(schedule.getId(), new ArrayList<>())
                ))
                .collect(Collectors.groupingBy(
                        (ScheduleAttendeeDto scheduleAttendeeDto) -> scheduleAttendeeDto.getStart()
                                .toLocalDate()));

        return ScheduleAttendeeMonthDto.from(groupedSchedules);
    }

    private void checkMainConflict(Study study, LocalDateTime start, LocalDateTime end,
                                   Long scheduleId) {
        List<ScheduleEntity> mainSchedules = scheduleRepository.findByStudyAndStartBetweenAndType(
                study, start, end, MAIN);

        for (ScheduleEntity schedule : mainSchedules) {
            if (schedule.getId().equals(scheduleId)) {
                continue;
            }

            if (!(end.isBefore(schedule.getStart()) || start.isAfter(schedule.getEnd()))) {
                throw new CustomException(CANNOT_OVERLAP_WITH_MAIN_SCHEDULE_TIME);
            }
        }
    }

    private boolean validateCreateVote(Long memberId,
                                       ScheduleUpdateDto scheduleUpdateDto,
                                       ScheduleEntity scheduleEntity, LocalDateTime start, LocalDateTime end) {

        if (MAIN.equals(scheduleEntity.getType()) && (!start.equals(scheduleEntity.getStart()))
                || !end.equals(scheduleEntity.getEnd())) {
            voteService.createChangeVote(memberId, scheduleEntity.getStudy().getStudyId(), scheduleEntity,
                    scheduleUpdateDto);
            return true;
        }
        return false;
    }

    private void validateSameUser(Long memberId, ScheduleEntity scheduleEntity) {
        StudyMemberEntity studyMemberEntity = getStudyMemberEntity(
                scheduleEntity.getStudy().getStudyId(), memberId);

        if (!memberId.equals(studyMemberEntity.getMember().getMemberId())) {
            throw new CustomException(NOT_AUTHOR);
        }
    }

    private StudyMemberEntity getStudyMemberEntity(Long studyId, Long memberId) {
        return joinRepository.findByMember_MemberIdAndStudy_StudyIdAndStatusIn(memberId,
                        studyId, List.of(LEADER, NORMAL))
                .orElseThrow(() -> new CustomException(NOT_A_STUDY_MEMBER));
    }

    private ScheduleEntity getScheduleEntity(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(SCHEDULE_NOT_FOUND));
    }

    @PostConstruct
    public void initializeScheduler() throws SchedulerException {

        JobDetail jobDetail = JobBuilder.newJob(UpdateStudyStatusJob.class)
                .withIdentity("updateStudyStatusJob")
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("updateStudyStatusTrigger")
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(1, 0))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }


    public void updateExpiredStudies() {

        List<Study> expiredStudies = scheduleRepository.findStudiesWithLastScheduleExpired(LocalDateTime.now());

        expiredStudies.forEach(study -> study.setStudyStatus(StudyStatus.COMPLETE));

        studyRepository.saveAll(expiredStudies);

        expiredStudies.forEach(this::refundDeposit);
    }

    private void refundDeposit(Study study) {
        List<ScheduleEntity> schedules = scheduleRepository.findAllByStudy_StudyIdAndType(study.getStudyId(), MAIN);

        List<StudyMemberEntity> studyMembers = joinRepository.findAllByStudy_StudyId(study.getStudyId());

        for (StudyMemberEntity studyMember : studyMembers) {
            long count = schedules.stream()
                    .filter(schedule -> attendanceRepository.existsByStudyMemberIdAndScheduleId(
                            studyMember.getId(), schedule.getId()))
                    .count();

            if((double) count /schedules.size()>=0.7){
                pointService.addPoint(studyMember.getMember().getMemberId(),schedules.size()*5,
                    TransactionType.REWARD);
            }
        }
    }
}
