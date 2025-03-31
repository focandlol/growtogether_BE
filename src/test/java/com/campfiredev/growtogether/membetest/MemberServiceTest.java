//package com.campfiredev.growtogether.membetest;
//
//import com.campfiredev.growtogether.mail.service.EmailService;
//import com.campfiredev.growtogether.member.dto.MemberDto;
//import focandlol.domain.entity.MemberEntity;
//import com.campfiredev.growtogether.member.entity.UserSkillEntity;
//import focandlol.domain.repository.MemberRepository;
//import com.campfiredev.growtogether.member.repository.UserSkillRepository;
//import com.campfiredev.growtogether.member.service.MemberService;
//import com.campfiredev.growtogether.member.service.S3Service;
//import focandlol.domain.entity.SkillEntity;
//import focandlol.domain.repository.SkillRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class MemberServiceTest {
//
//    @InjectMocks
//    private MemberService memberService;
//
//    @Mock
//    private MemberRepository memberRepository;
//
//    @Mock
//    private SkillRepository skillRepository;
//
//    @Mock
//    private UserSkillRepository userSkillRepository;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @Mock
//    private EmailService emailService;
//
//    @Mock
//    private S3Service s3Service;
//
//    @Mock
//    private MultipartFile mockFile;
//
//    @BeforeEach
//    void setUp() {
//        lenient().when(emailService.verifyCode(anyString(), anyString())).thenReturn(true);
//    }
//
//    @Test
//    @DisplayName("회원가입 성공 - 필수 입력값만 포함")
//    void register_success_with_required_fields() {
//        // Given
//        MemberDto request = new MemberDto();
//        request.setNickName("testUser");
//        request.setEmail("test@example.com");
//        request.setPhone("01012345678");
//        request.setPassword("Password123!");
//        request.setVerificationCode("123456");
//
//        doReturn(true).when(emailService).verifyCode(anyString(), anyString());
//        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
//        when(memberRepository.existsByNickName(anyString())).thenReturn(false);
//        when(memberRepository.existsByPhone(anyString())).thenReturn(false);
//        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
//
//        MemberEntity savedMember = MemberEntity.builder()
//                .userId(1L)
//                .nickName(request.getNickName())
//                .email(request.getEmail())
//                .phone(request.getPhone())
//                .password("hashedPassword")
//                .build();
//
//        when(memberRepository.save(any(MemberEntity.class))).thenReturn(savedMember);
//
//        // When
//        MemberEntity result = memberService.register(request, null);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1L, result.getMemberId());
//        assertEquals(request.getNickName(), result.getNickName());
//        assertEquals(request.getEmail(), result.getEmail());
//        assertEquals(request.getPhone(), result.getPhone());
//        assertNotEquals(request.getPassword(), result.getPassword());
//
//        verify(emailService, times(1)).verifyCode(eq(request.getEmail()), eq(request.getVerificationCode()));
//        verify(memberRepository, times(1)).save(any(MemberEntity.class));
//    }
//
//    @Test
//    @DisplayName("회원가입 성공 - 프로필 이미지 포함")
//    void register_success_with_profile_image() {
//        // Given
//        MemberDto request = new MemberDto();
//        request.setNickName("testUser");
//        request.setEmail("test@example.com");
//        request.setPhone("01012345678");
//        request.setPassword("Password123!");
//        request.setVerificationCode("123456");
//
//        doReturn(true).when(emailService).verifyCode(anyString(), anyString());
//        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
//        when(memberRepository.existsByNickName(anyString())).thenReturn(false);
//        when(memberRepository.existsByPhone(anyString())).thenReturn(false);
//        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
//        when(s3Service.uploadFile(any(MultipartFile.class))).thenReturn("s3-key");
//
//        MemberEntity savedMember = MemberEntity.builder()
//                .userId(1L)
//                .nickName(request.getNickName())
//                .email(request.getEmail())
//                .phone(request.getPhone())
//                .password("hashedPassword")
//                .profileImageUrl("s3-key")
//                .build();
//
//        when(memberRepository.save(any(MemberEntity.class))).thenReturn(savedMember);
//
//        // When
//        MemberEntity result = memberService.register(request, mockFile);
//
//        // Then
//        assertNotNull(result);
//        assertEquals("s3-key", result.getprofileImageUrl());
//
//        verify(s3Service, times(1)).uploadFile(mockFile);
//    }
//
//    @Test
//    @DisplayName("회원가입 성공 - 깃허브 URL과 기술 스택 포함")
//    void register_success_with_github_and_skills() {
//        // Given
//        MemberDto request = new MemberDto();
//        request.setNickName("testUser");
//        request.setEmail("test@example.com");
//        request.setPhone("01012345678");
//        request.setPassword("Password123!");
//        request.setVerificationCode("123456");
//        request.setGithubUrl("https://github.com/testUser");
//        request.setSkills(List.of(1L, 2L));
//
//        SkillEntity skill1 = SkillEntity.builder()
//                .skillId(1L)
//                .skillName("Java")
//                .category("Backend")
//                .skillImgUrl("java-logo")
//                .build();
//
//        SkillEntity skill2 = SkillEntity.builder()
//                .skillId(2L)
//                .skillName("Spring Boot")
//                .category("Backend")
//                .skillImgUrl("spring-logo")
//                .build();
//
//
//        doReturn(true).when(emailService).verifyCode(anyString(), anyString());
//        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
//        when(memberRepository.existsByNickName(anyString())).thenReturn(false);
//        when(memberRepository.existsByPhone(anyString())).thenReturn(false);
//        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
//        when(skillRepository.findAllById(anyList())).thenReturn(List.of(skill1, skill2));
//
//        MemberEntity savedMember = MemberEntity.builder()
//                .userId(1L)
//                .nickName(request.getNickName())
//                .email(request.getEmail())
//                .phone(request.getPhone())
//                .password("hashedPassword")
//                .githubUrl(request.getGithubUrl())
//                .build();
//
//        when(memberRepository.save(any(MemberEntity.class))).thenReturn(savedMember);
//
//        // When
//        MemberEntity result = memberService.register(request, null);
//
//        // Then
//        assertNotNull(result);
//        assertEquals("https://github.com/testUser", result.getGithubUrl());
//
//        verify(skillRepository, times(1)).findAllById(request.getSkills());
//        verify(userSkillRepository, times(2)).save(any(UserSkillEntity.class));
//    }
//}
