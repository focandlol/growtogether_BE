package com.campfiredev.growtogether.study.service.post;

import com.campfiredev.growtogether.exception.custom.CustomException;
import com.campfiredev.growtogether.exception.response.ErrorCode;
import com.campfiredev.growtogether.member.entity.MemberEntity;
import com.campfiredev.growtogether.member.repository.MemberRepository;
import com.campfiredev.growtogether.point.service.PointService;
import com.campfiredev.growtogether.skill.entity.SkillEntity;
import com.campfiredev.growtogether.skill.repository.SkillRepository;
import com.campfiredev.growtogether.study.dto.post.PagedStudyDTO;
import com.campfiredev.growtogether.study.dto.post.StudyDTO;
import com.campfiredev.growtogether.study.dto.post.StudyFilter;
import com.campfiredev.growtogether.study.dto.post.StudyScheduleDto;
import com.campfiredev.growtogether.study.dto.schedule.MainScheduleDto;
import com.campfiredev.growtogether.study.entity.SkillStudy;
import com.campfiredev.growtogether.study.entity.Study;
import com.campfiredev.growtogether.study.entity.join.StudyMemberEntity;
import com.campfiredev.growtogether.study.repository.bookmark.BookmarkRepository;
import com.campfiredev.growtogether.study.repository.comment.StudyCommentRepository;
import com.campfiredev.growtogether.study.repository.join.JoinRepository;
import com.campfiredev.growtogether.study.repository.post.SkillStudyRepository;
import com.campfiredev.growtogether.study.repository.post.StudyRepository;
import com.campfiredev.growtogether.study.service.schedule.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.campfiredev.growtogether.exception.response.ErrorCode.*;
import static com.campfiredev.growtogether.study.type.StudyMemberType.LEADER;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyService {

    private final StudyRepository studyRepository;

    private final SkillRepository skillRepository;

    private final SkillStudyRepository skillStudyRepository;

    private final MemberRepository memberRepository;

    private final StudyCommentRepository studyCommentRepository;

    private final JoinRepository joinRepository;

    private final BookmarkRepository bookmarkRepository;

    private final ScheduleService scheduleService;

    private final PointService pointService;

    public StudyDTO createStudy(StudyDTO dto, long memberId) {
        List<SkillEntity> skills = validateSkillName(dto);

        Study study = Study.fromDTO(dto);

        MemberEntity member = memberRepository.findByIdWithLock(memberId).orElseThrow(() -> new CustomException(NOT_INVALID_MEMBER));

        study.setAuthor(member);

        Study savedStudy = studyRepository.save(study);

        StudyMemberEntity studyMemberEntity = StudyMemberEntity.create(study, member);
        studyMemberEntity.setStatus(LEADER);

        joinRepository.save(studyMemberEntity);

        pointService.usePoint(memberId, savedStudy.getStudyCount() * 5);

        List<MainScheduleDto> list = StudyScheduleDto.formDto(dto.getMainScheduleList());

        scheduleService.createMainSchedule(study, memberId, list);

        List<SkillStudy> skillStudies = skills.stream()
                .map(skill -> SkillStudy.builder()
                        .skill(skill)
                        .study(savedStudy)
                        .build())
                .toList();

        study.addSkillStudies(skillStudyRepository.saveAll(skillStudies));

        return StudyDTO.fromEntity(study);
    }

    @Transactional(readOnly = true)
    public PagedStudyDTO getFilteredAndSortedStudies(StudyFilter filter, Pageable pageable) {
        Page<Study> studyPage = studyRepository.findFilteredAndSortedStudies(filter, pageable);

        List<StudyDTO> studyDtoList = studyPage.getContent().stream()
                .map(this::getStudyDTO)
                .toList();

        return PagedStudyDTO.from(studyPage, studyDtoList);
    }

    public StudyDTO getStudyById(Long studyId) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));

        if (Boolean.TRUE.equals(study.getIsDeleted())) {
            throw new CustomException(ALREADY_DELETED_STUDY);
        }

        study.updateViewCount();
        return getStudyDTO(study);
    }

    public StudyDTO updateStudy(Long studyId, StudyDTO dto, Long memberId) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));

        validateMember(memberId, study);

        List<SkillEntity> newSkills = validateSkillName(dto);
        List<SkillStudy> existSkillStudies = study.getSkillStudies();

        List<SkillStudy> toRemove = existSkillStudies.stream()
                .filter(skillStudy -> !newSkills.contains(skillStudy.getSkill()))
                .toList();

        existSkillStudies.removeAll(toRemove);
        skillStudyRepository.deleteAll(toRemove);

        List<SkillStudy> newSkillStudies = newSkills.stream()
                .filter(skill -> existSkillStudies.stream().noneMatch(skillStudy -> skillStudy.getSkill().equals(skill)))
                .map(skill -> SkillStudy.builder()
                        .skill(skill)
                        .study(study)
                        .build())
                .toList();

        skillStudyRepository.saveAll(newSkillStudies);

        study.updateFromDto(dto, newSkillStudies);

        return StudyDTO.fromEntity(study);
    }

    public void deleteStudy(Long studyId, Long memberId) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDY_NOT_FOUND));

        validateMember(memberId, study);

        study.setIsDeleted(true);
        studyRepository.save(study);
    }

    private void validateMember(Long memberId, Study study) {
        if (!study.getMember().getMemberId().equals(memberId)) {
            throw new CustomException(NOT_AUTHOR);
        }
    }

    private List<SkillEntity> validateSkillName(StudyDTO dto) {
        List<SkillEntity> skills = skillRepository.findBySkillNameIn(dto.getSkillNames());

        if (dto.getSkillNames().size() != skills.size()) {
            throw new CustomException(INVALID_SKILL);
        }

        return skills;
    }

    @Transactional(readOnly = true)
    public List<StudyDTO> getPopularStudies() {
        Pageable pageable = PageRequest.of(0, 3);
        return studyRepository.findByPopularity(pageable).stream()
                .map(StudyDTO::fromEntity)
                .toList();

    }

    private StudyDTO getStudyDTO(Study study) {
        StudyDTO studyDto = StudyDTO.fromEntity(study);
        studyDto.setCommentCount(studyCommentRepository.countAllByStudyId(study.getStudyId()));
        studyDto.setLikeCount(bookmarkRepository.countAllByStudyStudyId(study.getStudyId()));
        return studyDto;
    }

    @Transactional(readOnly = true)
    public PagedStudyDTO searchStudies(String title, Pageable pageable) {
        Page<Study> studyPage = studyRepository.searchPostsByTitle(title, pageable);
        List<StudyDTO> studies = studyPage.stream()
                .map(this::getStudyDTO)
                .collect(Collectors.toList());

        return PagedStudyDTO.from(studyPage, studies);
    }

    @Transactional
    public List<StudyDTO> getMyStudies(Long memberId) {
        return studyRepository.findByMemberMemberIdAndIsDeletedFalse(memberId)
                .stream()
                .map(StudyDTO::fromEntity)
                .collect(Collectors.toList());
    }
}