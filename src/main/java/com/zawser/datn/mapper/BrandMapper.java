package com.zawser.datn.mapper;

import com.zawser.datn.dto.request.BrandRequest;
import com.zawser.datn.dto.response.BrandResponse;
import com.zawser.datn.entity.Brand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BrandMapper {

    Brand toBrand(BrandRequest request);

    BrandResponse toBrandResponse(Brand brand);
}
