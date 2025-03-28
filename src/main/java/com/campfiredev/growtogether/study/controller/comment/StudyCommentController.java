package com.campfiredev.growtogether.study.controller.comment;

import com.campfiredev.growtogether.member.dto.CustomUserDetails;
import com.campfiredev.growtogether.study.dto.comment.StudyCommentDto;
import com.campfiredev.growtogether.study.service.comment.StudyCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study/comments")
public class StudyCommentController {

    private final StudyCommentService studyCommentService;

    @PostMapping
    public void createComment(@Valid @RequestBody StudyCommentDto dto, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        studyCommentService.createComment(dto,customUserDetails.getMemberId());
    }

    @GetMapping("/{studyId}")
    public List<StudyCommentDto> getComments(@PathVariable Long studyId,
                                             @RequestParam(defaultValue = "0") Long lastIdx,
                                             @RequestParam(defaultValue = "10") Long size) {
        return studyCommentService.getCommentsByStudyId(studyId,lastIdx,size);
    }

    @PutMapping("/{commentId}")
    public void updateComment(@PathVariable Long commentId, @Valid @RequestBody StudyCommentDto dto, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        studyCommentService.updateComment(commentId, dto, customUserDetails.getMemberId());
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long commentId,@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        studyCommentService.deleteComment(commentId,customUserDetails.getMemberId());
    }
}
