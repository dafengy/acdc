package cn.xdf.acdc.devops.service.process.connection.approval.action.impl;

import cn.xdf.acdc.devops.core.domain.dto.DomainUserDTO;
import cn.xdf.acdc.devops.core.domain.entity.enumeration.ApprovalState;
import cn.xdf.acdc.devops.service.process.connection.ConnectionRequisitionProcessService;
import cn.xdf.acdc.devops.service.process.connection.approval.ApprovalContext;
import cn.xdf.acdc.devops.service.process.connection.approval.ApprovalStateMachine;
import cn.xdf.acdc.devops.service.process.connection.approval.ApproveEmailSender;
import cn.xdf.acdc.devops.service.process.connection.approval.action.DbaApprovedAction;
import cn.xdf.acdc.devops.service.process.connection.approval.event.ApprovalEvent;
import cn.xdf.acdc.devops.service.utility.mail.EmailTemplate;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Component
@Transactional
public class DefaultDbaApprovedAction implements DbaApprovedAction {

    @Autowired
    private ConnectionRequisitionProcessService connectionRequisitionProcessService;

    @Autowired
    private ApproveEmailSender emailSender;

    @Override
    public void action(
            final ApprovalState from,
            final ApprovalState to,
            final ApprovalEvent event,
            final ApprovalContext context,
            final ApprovalStateMachine machine) {
        // 1. transform
        Long id = context.getId();
        String domainAccount = context.getOperatorId();
        connectionRequisitionProcessService.checkDbaPermissions(id, domainAccount);
        String approveResult = context.getDescription();
        connectionRequisitionProcessService.updateApproveStateByDBA(id, to, approveResult, domainAccount);
        connectionRequisitionProcessService.approveRequisitionConnections(id);

        // 2. send email
        DomainUserDTO proposer = machine.getProposer(id);

        List<DomainUserDTO> cc = new ArrayList<>();

        emailSender.sendApproveEmail(
                id,
                Lists.newArrayList(proposer),
                cc,
                EmailTemplate.DBA_APPROVED
        );
    }
}
