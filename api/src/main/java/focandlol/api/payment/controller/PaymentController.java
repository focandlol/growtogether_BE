package focandlol.api.payment.controller;

import focandlol.domain.dto.payment.PaymentApprove;
import focandlol.domain.dto.payment.PaymentOrder;
import focandlol.api.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/ready/callback")
    public ResponseEntity<?> payReady(@RequestBody PaymentOrder paymentOrder) {
        return ResponseEntity.ok(paymentService.payReady(paymentOrder));
    }
    // 해당 endpoint 프론트에서 구현해야 됨, ★ 카카오 디벨로퍼 uri 변경 필수
    @GetMapping("/approve/callback")
    public ResponseEntity<?> payApprove(@RequestParam("pg_token") String pgToken) {
        return ResponseEntity.ok(pgToken);
    }

    @PostMapping("/approve2")
    public ResponseEntity<?> payApproveKakao(@RequestBody PaymentApprove paymentApprove) {
        return ResponseEntity.ok(paymentService.payApprove(paymentApprove));
    }

}