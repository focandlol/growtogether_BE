package focandlol.api.study.service.comment;


import static focandlol.common.exception.response.ErrorCode.COMMENT_NOT_FOUND;
import static focandlol.common.exception.response.ErrorCode.NOT_AUTHOR;
import static focandlol.common.exception.response.ErrorCode.STUDY_NOT_FOUND;
import static focandlol.common.exception.response.ErrorCode.USER_NOT_FOUND;
import static focandlol.domain.type.NotiType.STUDY;

import focandlol.api.notification.service.NotificationService;
import focandlol.common.exception.custom.CustomException;
import focandlol.domain.dto.study.comment.StudyCommentDto;
import focandlol.domain.entity.MemberEntity;
import focandlol.domain.entity.Study;
import focandlol.domain.entity.StudyComment;
import focandlol.domain.repository.MemberRepository;
import focandlol.domain.repository.comment.StudyCommentRepository;
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
@Transactional
@RequiredArgsConstructor
public class StudyCommentService {
    private final String deletedCommentMessage = "작성자에 의해 삭제된 댓글입니다.";

    private final StudyCommentRepository studyCommentRepository;
    private final MemberRepository memberRepository;
    private final StudyRepository studyRepository;
    private final NotificationService notificationService;

    public void createComment(StudyCommentDto dto, Long memberId) {
        MemberEntity member = memberRepository.findById(memberId).orElseThrow(
                () -> new CustomException(USER_NOT_FOUND)
        );

        StudyComment comment = StudyComment.builder()
                .commentContent(dto.getCommentContent())
                .parentCommentId(dto.getParentCommentId())
                .studyId(dto.getStudyId())
                .member(member)
                .build();

        studyCommentRepository.save(comment);

        sendNotiComment(comment);
    }

    public List<StudyCommentDto> getCommentsByStudyId(Long studyId, Long lastIdx, Long size) {
        Pageable pageable = PageRequest.of(0, size.intValue());

        Page<StudyComment> studyCommentList;

        if (0 == lastIdx) {
            studyCommentList = studyCommentRepository.findByStudyId(studyId, pageable);
        } else {
            studyCommentList = studyCommentRepository.findByStudyIdAndIdLessThan(studyId, lastIdx, pageable);
        }

        return setChildComment(studyCommentList);
    }

    public List<StudyCommentDto> setChildComment(Page<StudyComment> studyCommentList) {
        return studyCommentList.stream()
                .map(comment ->
                        {
                            StudyCommentDto studyCommentDto = StudyCommentDto.fromEntity(comment);
                            List<StudyComment> childCommentList = studyCommentRepository.findByParentCommentIdOrderByCreatedAtDesc(comment.getStudyCommentId());
                            studyCommentDto.setChildComments(childCommentList.stream().map(StudyCommentDto::fromEntity).collect(Collectors.toList()));
                            return studyCommentDto;
                        }
                )
                .collect(Collectors.toList());
    }

    public void updateComment(Long commentId, StudyCommentDto dto, Long memberId) {
        StudyComment comment = studyCommentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(COMMENT_NOT_FOUND));

        validateMember(memberId, comment);

        comment.setCommentContent(dto.getCommentContent());
        studyCommentRepository.save(comment);
    }

    public void deleteComment(Long commentId, Long memberId) {
        StudyComment comment = studyCommentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(COMMENT_NOT_FOUND));

        validateMember(memberId, comment);

        comment.setCommentContent(deletedCommentMessage);
    }

    private void validateMember(Long memberId, StudyComment comment) {
        if (!comment.getMember().getMemberId().equals(memberId)) {
            throw new CustomException(NOT_AUTHOR);
        }
    }
    private void sendNotiComment(StudyComment comment) {
        if(0 == comment.getParentCommentId()){
            Study study = studyRepository.findByStudyIdAndIsDeletedFalse(comment.getStudyId()).orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));
            notificationService.sendNotification(study.getMember(),comment.getMember().getNickName()+"님이 새로운 댓글을 작성했습니다.",null, STUDY);
        }else{
            StudyComment parentComment = studyCommentRepository.findById(comment.getParentCommentId()).orElseThrow(()-> new CustomException(COMMENT_NOT_FOUND));
            notificationService.sendNotification(parentComment.getMember(),comment.getMember().getNickName()+"님이 새로운 대댓글을 작성했습니다.",null, STUDY);
        }
    }
}
