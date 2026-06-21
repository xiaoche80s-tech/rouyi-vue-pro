package cn.iocoder.yudao.module.opshub.service.oprequest.impl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskApproveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskRejectReqVO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import cn.iocoder.yudao.module.opshub.controller.admin.oprequest.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.oprequest.OpRequestDO;
import cn.iocoder.yudao.module.opshub.dal.mysql.oprequest.OpRequestMapper;
import cn.iocoder.yudao.module.opshub.enums.OpRequestStatusEnum;
import cn.iocoder.yudao.module.opshub.enums.OpRequestTypeEnum;
import cn.iocoder.yudao.module.opshub.service.oprequest.OpRequestService;
import cn.iocoder.yudao.module.opshub.service.oprequest.handler.OpRequestTypeHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.opshub.enums.ErrorCodeConstants.*;

/**
 * 操作请求 Service 实现类
 */
@Service
@Validated
@Slf4j
public class OpRequestServiceImpl implements OpRequestService {

    public static final String PROCESS_KEY = "ops-op-request";

    @Resource
    private OpRequestMapper opRequestMapper;

    @Resource
    private cn.iocoder.yudao.module.opshub.dal.mysql.oprequest.OpRequestSigningMapper opRequestSigningMapper;

    @Resource
    private BpmProcessInstanceApi processInstanceApi;
    @Resource
    private BpmTaskService bpmTaskService;

    /**
     * 策略处理器 Map（Spring 自动注入所有 Handler Bean）
     */
    private final Map<String, OpRequestTypeHandler> handlerMap;

    public OpRequestServiceImpl(List<OpRequestTypeHandler> handlers) {
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(OpRequestTypeHandler::getRequestType, Function.identity()));
    }

    // ========== CRUD ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOpRequest(OpRequestCreateReqVO vo) {
        OpRequestTypeHandler handler = getHandler(vo.getRequestType());
        OpRequestTypeEnum typeEnum = OpRequestTypeEnum.getByCode(vo.getRequestType());

        // 1. 生成请求编号，插入主表
        OpRequestDO request = new OpRequestDO();
        request.setRequestNo(generateRequestNo());
        request.setRequestType(vo.getRequestType());
        request.setRequestTypeName(typeEnum != null ? typeEnum.getName() : vo.getRequestType());
        request.setDealerId(vo.getDealerId());
        request.setDealerCode(vo.getDealerCode());
        request.setRequestStatus(OpRequestStatusEnum.WAITING.getCode());
        request.setRemark(vo.getRemark());
        opRequestMapper.insert(request);

        // 2. 委托策略写入子表
        handler.onCreate(request, vo);

        // 3. 发起 BPM 流程
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();
        BpmProcessInstanceCreateReqDTO createReqDTO = new BpmProcessInstanceCreateReqDTO();
        createReqDTO.setProcessDefinitionKey(PROCESS_KEY);
        createReqDTO.setBusinessKey(String.valueOf(request.getId()));
        Map<String, Object> variables = new HashMap<>();
        variables.put("requestType", vo.getRequestType());
        createReqDTO.setVariables(variables);
        String processInstanceId = processInstanceApi.createProcessInstance(currentUserId, createReqDTO);

        // 4. 回写 processInstanceId
        opRequestMapper.updateById(new OpRequestDO()
                .setId(request.getId())
                .setProcessInstanceId(processInstanceId));

        // 5. 委托策略执行发起后处理（如合同 subStatus→signing）
        handler.onStart(request);

        return request.getId();
    }

    @Override
    public PageResult<OpRequestDO> getOpRequestPage(OpRequestPageReqVO reqVO) {
        return opRequestMapper.selectPage(reqVO);
    }

    @Override
    public OpRequestRespVO getOpRequest(Long id) {
        OpRequestDO request = opRequestMapper.selectById(id);
        if (request == null) {
            throw exception(OP_REQUEST_NOT_EXISTS);
        }
        OpRequestRespVO respVO = BeanUtils.toBean(request, OpRequestRespVO.class);
        // 委托策略填充子表数据
        OpRequestTypeHandler handler = handlerMap.get(request.getRequestType());
        if (handler != null) {
            handler.fillDetail(request, respVO);
        }
        return respVO;
    }

    // ========== 执行员提交处理结果 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitResult(OpRequestSubmitResultReqVO vo) {
        // 校验请求存在
        OpRequestDO request = opRequestMapper.selectById(vo.getId());
        if (request == null) {
            throw exception(OP_REQUEST_NOT_EXISTS);
        }
        // 校验状态
        if (!OpRequestStatusEnum.WAITING.getCode().equals(request.getRequestStatus())
                && !OpRequestStatusEnum.IN_PROGRESS.getCode().equals(request.getRequestStatus())) {
            throw exception(OP_REQUEST_STATUS_INVALID);
        }

        // 委托策略处理子表更新
        OpRequestTypeHandler handler = getHandler(request.getRequestType());
        handler.onSubmitResult(request, vo);

        // 推动 BPM 执行员节点审批通过
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();
        approveCurrentBpmTask(request, currentUserId, "提交处理结果");

        // Service 直接设置 DELIVERED（业务层驱动，非 BPM 回调）
        opRequestMapper.updateById(new OpRequestDO()
                .setId(request.getId())
                .setRequestStatus(OpRequestStatusEnum.DELIVERED.getCode()));
    }

    // ========== 经销商验收 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyRequest(OpRequestVerifyReqVO vo) {
        // 校验请求存在
        OpRequestDO request = opRequestMapper.selectById(vo.getId());
        if (request == null) {
            throw exception(OP_REQUEST_NOT_EXISTS);
        }
        // 校验状态
        if (!OpRequestStatusEnum.DELIVERED.getCode().equals(request.getRequestStatus())) {
            throw exception(OP_REQUEST_STATUS_INVALID);
        }

        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();
        if (Boolean.TRUE.equals(vo.getPassed())) {
            // 验收通过 → 推动 BPM 验收节点审批通过
            approveCurrentBpmTask(request, currentUserId, "验收通过");
        } else {
            // 验收不通过 → 推动 BPM 退回
            rejectCurrentBpmTask(request, currentUserId, vo.getRejectReason());
        }
    }

    // ========== BPM 回调（仅终态） ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOpRequestStatusByBpm(Long id, Integer bpmStatus) {
        OpRequestDO request = opRequestMapper.selectById(id);
        if (request == null) {
            return;
        }

        BpmProcessInstanceStatusEnum statusEnum = BpmProcessInstanceStatusEnum.valueOf(bpmStatus);
        switch (statusEnum) {
            case APPROVE -> {
                // 流程结束，验收通过
                opRequestMapper.updateById(new OpRequestDO()
                        .setId(id)
                        .setRequestStatus(OpRequestStatusEnum.CLOSED.getCode()));
                // 委托策略执行终态后处理
                OpRequestTypeHandler handler = handlerMap.get(request.getRequestType());
                if (handler != null) {
                    handler.onClosed(request);
                }
            }
            case REJECT -> opRequestMapper.updateById(new OpRequestDO()
                    .setId(id)
                    .setRequestStatus(OpRequestStatusEnum.REJECTED.getCode()));
            case CANCEL -> opRequestMapper.updateById(new OpRequestDO()
                    .setId(id)
                    .setRequestStatus(OpRequestStatusEnum.CANCEL.getCode()));
            default -> log.info("[updateOpRequestStatusByBpm][忽略 BPM 状态: {}]", statusEnum);
        }
    }

    // ========== 内部辅助方法 ==========

    private OpRequestTypeHandler getHandler(String requestType) {
        OpRequestTypeHandler handler = handlerMap.get(requestType);
        if (handler == null) {
            throw exception(OP_REQUEST_TYPE_NOT_SUPPORTED);
        }
        return handler;
    }

    private String generateRequestNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // 简单实现：OP-{日期}-{3位序号}
        // TODO: 后续可优化为数据库序列或 Redis 自增
        String prefix = "OP-" + dateStr + "-";
        List<OpRequestDO> list = opRequestMapper.selectList();
        long todayCount = list.stream()
                .filter(r -> r.getRequestNo() != null && r.getRequestNo().startsWith(prefix))
                .count();
        return prefix + String.format("%03d", todayCount + 1);
    }

    private String findCurrentBpmTaskId(String processInstanceId) {
        if (processInstanceId == null) {
            return null;
        }
        try {
            List<Task> tasks = bpmTaskService.getTasksByProcessInstanceIds(
                    Collections.singletonList(processInstanceId));
            if (CollUtil.isEmpty(tasks)) {
                return null;
            }
            return tasks.get(0).getId();
        } catch (Exception e) {
            log.warn("[findCurrentBpmTaskId][查找 BPM 任务失败 processInstanceId={}]", processInstanceId, e);
            return null;
        }
    }

    private void approveCurrentBpmTask(OpRequestDO request, Long userId, String reason) {
        String bpmTaskId = findCurrentBpmTaskId(request.getProcessInstanceId());
        if (bpmTaskId != null) {
            try {
                BpmTaskApproveReqVO approveReqVO = new BpmTaskApproveReqVO();
                approveReqVO.setId(bpmTaskId);
                approveReqVO.setReason(reason);
                bpmTaskService.approveTask(userId, approveReqVO);
            } catch (Exception e) {
                log.warn("[approveCurrentBpmTask][BPM 审批失败 requestId={}, bpmTaskId={}]", request.getId(), bpmTaskId, e);
            }
        }
    }

    private void rejectCurrentBpmTask(OpRequestDO request, Long userId, String reason) {
        String bpmTaskId = findCurrentBpmTaskId(request.getProcessInstanceId());
        if (bpmTaskId != null) {
            try {
                BpmTaskRejectReqVO rejectReqVO = new BpmTaskRejectReqVO();
                rejectReqVO.setId(bpmTaskId);
                rejectReqVO.setReason(reason);
                bpmTaskService.rejectTask(userId, rejectReqVO);
            } catch (Exception e) {
                log.warn("[rejectCurrentBpmTask][BPM 退回失败 requestId={}, bpmTaskId={}]", request.getId(), bpmTaskId, e);
            }
        }
    }

    @Override
    public OpRequestDO findActiveByContractId(Long contractId) {
        var signing = opRequestSigningMapper.selectByContractId(contractId);
        if (signing == null) {
            return null;
        }
        return opRequestMapper.selectById(signing.getRequestId());
    }

}
