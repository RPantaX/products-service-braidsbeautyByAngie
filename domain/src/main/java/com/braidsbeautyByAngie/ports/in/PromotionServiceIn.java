package com.braidsbeautyByAngie.ports.in;

import com.braidsbeautyByAngie.aggregates.dto.PromotionDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestPromotion;
import com.braidsbeautyByAngie.aggregates.response.promotions.ResponseListPageablePromotion;
import com.braidsbeautyByAngie.aggregates.response.promotions.ResponsePromotion;

import java.util.List;
import java.util.Optional;

public interface PromotionServiceIn {

    PromotionDTO createPromotionIn(RequestPromotion requestPromotion);

    Optional<ResponsePromotion> findPromotionByIdIn(Long promotionId);

    PromotionDTO updatePromotionIn(Long promotionId, RequestPromotion requestPromotion);

    PromotionDTO deletePromotionIn(Long promotionId);

    ResponseListPageablePromotion listPromotionByPageIn(int pageNumber, int pageSize, String orderBy, String sortDir);
    List<PromotionDTO> listPromotionIn();
    Optional<PromotionDTO> findPromotionByNameIn(String promotionName);
}
