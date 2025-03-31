package focandlol.api.bootcamp.service;

import focandlol.api.member.service.S3Service;
import focandlol.common.auth.CustomUserDetails;
import focandlol.common.exception.custom.CustomException;
import focandlol.common.exception.response.ErrorCode;
import focandlol.domain.dto.bootcamp.BootCampReviewCreateDto;
import focandlol.domain.dto.bootcamp.BootCampReviewResponseDto;
import focandlol.domain.dto.bootcamp.BootCampReviewSearchRequest;
import focandlol.domain.dto.bootcamp.BootCampReviewUpdateDto;
import focandlol.domain.entity.BootCampReview;
import focandlol.domain.entity.BootCampSkill;
import focandlol.domain.entity.MemberEntity;
import focandlol.domain.entity.ReviewLike;
import focandlol.domain.entity.SkillEntity;
import focandlol.domain.repository.BootCampReviewRepository;
import focandlol.domain.repository.BootCampReviewRepositoryCustom;
import focandlol.domain.repository.BootCampSkillRepository;
import focandlol.domain.repository.MemberRepository;
import focandlol.domain.repository.ReviewLikeRepository;
import focandlol.domain.repository.SkillRepository;
import focandlol.domain.strategy.WeightCalculateStrategy;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BootCampReviewService {

    private final BootCampReviewRepository bootCampReviewRepository;
    private final BootCampSkillRepository bootCampSkillRepository;
    private final BootCampReviewRepositoryCustom bootCampReviewRepositoryCustom;
    private final MemberRepository memberRepository;
    private final SkillRepository skillRepository;
    private final S3Service s3Service;

    private static final String CREATED_AT = "createdAt";
    private static final String LIKE_COUNT = "likeCount";
    private static final String HOT = "hot";
    private static final String NEW = "new";
    private final ReviewLikeRepository reviewLikeRepository;

    private final List<WeightCalculateStrategy> strategies;
    private Map<String , WeightCalculateStrategy> strategyMap;

    @PostConstruct
    private void initializeStrategyMap() {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(strategy -> strategy.getClass().getSimpleName(), strategy -> strategy));
    }

    //후기 등록
    @Transactional
    public BootCampReviewCreateDto createReview(BootCampReviewCreateDto request , MultipartFile imageKey, CustomUserDetails customUserDetails) {

        MemberEntity member = memberRepository.findByEmail(customUserDetails.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        BootCampReview review = request.toEntity(member);

        if (imageKey != null && !imageKey.isEmpty()) {
            String imageUrl = s3Service.uploadFile(imageKey);
            review.setImageUrl(imageUrl);
        }

        bootCampReviewRepository.save(review);

        if(request.getSkillNames() != null && !request.getSkillNames().isEmpty()){
            List<SkillEntity> skills = skillRepository.findBySkillNameIn(request.getSkillNames());

            List<BootCampSkill> bootCampSkills = skills.stream()
                    .map(skill -> BootCampSkill.builder()
                            .bootCampReview(review)
                            .skill(skill)
                            .build())
                    .collect(Collectors.toList());
            review.setBootCampSkills(bootCampSkills);
            bootCampSkillRepository.saveAll(bootCampSkills);
        }
        return BootCampReviewCreateDto.fromEntity(review);
    }

    //후기 수정
    @Transactional
    public BootCampReviewUpdateDto updateReview(Long bootCampId, BootCampReviewUpdateDto request ,MultipartFile imageKey, CustomUserDetails customUserDetails) {

        BootCampReview review = bootCampReviewRepository.findById(bootCampId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        MemberEntity member = memberRepository.findByEmail(customUserDetails.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if(!review.getMember().equals(member)){
            throw new CustomException(ErrorCode.NOT_AUTHOR);
        }

        if (imageKey != null && !imageKey.isEmpty()) {
            if (review.getImageUrl() != null) {
                s3Service.deleteFile(review.getImageUrl());
            }
            String imageUrl = s3Service.uploadFile(imageKey);
            review.setImageUrl(imageUrl);
        }

        request.updateEntity(review);
        bootCampReviewRepository.save(review);

        bootCampSkillRepository.deleteByBootCampReview(review);
        bootCampSkillRepository.flush();

        if(request.getSkillNames() != null && !request.getSkillNames().isEmpty()){
            List<SkillEntity> skills = skillRepository.findBySkillNameIn(request.getSkillNames());

            List<BootCampSkill> bootCampSkills = skills.stream()
                    .map(skill -> BootCampSkill.builder()
                            .bootCampReview(review)
                            .skill(skill)
                            .build())
                    .collect(Collectors.toList());

            bootCampSkillRepository.saveAll(bootCampSkills);
        }

        return BootCampReviewUpdateDto.fromEntity(review);
    }

    //후기 삭제
    @Transactional
    public void deleteReview(Long bootCampReviewId){
        BootCampReview review =  bootCampReviewRepository.findById(bootCampReviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        bootCampSkillRepository.deleteByBootCampReview(review);

        bootCampReviewRepository.delete(review);
    }

    //후기 조회
    @Transactional
    public BootCampReviewResponseDto.PageResponse getBootCampReviews(int page, String sortType){

        int pageIndex = Math.max(page - 1, 0);
        Pageable pageable;

        // 정렬 방식 설정 (기본값은 최신순)
        if (NEW.equalsIgnoreCase(sortType)) {
            pageable = PageRequest.of(pageIndex, 9, Sort.by(Sort.Order.desc(CREATED_AT))); // 최신순 정렬
        } else if (HOT.equalsIgnoreCase(sortType)) {
            pageable = PageRequest.of(pageIndex, 9, Sort.by(Sort.Order.desc(LIKE_COUNT))); // 좋아요 순 정렬
        } else {
            pageable = PageRequest.of(pageIndex, 9, Sort.by(Sort.Order.desc(CREATED_AT))); // 기본값 최신순
        }

        // 1. BootCampReview의 ID만 페이징하여 가져오기 (최신순 or 좋아요순)
        Page<Long> reviewIdsPage = bootCampReviewRepository.findBootCampReviewIdsBySortType(sortType, pageable);
        List<Long> reviewIds = reviewIdsPage.getContent();

        if (reviewIds.isEmpty()) {
            return new BootCampReviewResponseDto.PageResponse(Collections.emptyList(), reviewIdsPage.getTotalPages(), page, reviewIdsPage.getTotalElements(), pageable.getPageSize());
        }

        // 2. Fetch Join을 사용하여 ID 기반으로 BootCampReview 조회
        List<BootCampReview> reviews = bootCampReviewRepository.findAllByIdsWithDetails(reviewIds);

        // 3. ID 조회 시의 순서를 유지하기 위해 정렬 재적용
        Map<Long, Integer> orderMap = new HashMap<>();
        for (int i = 0; i < reviewIds.size(); i++) {
            orderMap.put(reviewIds.get(i), i);
        }
        reviews.sort(Comparator.comparingInt(r -> orderMap.get(r.getBootCampId())));

        Map<Long,Integer> commentCountMap = getCommentCounts(reviews);


        List<BootCampReviewResponseDto.Response> reviewDtos = reviews.stream()
                .map(review -> {
                    BootCampReviewResponseDto.Response responseDto = BootCampReviewResponseDto.Response.fromEntity(review);
                    responseDto.setCommentCount(commentCountMap.getOrDefault(review.getBootCampId(), 0));
                    return responseDto;
                }).toList();


        return new BootCampReviewResponseDto.PageResponse(
                reviewDtos , reviewIdsPage.getTotalPages(),page,reviewIdsPage.getTotalElements(), pageable.getPageSize()
        );
    }

    //후기 상세 조회
    @Transactional
    public BootCampReviewResponseDto.Response getBootCampReviewDetail(Long bootCampId){


        BootCampReview bootCampReview = bootCampReviewRepository.findByIdWithSkills(bootCampId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        bootCampReview.increaseViewCount();

        return BootCampReviewResponseDto.Response.fromEntity(bootCampReview);
    }

    //게시글 좋아요
    @Transactional
    public void toggleLike(Long reviewId , CustomUserDetails customUserDetails) {

        MemberEntity member = memberRepository.findByEmail(customUserDetails.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        BootCampReview review = bootCampReviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        boolean isAlerady = reviewLikeRepository.existsByBootCampReviewAndMember(review,member);

        if(isAlerady){
            reviewLikeRepository.deleteByBootCampReviewAndMember(review,member);
            review.decreaseLikeCount();
        } else {
            ReviewLike like = ReviewLike.builder()
                                        .bootCampReview(review)
                                        .member(member)
                                        .build();

            reviewLikeRepository.save(like);
            review.increaseLikeCount();
        }

        bootCampReviewRepository.save(review);
    }

    //검색 기능
    public List<BootCampReviewResponseDto.Response> searchBootCamps(BootCampReviewSearchRequest request){

        Sort sort;

        if(NEW.equalsIgnoreCase(request.getSortType())){
            sort = Sort.by(Sort.Order.desc(CREATED_AT));
        } else {
            sort = Sort.by(Sort.Order.desc(HOT));
        }

      //  Pageable pageable = PageRequest.of(request.getPage(),request.getSize(),sort);

        List<BootCampReview> bootCampReviews = bootCampReviewRepositoryCustom.searchBootCamps(
                request.getBootCampName(),
                request.getTitle(),
                request.getProgramCourse(),
                request.getSkillName(),
                sort
        );

        return bootCampReviews.stream()
                .map(BootCampReviewResponseDto.Response::fromEntity)
                .collect(Collectors.toList());
    }
  //  @Transactional(readOnly = true)
    public List<BootCampReviewResponseDto.Response> getTopBootCampReviews(String strategyType , int limit){
        WeightCalculateStrategy strategy = strategyMap.getOrDefault(strategyType,strategyMap.get("WeightStrategy"));

        List<BootCampReview> topReviews = bootCampReviewRepositoryCustom.findTopRankedReviews(strategy,limit);

        return topReviews.stream()
                .map(review -> {
                    BootCampReviewResponseDto.Response reviewDto = BootCampReviewResponseDto.Response.fromEntity(review);
                    reviewDto.setCommentCount(review.getComments().size());
                    return reviewDto;
                })
                .collect(Collectors.toList());
    }

    public BootCampReviewResponseDto.PageResponse getLikeReviews(CustomUserDetails customUserDetails, Pageable pageable) {

        MemberEntity member = memberRepository.findByEmail(customUserDetails.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Page<ReviewLike> reviewLikes = reviewLikeRepository.findByMember(member, pageable);
        Page<BootCampReview> bootCampReviews = reviewLikes.map(ReviewLike::getBootCampReview);

        return BootCampReviewResponseDto.PageResponse.fromEntityPage(bootCampReviews);
    }


    public List<String> getProgramCourse(){

        return skillRepository.findDistinctCategories();
    }

    public List<String> getSkillName(){

        return skillRepository.findDistinctSkillName();
    }

    private Map<Long,Integer> getCommentCounts(List<BootCampReview> reviews){
        List<Long> bootCampIds = reviews.stream().map(BootCampReview::getBootCampId)
                .toList();

        return bootCampReviewRepository.findCommentCountsByBootCampIds(bootCampIds)
                .stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> ((Number) result[1]).intValue()
                ));
    }
}