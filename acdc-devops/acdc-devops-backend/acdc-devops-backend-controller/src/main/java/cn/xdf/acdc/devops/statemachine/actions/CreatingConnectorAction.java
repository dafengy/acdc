package cn.xdf.acdc.devops.statemachine.actions;

import cn.xdf.acdc.devops.biz.connect.ConnectClusterRest;
import cn.xdf.acdc.devops.core.domain.dto.ConnectorInfoDTO;
import cn.xdf.acdc.devops.core.domain.entity.enumeration.EventReason;
import cn.xdf.acdc.devops.core.domain.entity.enumeration.EventSource;
import cn.xdf.acdc.devops.core.domain.enumeration.ConnectorEvent;
import cn.xdf.acdc.devops.core.domain.enumeration.ConnectorState;
import cn.xdf.acdc.devops.service.aop.Event;
import cn.xdf.acdc.devops.service.process.connector.ConnectorCoreProcessService;
import cn.xdf.acdc.devops.statemachine.ConnectorStateMachine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
@Slf4j
public class CreatingConnectorAction extends UpdateStateToDbAction {

    private final ConnectClusterRest connectClusterRest;

    public CreatingConnectorAction(final ConnectClusterRest connectClusterRest,
            final ConnectorCoreProcessService connectorCoreProcessService
    ) {
        super(connectorCoreProcessService);
        this.connectClusterRest = connectClusterRest;
    }

    @Override
    @Event(connectorId = "#connectorInfoDTO.id", reason = EventReason.CONNECTOR_ACTUAL_STATUS_CHANGED, source = EventSource.ACDC_SCHEDULER,
            level = EVENT_LEVEL_EXPRESSION, message = EVENT_MESSAGE_EXPRESSION)
    public void execute(final ConnectorState from, final ConnectorState to, final ConnectorEvent event,
            final ConnectorInfoDTO connectorInfoDTO, final ConnectorStateMachine stateMachine) {
        try {
            connectClusterRest.createConnector(connectorInfoDTO.getConnectClusterUrl(), connectorInfoDTO.getName(),
                    connectorInfoDTO.getConnectorConfig());
        } catch (HttpClientErrorException exception) {
            if (!HttpStatus.CONFLICT.equals(exception.getStatusCode())) {
                throw exception;
            }
            log.warn("Create connector conflict: {}", exception.getMessage());
        }
        super.execute(from, to, event, connectorInfoDTO, stateMachine);
    }

    @Override
    public String name() {
        return "CreatingConnectorAction";
    }

    @Override
    public int weight() {
        return 0;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public long timeout() {
        return 0;
    }
}
