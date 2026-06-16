package cn.iocoder.yudao.module.opshub.service.cs;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsTaskDO;

/**
 * 客服工单 Service 接口
 */
public interface CsTaskService {

    /**
     * 创建工单（经销商提单）
     * 自动生成工单编号，状态=待接单
     *
     * @param reqVO 创建请求
     * @return 工单ID
     */
    Long createCsTask(CsTaskCreateReqVO reqVO);

    /**
     * 获得工单详情
     */
    CsTaskDO getCsTask(Long id);

    /**
     * 获得工单分页
     */
    PageResult<CsTaskDO> getCsTaskPage(CsTaskPageReqVO reqVO);

    /**
     * 执行员接单
     * 状态：待接单(0) → 处理中(1)
     */
    void acceptTask(Long id);

    /**
     * 执行员转单
     * 变更处理人，状态保持处理中(1)
     */
    void transferTask(CsTaskTransferReqVO reqVO);

    /**
     * 执行员交付工单
     * 状态：处理中(1) → 已交付(2)
     */
    void deliverTask(Long id);

    /**
     * 经销商验收工单
     * 通过：已交付(2) → 已关闭(3)
     * 不通过：已交付(2) → 已退回(4)
     */
    void verifyTask(CsTaskVerifyReqVO reqVO);

    /**
     * 退回后重新处理
     * 状态：已退回(4) → 处理中(1)
     */
    void reprocessTask(Long id);

    /**
     * 催办工单
     * 发送催办 WebSocket 通知给处理人
     */
    void urgeTask(Long id);

    /**
     * BPM 流程状态回调更新工单状态
     * <p>
     * APPROVE → DELIVERED, REJECT → REJECTED, CANCEL → CLOSED
     * RUNNING 阶段不干预业务状态
     *
     * @param id        工单 ID
     * @param bpmStatus BPM 流程状态 {@link cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum}
     */
    void updateCsTaskStatusByBpm(Long id, Integer bpmStatus);

}
