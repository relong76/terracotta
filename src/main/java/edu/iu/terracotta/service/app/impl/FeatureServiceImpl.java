package edu.iu.terracotta.service.app.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.iu.terracotta.model.app.Feature;
import edu.iu.terracotta.model.app.dto.FeatureDto;
import edu.iu.terracotta.service.app.FeatureService;

@Service
public class FeatureServiceImpl implements FeatureService {

    @Override
    public List<FeatureDto> toDto(List<Feature> features) {
        return features.stream()
            .map(feature -> toDto(feature))
            .toList();
    }

    @Override
    public FeatureDto toDto(Feature feature) {
        return FeatureDto.builder()
            .type(feature.getType())
            .build();
    }

}
