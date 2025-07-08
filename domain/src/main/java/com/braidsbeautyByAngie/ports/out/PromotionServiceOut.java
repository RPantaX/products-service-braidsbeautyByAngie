package com.braidsbeautyByAngie.ports.out;

import com.braidsbeautyByAngie.aggregates.dto.PromotionDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestPromotion;
import com.braidsbeautyByAngie.aggregates.response.promotions.ResponseListPageablePromotion;
import com.braidsbeautyByAngie.aggregates.response.promotions.ResponsePromotion;

import java.util.List;
import java.util.Optional;

public interface PromotionServiceOut {
    PromotionDTO createPromotionOut(RequestPromotion requestPromotion);

    Optional<ResponsePromotion> findPromotionByIdOut(Long promotionId);

    PromotionDTO updatePromotionOut(Long promotionId, RequestPromotion requestPromotion);

    PromotionDTO deletePromotionOut(Long promotionId);

    ResponseListPageablePromotion listPromotionByPageOut(int pageNumber, int pageSize, String orderBy, String sortDir);
    List<PromotionDTO> listPromotionOut();
    Optional<PromotionDTO> findPromotionByNameOut(String promotionName);
}
