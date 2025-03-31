package focandlol.domain.dto.payment;

// CamelCase가 기본이지만 kakao 측에서 해당 변수 case를 사용
public record ApproveResponse(

        String aid,
        String tid,
        String cid,
        String partner_order_id,
        String partner_user_id,
        String payment_method_type,
        Amount amount,
        String item_name,
        String item_code,
        int quantity,
        String created_at,
        String approved_at,
        String payload

) {
}
