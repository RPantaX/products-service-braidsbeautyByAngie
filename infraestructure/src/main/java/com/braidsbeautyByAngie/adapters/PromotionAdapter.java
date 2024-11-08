package com.braidsbeautyByAngie.adapters;

import com.braidsbeautyByAngie.aggregates.dto.PromotionDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestPromotion;
import com.braidsbeautyByAngie.aggregates.response.promotions.ResponseListPageablePromotion;
import com.braidsbeautyByAngie.aggregates.response.promotions.ResponsePromotion;
import com.braidsbeautyByAngie.ports.out.PromotionServiceOut;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PromotionAdapter implements PromotionServiceOut {
    @Override
    public PromotionDTO createPromotionOut(RequestPromotion requestPromotion) {
        return null;
    }

    @Override
    public Optional<ResponsePromotion> findPromotionByIdOut(Long promotionId) {
        return Optional.empty();
    }

    @Override
    public PromotionDTO updatePromotionOut(Long promotionId, PromotionDTO promotion) {
        return null;
    }

    @Override
    public PromotionDTO deletePromotionOut(Long promotionId) {
        return null;
    }

    @Override
    public ResponseListPageablePromotion listPromotionByPageOut(int pageNumber, int pageSize, String orderBy, String sortDir) {
        return null;
    }
}
