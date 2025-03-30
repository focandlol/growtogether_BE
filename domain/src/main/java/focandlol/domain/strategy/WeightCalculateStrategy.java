package focandlol.domain.strategy;

import com.querydsl.core.types.dsl.NumberExpression;
import focandlol.domain.entity.QBootCampReview;

public interface WeightCalculateStrategy {

    NumberExpression<Double> calculateWeightExpression(QBootCampReview review);
}


