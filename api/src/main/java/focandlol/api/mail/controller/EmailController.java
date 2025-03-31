package focandlol.api.mail.controller;


import focandlol.api.mail.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.http.RequestEntity.post;

@RestController
@RequestMapping("api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    // 이메일 인증 코드 요청
    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendVerificationCode(@RequestParam String email) {
        try {
            emailService.sendVerificationEmail(email);
            return ResponseEntity.ok(Map.of("message", "이메일 인증 코드가 전송되었습니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 인증 코드 확인 요청
    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyCode(@RequestParam String email, @RequestParam String code) {
        boolean isValid = emailService.verifyCode(email, code);
        if (isValid) {
            return ResponseEntity.ok(Map.of("message", "이메일 인증이 완료되었습니다."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "인증 코드가 올바르지 않거나 만료되었습니다."));
        }
    }

}
