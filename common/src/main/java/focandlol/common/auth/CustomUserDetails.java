package focandlol.common.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomUserDetails  {

    private String password;
    private Long memberId;
    private String email;
    private String nickName;
}
