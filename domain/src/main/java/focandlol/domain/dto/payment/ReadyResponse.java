package focandlol.domain.dto.payment;

// CamelCase가 기본이지만 kakao 측에서 해당 변수 case를 사용
public record ReadyResponse(

        String tid,                  // 결제 고유번호
        String next_redirect_pc_url  // 카카오톡으로 결제 요청 메시지(TMS)를 보내기 위한 사용자 정보 입력화면 Redirect URL (카카오 측 제공)

) {
}
