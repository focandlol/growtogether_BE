package focandlol.api.study.service.post;


import static focandlol.common.exception.response.ErrorCode.ALREADY_DELETED_STUDY;
import static focandlol.common.exception.response.ErrorCode.INVALID_SKILL;
import static focandlol.common.exception.response.ErrorCode.NOT_AUTHOR;
import static focandlol.common.exception.response.ErrorCode.NOT_INVALID_MEMBER;
import static focandlol.common.exception.response.ErrorCode.STUDY_NOT_FOUND;
import static focandlol.domain.type.StudyMemberType.LEADER;

import focandlol.api.point.service.PointService;
import focandlol.api.study.service.schedule.ScheduleService;
import focandlol.common.exception.custom.CustomException;
import focandlol.common.exception.response.ErrorCode;
import focandlol.domain.dto.study.post.PagedStudyDTO;
import focandlol.domain.dto.study.post.StudyDTO;
import focandlol.domain.dto.study.post.StudyFilter;
import focandlol.domain.dto.study.post.StudyScheduleDto;
import focandlol.domain.dto.study.schedule.MainScheduleDto;
import focandlol.domain.entity.MemberEntity;
import focandlol.domain.entity.SkillEntity;
import focandlol.domain.entity.SkillStudy;
import focandlol.domain.entity.Study;
import focandlol.domain.entity.join.StudyMemberEntity;
import focandlol.domain.repository.MemberRepository;
import focandlol.domain.repository.SkillRepository;
import focandlol.domain.repository.bookmark.BookmarkRepository;
import focandlol.domain.repository.comment.StudyCommentRepository;
import focandlol.domain.repository.join.JoinRepository;
import focandlol.domain.repository.post.SkillStudyRepository;
import focandlol.domain.repository.post.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;



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
                .orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));

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