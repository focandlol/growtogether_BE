package focandlol.domain.strategy;

import com.querydsl.core.types.dsl.NumberExpression;
import focandlol.domain.entity.QBootCampReview;
import org.springframework.stereotype.Component;

@Component("WeightStrategy")
public class WeightStrategy implements WeightCalculateStrategy{

    @Override
    public NumberExpression<Double> calculateWeightExpression(QBootCampReview review) {

        return review.likeCount.multiply(5).castToNum(Double.class)
                .add(review.viewCount.multiply(1)).castToNum(Double.class)
                .add(review.comments.size().multiply(3).castToNum(Double.class));
    }

}
