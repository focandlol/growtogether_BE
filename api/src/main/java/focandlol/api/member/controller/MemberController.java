package focandlol.api.member.controller;

import focandlol.domain.dto.member.MemberLoginDto;
import focandlol.domain.dto.member.MemberRegisterDto;
import focandlol.domain.dto.member.MemberResponseDto;
import focandlol.api.member.service.MemberService;
import focandlol.domain.entity.MemberEntity;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 회원가입 API
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody MemberRegisterDto memberRegisterDto,
            @RequestParam(required = false) MultipartFile profileImage) {
        MemberEntity member = memberService.register(memberRegisterDto, profileImage);
        return ResponseEntity.ok(Map.of(
                "message", "회원가입 완료",
                "user", new MemberResponseDto(member)
        ));
    }


    // 회원 로그인 api
    @PostMapping("/memberLogin")
    public ResponseEntity<?> userLogin(@RequestBody MemberLoginDto memberLoginDto) {
        String accessToken = memberService.userLogin(memberLoginDto);
        return ResponseEntity.ok(Map.of("message", "로그인이 완료되었습니다.", "accessToken", accessToken));
    }
    // 비밀번호 재설정 요청 (이메일 전송)
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        memberService.sendPasswordResetEmail(email);
        return ResponseEntity.ok(Map.of("message", "비밀번호 재설정 이메일이 전송되었습니다."));
    }
    // 비밀번호 재설정 처리
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        memberService.resetPassword(token, newPassword);
        return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
    }
    // 이메일 찾기 API (사용자 요청 시 마스킹된 이메일 반환)
    @PostMapping("/find-email")
    public ResponseEntity<?> findEmail(@RequestParam String phone) {
        String maskedEmail = memberService.findEmailByPhone(phone);
        return ResponseEntity.ok(Map.of("maskedEmail", maskedEmail));
    }


    @Operation(summary = "휴대폰 번호로 이메일 찾기",
            description = "입력한 휴대폰 번호로 등록된 계정을 조회해 이메일을 마스킹 형태로 반환합니다.")
    @GetMapping("/find-email")
    public ResponseEntity<String> findEmailByPhone(@RequestParam String phone) {
        String maskedEmail = memberService.findEmailByPhone(phone);
        return ResponseEntity.ok(maskedEmail);
    }



}
