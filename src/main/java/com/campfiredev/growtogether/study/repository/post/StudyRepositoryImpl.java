package com.campfiredev.growtogether.study.repository.post;

import com.campfiredev.growtogether.study.dto.post.StudyFilter;
import com.campfiredev.growtogether.study.entity.QStudy;
import com.campfiredev.growtogether.study.entity.Study;
import com.campfiredev.growtogether.study.entity.schedule.QScheduleEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class StudyRepositoryImpl implements StudyRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Study> findFilteredAndSortedStudies(StudyFilter filter, Pageable pageable) {
        QStudy study = QStudy.study;
        QScheduleEntity schedule = QScheduleEntity.scheduleEntity;

        BooleanBuilder whereClause = new BooleanBuilder();

        // 필터링 조건 추가
        if (filter.getStudyType() != null) {
            whereClause.and(study.type.eq(filter.getStudyType())); // 스터디 타입 필터링
        }
        if (filter.getTechnologyStacks() != null && !filter.getTechnologyStacks().isEmpty()) {
            whereClause.and(study.skillStudies.any().skill.skillName.in(filter.getTechnologyStacks())); // 기술 스택 필터링
        }
        if (filter.getDate() != null) {
            List<Long> studyIdsWithDate = queryFactory.select(schedule.study.studyId)
                    .from(schedule)
                    .where(schedule.start.between(
                            filter.getDate().atStartOfDay(),
                            filter.getDate().atTime(23, 59, 59)
                    ))
                    .fetch();

            whereClause.and(study.studyId.in(studyIdsWithDate)); // 특정 날짜 필터링
        }

        // 정렬 조건 추가
        JPAQuery<Study> query = queryFactory.selectFrom(study)
                .where(whereClause)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        if (filter.getSortBy() != null) {
            switch (filter.getSortBy()) {
                case CREATED_AT -> query.orderBy(study.createdAt.desc()); // 최신순
                case VIEW_COUNT -> query.orderBy(study.viewCount.desc()); // 조회순
                case DEADLINE -> query.orderBy(study.studyClosingDate.asc()); // 모집 마감 임박순
            }
        }

        List<Study> studies = query.distinct().fetch();
        long total = queryFactory.select(study.count()).from(study).where(whereClause).fetchOne();

        return new PageImpl<>(studies, pageable, total);
    }

    @Override
    public Page<Study> searchPostsByTitle(String title, Pageable pageable) {
        QStudy study = QStudy.study;

        BooleanBuilder whereClause = new BooleanBuilder();

        // 제목 검색 조건
        if (title != null && !title.isEmpty()) {
            whereClause.and(study.title.containsIgnoreCase(title));
        }

        // QueryDSL 쿼리 생성
        JPAQuery<Study> query = queryFactory.selectFrom(study)
                .where(whereClause)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(study.createdAt.desc()); // 기본 정렬: 최신순

        List<Study> studyPosts = query.fetch();
        long total = queryFactory.select(study.count()).from(study).where(whereClause).fetchOne();

        return new PageImpl<>(studyPosts, pageable, total);
    }
}
