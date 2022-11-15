package cn.xdf.acdc.devops.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConnectorCreationResultDTO {

    private ConnectorDTO sourceConnector;

    private ConnectorDTO sinkConnector;
}
