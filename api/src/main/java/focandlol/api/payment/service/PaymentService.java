package focandlol.api.payment.service;

import focandlol.api.member.service.MemberService;
import focandlol.domain.dto.payment.ApproveResponse;
import focandlol.domain.dto.payment.PaymentApprove;
import focandlol.domain.dto.payment.PaymentOrder;
import focandlol.domain.dto.payment.ReadyResponse;
import focandlol.api.point.service.PointService;
import focandlol.common.config.PayConfig;
import focandlol.domain.entity.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RestTemplate restTemplate;
    private final PayConfig payConfig;

    private final MemberService memberService;
    private final PointService pointService;

    // 카카오페이 요청 시 필요한 헤더 값
    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "SECRET_KEY " + payConfig.secretKey());
        headers.set("Content-type", "application/json");
        return headers;
    }

    // 카카오페이 결제창 연결
    public ReadyResponse payReady(PaymentOrder paymentOrder) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("cid", payConfig.clientId());                                    // 가맹점 코드(테스트용)
        parameters.put("partner_order_id", "1234567890");                               // 주문번호
        parameters.put("partner_user_id", String.valueOf(paymentOrder.id()));           // 회원 아이디
        parameters.put("item_name", paymentOrder.name());                               // 상품명
        parameters.put("quantity", "1");                                                // 상품 수량
        parameters.put("total_amount", String.valueOf(paymentOrder.totalPrice()));      // 상품 총액
        parameters.put("tax_free_amount", "0");                                         // 상품 비과세 금액
        parameters.put("approval_url", "http://localhost:5173/payment/approve");        // 결제 성공 시 URL
        parameters.put("cancel_url", "http://localhost:8080/payment/cancel");           // 결제 취소 시 URL
        parameters.put("fail_url", "http://localhost:5173/mypage");              // 결제 실패 시 URL

        return restTemplate.postForEntity(
                payConfig.readyUri(),
                new HttpEntity<>(parameters, getHeaders()),
                ReadyResponse.class).getBody();
    }
    // 카카오페이 결제 승인
    // 사용자가 결제 수단을 선택하고 비밀번호를 입력해 결제 인증을 완료한 뒤, 최종적으로 결제 완료 처리를 하는 단계
    @Transactional
    public ApproveResponse payApprove(PaymentApprove paymentApprove) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("cid", payConfig.clientId());                                    // 가맹점 코드(테스트용)
        parameters.put("tid", paymentApprove.tid());                                    // 결제 고유번호
        parameters.put("partner_order_id", "1234567890");                               // 주문번호
        parameters.put("partner_user_id", String.valueOf(paymentApprove.id()));         // 회원 아이디
        parameters.put("pg_token", paymentApprove.pgToken());                           // 결제승인 요청을 인증하는 토큰

        ApproveResponse approveResponse = restTemplate.postForObject(
                payConfig.approveUri(),
                new HttpEntity<>(parameters, getHeaders()),
                ApproveResponse.class);

        Long memberId = Long.valueOf(Objects.requireNonNull(approveResponse).partner_user_id());
        MemberEntity memberEntity = memberService.findById(memberId);
        pointService.updatePoint(memberEntity, approveResponse.amount().total());

        return approveResponse;
    }

}