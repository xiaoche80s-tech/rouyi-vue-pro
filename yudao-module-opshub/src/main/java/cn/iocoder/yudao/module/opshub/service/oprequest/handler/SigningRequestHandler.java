package cn.iocoder.yudao.module.opshub.service.oprequest.handler;

import cn.iocoder.yudao.module.opshub.controller.admin.oprequest.vo.OpRequestCreateReqVO;
import cn.iocoder.yudao.module.opshub.controller.admin.oprequest.vo.OpRequestRespVO;
import cn.iocoder.yudao.module.opshub.controller.admin.oprequest.vo.OpRequestSubmitResultReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.oprequest.OpRequestDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.oprequest.OpRequestSigningDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.signing.SigningContractDO;
import cn.iocoder.yudao.module.opshub.dal.mysql.oprequest.OpRequestSigningMapper;
import cn.iocoder.yudao.module.opshub.dal.mysql.signing.SigningContractMapper;
import cn.iocoder.yudao.module.opshub.enums.ContractSubStatusEnum;
import cn.iocoder.yudao.module.opshub.enums.ContractStatusEnum;
import cn.iocoder.yudao.module.opshub.enums.OpRequestTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.opshub.enums.ErrorCodeConstants.SIGNING_CONTRACT_NOT_EXISTS;

/**
 * 签约类型操作请求处理器
 */
@Component
public class SigningRequestHandler implements OpRequestTypeHandler {

    @Resource
    private OpRequestSigningMapper opRequestSigningMapper;
    @Resource
    private SigningContractMapper signingContractMapper;

    @Override
    public String getRequestType() {
        return OpRequestTypeEnum.SIGNING.getCode();
    }

    @Override
    public void onCreate(OpRequestDO request, OpRequestCreateReqVO vo) {
        // 校验合同存在
        SigningContractDO contract = signingContractMapper.selectById(vo.getContractId());
        if (contract == null) {
            throw exception(SIGNING_CONTRACT_NOT_EXISTS);
        }
        // 写入签约子表
        OpRequestSigningDO signing = new OpRequestSigningDO();
        signing.setRequestId(request.getId());
        signing.setContractId(contract.getId());
        signing.setContractCode(contract.getContractCode());
        signing.setContractName(contract.getContractName());
        opRequestSigningMapper.insert(signing);
    }

    @Override
    public void onStart(OpRequestDO request) {
        // 更新合同 subStatus → signing
        OpRequestSigningDO signing = opRequestSigningMapper.selectByRequestId(request.getId());
        if (signing != null) {
            signingContractMapper.updateById(new SigningContractDO()
                    .setId(signing.getContractId())
                    .setSubStatus(ContractSubStatusEnum.SIGNING.getCode()));
        }
    }

    @Override
    public void onSubmitResult(OpRequestDO request, OpRequestSubmitResultReqVO vo) {
        // 附件已通过 CsAttachmentService 上传，此处仅做关联记录
        // attachmentIds 已在 Controller 层通过 CsAttachmentService 处理
        // 这里可以做额外的子表更新（如需要）
    }

    @Override
    public void onClosed(OpRequestDO request) {
        // BPM 终态 APPROVE → 更新合同 status=SIGNED、signDate=今天、清空 subStatus
        OpRequestSigningDO signing = opRequestSigningMapper.selectByRequestId(request.getId());
        if (signing != null) {
            signingContractMapper.updateById(new SigningContractDO()
                    .setId(signing.getContractId())
                    .setStatus(ContractStatusEnum.SIGNED.getCode())
                    .setSignDate(LocalDate.now())
                    .setSubStatus(null));
        }
    }

    @Override
    public void fillDetail(OpRequestDO request, OpRequestRespVO respVO) {
        OpRequestSigningDO signing = opRequestSigningMapper.selectByRequestId(request.getId());
        respVO.fillSigning(signing);
    }

}
