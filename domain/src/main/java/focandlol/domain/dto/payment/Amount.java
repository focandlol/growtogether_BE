package focandlol.domain.dto.payment;

public record Amount(

        Integer total,
        Integer tax_free,
        Integer vat,
        Integer point,
        Integer discount,
        Integer green_deposit

) {
}
