package com.braidsbeautyByAngie.impl;

import com.braidsbeautyByAngie.aggregates.dto.PromotionDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestPromotion;
import com.braidsbeautyByAngie.aggregates.response.promotions.ResponseListPageablePromotion;
import com.braidsbeautyByAngie.aggregates.response.promotions.ResponsePromotion;
import com.braidsbeautyByAngie.ports.in.PromotionServiceIn;
import com.braidsbeautyByAngie.ports.out.PromotionServiceOut;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionServiceIn {

    private final PromotionServiceOut promotionServiceOut;

    @Override
    public PromotionDTO createPromotionIn(RequestPromotion requestPromotion) {
        return promotionServiceOut.createPromotionOut(requestPromotion);
    }

    @Override
    public Optional<ResponsePromotion> findPromotionByIdIn(Long promotionId) {
        return promotionServiceOut.findPromotionByIdOut(promotionId);
    }

    @Override
    public PromotionDTO updatePromotionIn(Long promotionId, PromotionDTO promotion) {
        return promotionServiceOut.updatePromotionOut(promotionId, promotion);
    }

    @Override
    public PromotionDTO deletePromotionIn(Long promotionId) {
        return promotionServiceOut.deletePromotionOut(promotionId);
    }

    @Override
    public ResponseListPageablePromotion listPromotionByPageIn(int pageNumber, int pageSize, String orderBy, String sortDir) {
        return promotionServiceOut.listPromotionByPageOut(pageNumber, pageSize, orderBy, sortDir);
    }
}
