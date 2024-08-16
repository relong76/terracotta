package edu.iu.terracotta.service.app;

import java.util.List;

import edu.iu.terracotta.model.app.Feature;
import edu.iu.terracotta.model.app.dto.FeatureDto;

public interface FeatureService {

    FeatureDto toDto(Feature feature);
    List<FeatureDto> toDto(List<Feature> features);

}
