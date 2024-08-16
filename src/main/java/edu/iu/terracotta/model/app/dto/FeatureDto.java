package edu.iu.terracotta.model.app.dto;

import edu.iu.terracotta.model.app.enumerator.FeatureType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureDto {

    private FeatureType type;

}
