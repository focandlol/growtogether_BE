package focandlol.domain.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MemberRegisterDto {

    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickName;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    // 이메일 인증 코드
    @NotBlank(message = "이메일 인증 코드는 필수입니다.")
    private String verificationCode;

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "전화번호는 10~11자리 숫자만 입력 가능합니다.")
    private String phone;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하이어야 합니다.")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]:\";'<>?,./]).{8,20}$",
            message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다."
    )
    private String password;

    // 선택 입력 사항 (null 허용)
    private String githubUrl;
    private String profileImageUrl;
    private List<String> skills;    // 선택한 기술 스택 ID 리스트


}
