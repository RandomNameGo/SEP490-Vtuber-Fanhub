package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class BannerDetailResponse extends BannerResponse {
    private List<BannerItemResponse> items;
}
