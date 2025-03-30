package focandlol.api.bootcamp.service;

import focandlol.api.notification.service.NotificationService;
import focandlol.common.auth.CustomUserDetails;
import focandlol.common.exception.custom.CustomException;
import focandlol.common.exception.response.ErrorCode;
import focandlol.domain.dto.bootcamp.CommentRequest;
import focandlol.domain.dto.bootcamp.CommentResponseDto;
import focandlol.domain.entity.BootCampComment;
import focandlol.domain.entity.BootCampReview;
import focandlol.domain.entity.MemberEntity;
import focandlol.domain.repository.BootCampCommentRepository;
import focandlol.domain.repository.BootCampReviewRepository;
import focandlol.domain.repository.MemberRepository;
import focandlol.domain.type.NotiType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BootCampCommentService {

    private final BootCampCommentRepository bootCampCommentRepository;
    private final BootCampReviewRepository bootCampReviewRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    @Transactional
    public void addComment(CommentRequest request , CustomUserDetails customUserDetails){

        MemberEntity member = memberRepository.findByEmail(customUserDetails.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        BootCampReview review = bootCampReviewRepository.findById(request.getBootCampId())
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        BootCampComment parentComment = null;
        int depth = 0;

        if(request.getParentCommentId() != null){
            parentComment = bootCampCommentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

            depth = parentComment.getDepth() + 1;

            if(depth > 2) throw new CustomException(ErrorCode.COMMENT_DEPTH_EXCEED);
        }


        BootCampComment comment = BootCampComment.builder()
                .commentContent(request.getContent())
                .bootCampReview(review)
                .member(member)
                .parentComment(parentComment)
                .isDeleted(false)
                .depth(depth)
                .build();

        bootCampCommentRepository.save(comment);

        String reviewUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/bootcamp/comments/{id}").buildAndExpand(review.getBootCampId()).toUriString();
        // 추후에 수정 예정
        if(parentComment == null){
            notificationService.sendNotification(review.getMember(),"[부트캠프]"+review.getTitle() +" 새로운 답글이 달렸습니다.", reviewUrl , NotiType.BOOTCAMP);
        }

        if(parentComment != null){
            notificationService.sendNotification(parentComment.getMember(),"[부트캠프]"+review.getTitle() + " 댓글에 새로운 답글이 달렸습니다.", reviewUrl , NotiType.BOOTCAMP);
        }
    }

    @Transactional
    public void updateComment(Long commentId , String newContent, CustomUserDetails customUserDetails){

        BootCampComment comment = bootCampCommentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        MemberEntity member = memberRepository.findByEmail(customUserDetails.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if(!comment.getMember().equals(member)){
            throw new CustomException(ErrorCode.NOT_AUTHOR);
        }

        boolean isDelete = bootCampCommentRepository.existsByBootCampCommentIdAndIsDeletedTrue(comment.getBootCampCommentId());

        if(isDelete){
            throw new CustomException(ErrorCode.COMMENT_ACCESS_DENIED);
        }

        comment.setCommentContent(newContent);

        bootCampCommentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, CustomUserDetails customUserDetails){

        BootCampComment comment = bootCampCommentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        MemberEntity member = memberRepository.findByEmail(customUserDetails.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if(!comment.getMember().equals(member)){
            throw new CustomException(ErrorCode.NOT_AUTHOR);
        }

        comment.setCommentContent("작성자에 의해 삭제된 댓글입니다.");
        comment.setIsDeleted(true);
        bootCampCommentRepository.save(comment);
    }
/*
    public Page<BootCampComment> getComments(Long reviewId, Pageable pageable) {
        bootCampReviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        List<BootCampComment> comments = bootCampCommentRepository.findCommentsWithChildrenByBootCampId(reviewId);
        return new PageImpl<>(comments, pageable, comments.size());
    }
*/
    //무한 스크롤
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getComments(Long bootCampId , Long lastIdx, Long size) {

        bootCampReviewRepository.findById(bootCampId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        Pageable pageable = PageRequest.of(0, size.intValue());

        // 부모 댓글을 Page로 가져오기
        Page<BootCampComment> parentCommentsPage = (lastIdx == 0) ?
                bootCampCommentRepository.findParentComments(bootCampId, pageable) :
                bootCampCommentRepository.findParentCommentsWithLastIdx(bootCampId, lastIdx, pageable);

        List<BootCampComment> parentComments = parentCommentsPage.getContent();

        // 부모 댓글 ID 리스트 추출
        List<Long> parentIds = parentComments.stream()
                .map(BootCampComment::getBootCampCommentId)
                .collect(Collectors.toList());

        // 자식 댓글을 한 번의 쿼리로 가져옴
        List<BootCampComment> childComments = parentIds.isEmpty() ?
                Collections.emptyList() : bootCampCommentRepository.findChildCommentsByParentIds(parentIds);

        // 자식 댓글을 부모 댓글에 매핑
        Map<Long, List<BootCampComment>> childCommentMap = childComments.stream()
                .collect(Collectors.groupingBy(c -> c.getParentComment().getBootCampCommentId()));

        parentComments.forEach(parent ->
                parent.setChildComments(childCommentMap.getOrDefault(parent.getBootCampCommentId(), new ArrayList<>()))
        );

        return parentComments.stream()
                .map(CommentResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}
