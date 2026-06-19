package cn.iocoder.yudao.module.opshub.service.cs;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsTaskDO;

import java.util.Map;
import java.util.Set;

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
     * 执行员提交审批（原交付）
     * 推动 BPM 流程到审批节点，由 BPM 回调设置 DELIVERED
     *
     * @param id     工单 ID
     * @param reason 审批意见（可选）
     */
    void submitForApproval(Long id, String reason);

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
     * 取消/关闭工单
     * <p>
     * 经销商：仅 PENDING 状态 + 仅提单人可取消
     * 管理员：任意非 CLOSED 状态可关闭
     * 同步取消 BPM 流程实例
     *
     * @param id     工单 ID
     * @param reason 取消原因
     */
    void cancelTask(Long id, String reason);

    /**
     * BPM 流程状态回调更新工单状态
     * <p>
     * APPROVE → 根据当前状态推进（IN_PROGRESS→DELIVERED, DELIVERED→CLOSED）
     * REJECT → REJECTED, CANCEL → CLOSED
     * RUNNING 阶段不干预业务状态
     *
     * @param id        工单 ID
     * @param bpmStatus BPM 流程状态 {@link cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum}
     */
    void updateCsTaskStatusByBpm(Long id, Integer bpmStatus);

    /**
     * 获取各子标签的工单数量
     * @return Map: tabFilter -> count
     */
    Map<String, Long> getTabCounts();

    /**
     * 获取工单 BPM 流程当前节点的候选人用户 ID 集合
     *
     * @param csTaskId 工单 ID
     * @return 候选人用户 ID 集合
     */
    Set<Long> getTaskCandidateUserIds(Long csTaskId);

}
