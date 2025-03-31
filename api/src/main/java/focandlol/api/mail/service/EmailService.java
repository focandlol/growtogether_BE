package focandlol.api.mail.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final Map<String, String> m = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;
    private static final String EMAIL_PREFIX = "EMAIL_VERIFICATION:";
    private static final long EXPIRATION_TIME = 5; // 인증번호 유효시간 : 5분

    // 인증번호 랜덤 생성
    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    // 인증번호 이메일 전송
    public void sendVerificationEmail(String toEmail) {
        String code = generateVerificationCode();
        String subject = "GrowTogether 이메일 인증 코드";
        String content = "<h3>이메일 인증 코드:</h3><h1>" + code + "</h1>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);

            // Redis에 저장 (5분 동안 유효)
            redisTemplate.opsForValue().set(EMAIL_PREFIX + toEmail, code, EXPIRATION_TIME, TimeUnit.MINUTES);
//			m.put(toEmail, code);
            logger.info("이메일 인증 코드 [{}] 가 {} 에게 전송되었습니다.", code, toEmail);
        } catch (MessagingException e) {
            logger.error("이메일 전송 실패: {}", e.getMessage());
            throw new RuntimeException("이메일 전송에 실패했습니다. 다시 시도해주세요.");
        }
    }

    // 인증번호 검증
    public boolean verifyCode(String email, String code) {
        String storedCode = redisTemplate.opsForValue().get(EMAIL_PREFIX + email);
//		String storedCode = m.get(email);
//		if (storedCode != null && storedCode.equals(code)) {
////              redisTemplate.delete(EMAIL_PREFIX + email); // 인증 성공 후 코드 삭제
////			m.remove(email);
//			return true;
//		}
//		return false;

        return storedCode != null && storedCode.equals(code);
    }
    public void sendPasswordResetEmail(String toEmail, String resetUrl) {
        String subject = "GrowTogether 비밀번호 재설정 안내";
        String content = "<h3>비밀번호 재설정 링크:</h3>" +
                "<p>안녕하세요. 이 메일은 귀하의 이요예정 비밀번호 요청에 의해 발송되었습습니다.<p>"+
                "<p>아래 링크를 클릭하여 새로운 비밀번호를 설정하세요.</p>" +
                "<a href='" + resetUrl + "'>" + resetUrl + "</a>" +
                "<p>해당 링크는 5분 후 만료됩니다.</p>"+
                "<p></p>"+
                "<p>감사합니다.</p>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송에 실패했습니다.");
        }
    }
}
