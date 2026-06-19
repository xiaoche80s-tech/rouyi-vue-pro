package cn.iocoder.yudao.module.opshub.service.cs.impl;

import cn.iocoder.yudao.framework.common.biz.system.permission.PermissionCommonApi;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskApproveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskRejectReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskTransferReqVO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.CsTaskTransferReqVO;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.CsTaskVerifyReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsTaskDO;
import cn.iocoder.yudao.module.opshub.dal.mysql.cs.CsTaskMapper;
import cn.iocoder.yudao.module.opshub.enums.CsTaskStatusEnum;
import cn.iocoder.yudao.module.opshub.enums.OpsRoleCodeConstants;
import cn.iocoder.yudao.module.opshub.service.cs.event.CsTaskStatusChangeEvent;
import cn.iocoder.yudao.module.opshub.service.cs.websocket.CsWebSocketService;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;

import static cn.iocoder.yudao.module.opshub.enums.ErrorCodeConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link CsTaskServiceImpl} 单元测试
 * <p>
 * 覆盖 Step 11 核心变更：
 * 1. BPM 状态机回调（updateCsTaskStatusByBpm）
 * 2. 混合接单模式（acceptTask）
 * 3. 提交审批（submitForApproval）
 * 4. 验收（verifyTask）
 * 5. 取消/关闭（cancelTask）
 */
class CsTaskServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private CsTaskServiceImpl csTaskService;

    @Mock
    private CsTaskMapper csTaskMapper;
    @Mock
    private CsWebSocketService csWebSocketService;
    @Mock
    private BpmProcessInstanceApi processInstanceApi;
    @Mock
    private BpmTaskService bpmTaskService;
    @Mock
    private NotifyMessageSendApi notifyMessageSendApi;
    @Mock
    private PermissionCommonApi permissionCommonApi;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private SecurityFrameworkUtils securityFrameworkUtils;

    private MockedStatic<SecurityFrameworkUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityFrameworkUtils.class);
    }

    @AfterEach
    void tearDown() {
        if (securityUtilsMock != null) {
            securityUtilsMock.close();
        }
    }

    private static final Long USER_ID = 100L;
    private static final Long OTHER_USER_ID = 200L;
    private static final Long TASK_ID = 1L;
    private static final String PROCESS_INSTANCE_ID = "process-123";
    private static final String BPM_TASK_ID = "bpm-task-456";

    // ========== BPM 状态机回调测试 ==========

    @Nested
    @DisplayName("updateCsTaskStatusByBpm - BPM 回调状态推进")
    class UpdateCsTaskStatusByBpmTests {

        @Test
        @DisplayName("APPROVE + IN_PROGRESS → DELIVERED，发布事件 + 通知")
        void testApproveFromInProgress() {
            // 准备：IN_PROGRESS 状态工单
            CsTaskDO task = buildTask(CsTaskStatusEnum.IN_PROGRESS);
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);

            // 执行
            csTaskService.updateCsTaskStatusByBpm(TASK_ID, BpmProcessInstanceStatusEnum.APPROVE.getStatus());

            // 验证：状态更新为 DELIVERED
            verify(csTaskMapper).updateById(ArgumentMatchers.<CsTaskDO>argThat(update ->
                    TASK_ID.equals(update.getId())
                            && CsTaskStatusEnum.DELIVERED.getCode().equals(update.getStatus())));
            // 验证：发布事件
            verify(applicationEventPublisher).publishEvent(argThat(event ->
                    event instanceof CsTaskStatusChangeEvent e
                            && CsTaskStatusEnum.DELIVERED.getCode().equals(e.getNewStatus())));
            // 验证：通知经销商验收
            verify(csWebSocketService).sendTaskNotifyAsync(eq(task.getCreatorUserId()), any());
        }

        @Test
        @DisplayName("APPROVE + DELIVERED → CLOSED（验收通过）")
        void testApproveFromDelivered() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.DELIVERED);
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);

            csTaskService.updateCsTaskStatusByBpm(TASK_ID, BpmProcessInstanceStatusEnum.APPROVE.getStatus());

            verify(csTaskMapper).updateById(ArgumentMatchers.<CsTaskDO>argThat(update ->
                    CsTaskStatusEnum.CLOSED.getCode().equals(update.getStatus())));
            verify(applicationEventPublisher).publishEvent(argThat(event ->
                    event instanceof CsTaskStatusChangeEvent e
                            && CsTaskStatusEnum.CLOSED.getCode().equals(e.getNewStatus())));
        }

        @Test
        @DisplayName("REJECT → REJECTED（验收不通过）")
        void testReject() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.DELIVERED);
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);

            csTaskService.updateCsTaskStatusByBpm(TASK_ID, BpmProcessInstanceStatusEnum.REJECT.getStatus());

            verify(csTaskMapper).updateById(ArgumentMatchers.<CsTaskDO>argThat(update ->
                    CsTaskStatusEnum.REJECTED.getCode().equals(update.getStatus())));
            verify(applicationEventPublisher).publishEvent(argThat(event ->
                    event instanceof CsTaskStatusChangeEvent e
                            && CsTaskStatusEnum.REJECTED.getCode().equals(e.getNewStatus())));
            // 验证：通知处理人被退回
            verify(csWebSocketService).sendTaskNotifyAsync(eq(task.getAssigneeId()), any());
        }

        @Test
        @DisplayName("CANCEL → CLOSED（流程取消）")
        void testCancel() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.IN_PROGRESS);
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);

            csTaskService.updateCsTaskStatusByBpm(TASK_ID, BpmProcessInstanceStatusEnum.CANCEL.getStatus());

            verify(csTaskMapper).updateById(ArgumentMatchers.<CsTaskDO>argThat(update ->
                    CsTaskStatusEnum.CLOSED.getCode().equals(update.getStatus())));
        }

        @Test
        @DisplayName("已 CLOSED 工单不再处理（幂等保护）")
        void testAlreadyClosedIdempotent() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.CLOSED);
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);

            csTaskService.updateCsTaskStatusByBpm(TASK_ID, BpmProcessInstanceStatusEnum.APPROVE.getStatus());

            verify(csTaskMapper, never()).updateById(any(CsTaskDO.class));
            verify(applicationEventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("工单不存在时直接返回，不抛异常")
        void testTaskNotExists() {
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(null);

            csTaskService.updateCsTaskStatusByBpm(TASK_ID, BpmProcessInstanceStatusEnum.APPROVE.getStatus());

            verify(csTaskMapper, never()).updateById(any(CsTaskDO.class));
        }

        @Test
        @DisplayName("RUNNING 状态不干预业务状态")
        void testRunningNoOp() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.IN_PROGRESS);
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);

            csTaskService.updateCsTaskStatusByBpm(TASK_ID, BpmProcessInstanceStatusEnum.RUNNING.getStatus());

            verify(csTaskMapper, never()).updateById(any(CsTaskDO.class));
        }
    }

    // ========== 混合接单模式测试 ==========

    @Nested
    @DisplayName("acceptTask - 混合接单模式")
    class AcceptTaskTests {

        @Test
        @DisplayName("指定模式 - 指定人接单成功")
        void testDesignatedMode_assigneeAccepts() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.PENDING);
            task.setAssigneeId(USER_ID); // 指定模式
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);
            mockLoginUserId(USER_ID);

            csTaskService.acceptTask(TASK_ID);

            verify(csTaskMapper).updateById(ArgumentMatchers.<CsTaskDO>argThat(update ->
                    CsTaskStatusEnum.IN_PROGRESS.getCode().equals(update.getStatus())
                            && USER_ID.equals(update.getAssigneeId())
                            && update.getAcceptTime() != null));
        }

        @Test
        @DisplayName("指定模式 - 非指定人不可接单")
        void testDesignatedMode_nonAssigneeRejected() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.PENDING);
            task.setAssigneeId(USER_ID); // 指定给 USER_ID
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);
            mockLoginUserId(OTHER_USER_ID); // 但 OTHER_USER_ID 尝试接单

            assertThatThrownBy(() -> csTaskService.acceptTask(TASK_ID))
                    .message().contains("非当前处理人");
        }

        @Test
        @DisplayName("抢单模式 - 任意执行员可接单")
        void testGrabMode_anyExecutorCanAccept() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.PENDING);
            task.setAssigneeId(null); // 抢单模式
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);
            mockLoginUserId(USER_ID);

            csTaskService.acceptTask(TASK_ID);

            verify(csTaskMapper).updateById(ArgumentMatchers.<CsTaskDO>argThat(update ->
                    CsTaskStatusEnum.IN_PROGRESS.getCode().equals(update.getStatus())
                            && USER_ID.equals(update.getAssigneeId())));
        }

        @Test
        @DisplayName("非 PENDING 状态不可接单")
        void testNotPendingCannotAccept() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.IN_PROGRESS);
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);
            mockLoginUserId(USER_ID);

            assertThatThrownBy(() -> csTaskService.acceptTask(TASK_ID))
                    .message().contains("仅待接单");
        }
    }

    // ========== 提交审批测试 ==========

    @Nested
    @DisplayName("submitForApproval - 提交审批（推动 BPM）")
    class SubmitForApprovalTests {

        @Test
        @DisplayName("正常提交审批 - 推动 BPM + 记录交付时间")
        void testSubmitForApprovalSuccess() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.IN_PROGRESS);
            task.setProcessInstanceId(PROCESS_INSTANCE_ID);
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);
            mockLoginUserId(USER_ID);
            mockBpmTask(PROCESS_INSTANCE_ID, BPM_TASK_ID);

            csTaskService.submitForApproval(TASK_ID);

            // 验证：BPM 审批被推动
            verify(bpmTaskService).approveTask(eq(USER_ID), argThat(req ->
                    BPM_TASK_ID.equals(req.getId())));
            // 验证：记录交付时间
            verify(csTaskMapper).updateById(ArgumentMatchers.<CsTaskDO>argThat(update ->
                    TASK_ID.equals(update.getId()) && update.getDeliverTime() != null));
            // 验证：通知创建人验收
            verify(csWebSocketService).sendTaskNotifyAsync(eq(task.getCreatorUserId()), any());
        }

        @Test
        @DisplayName("非处理人不可提交审批")
        void testNonAssigneeCannotSubmit() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.IN_PROGRESS);
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);
            mockLoginUserId(OTHER_USER_ID);

            assertThatThrownBy(() -> csTaskService.submitForApproval(TASK_ID))
                    .message().contains("非当前处理人");
        }
    }

    // ========== 验收测试 ==========

    @Nested
    @DisplayName("verifyTask - 验收工单")
    class VerifyTaskTests {

        @Test
        @DisplayName("验收通过 - 推动 BPM 前进")
        void testVerifyPassed() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.DELIVERED);
            task.setProcessInstanceId(PROCESS_INSTANCE_ID);
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);
            mockLoginUserId(task.getCreatorUserId());
            mockBpmTask(PROCESS_INSTANCE_ID, BPM_TASK_ID);

            CsTaskVerifyReqVO reqVO = new CsTaskVerifyReqVO();
            reqVO.setId(TASK_ID);
            reqVO.setPassed(true);

            csTaskService.verifyTask(reqVO);

            // 验证：BPM 审批被推动
            verify(bpmTaskService).approveTask(eq(task.getCreatorUserId()), any());
            // 验证：记录验收时间 + 清空退回原因
            verify(csTaskMapper).updateById(ArgumentMatchers.<CsTaskDO>argThat(update ->
                    TASK_ID.equals(update.getId())
                            && update.getVerifyTime() != null
                            && update.getRejectReason() == null));
        }

        @Test
        @DisplayName("验收不通过 - 推动 BPM 退回")
        void testVerifyRejected() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.DELIVERED);
            task.setProcessInstanceId(PROCESS_INSTANCE_ID);
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);
            mockLoginUserId(task.getCreatorUserId());
            mockBpmTask(PROCESS_INSTANCE_ID, BPM_TASK_ID);

            CsTaskVerifyReqVO reqVO = new CsTaskVerifyReqVO();
            reqVO.setId(TASK_ID);
            reqVO.setPassed(false);
            reqVO.setRejectReason("数据有误，请重新处理");

            csTaskService.verifyTask(reqVO);

            // 验证：BPM 退回被推动
            verify(bpmTaskService).rejectTask(eq(task.getCreatorUserId()), argThat(req ->
                    BPM_TASK_ID.equals(req.getId())
                            && "数据有误，请重新处理".equals(req.getReason())));
            // 验证：记录退回原因
            verify(csTaskMapper).updateById(ArgumentMatchers.<CsTaskDO>argThat(update ->
                    "数据有误，请重新处理".equals(update.getRejectReason())));
        }

        @Test
        @DisplayName("非创建人不可验收")
        void testNonCreatorCannotVerify() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.DELIVERED);
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);
            mockLoginUserId(OTHER_USER_ID);

            CsTaskVerifyReqVO reqVO = new CsTaskVerifyReqVO();
            reqVO.setId(TASK_ID);
            reqVO.setPassed(true);

            assertThatThrownBy(() -> csTaskService.verifyTask(reqVO))
                    .message().contains("非提单人");
        }
    }

    // ========== 取消/关闭测试 ==========

    @Nested
    @DisplayName("cancelTask - 取消/关闭工单")
    class CancelTaskTests {

        @Test
        @DisplayName("经销商 - PENDING 状态提单人可取消")
        void testDealerCancelPending() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.PENDING);
            task.setProcessInstanceId(PROCESS_INSTANCE_ID);
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);
            mockLoginUserId(task.getCreatorUserId());
            when(permissionCommonApi.hasAnyRoles(eq(task.getCreatorUserId()),
                    eq(OpsRoleCodeConstants.BRAND_ADMIN), eq(OpsRoleCodeConstants.SUPER_ADMIN)))
                    .thenReturn(false);

            csTaskService.cancelTask(TASK_ID, "不需要了");

            verify(csTaskMapper).updateById(ArgumentMatchers.<CsTaskDO>argThat(update ->
                    CsTaskStatusEnum.CLOSED.getCode().equals(update.getStatus())));
            // 验证：发布状态变更事件
            verify(applicationEventPublisher).publishEvent(any(CsTaskStatusChangeEvent.class));
        }

        @Test
        @DisplayName("经销商 - 非 PENDING 不可取消")
        void testDealerCancelNonPending() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.IN_PROGRESS);
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);
            mockLoginUserId(task.getCreatorUserId());
            when(permissionCommonApi.hasAnyRoles(eq(task.getCreatorUserId()),
                    eq(OpsRoleCodeConstants.BRAND_ADMIN), eq(OpsRoleCodeConstants.SUPER_ADMIN)))
                    .thenReturn(false);

            assertThatThrownBy(() -> csTaskService.cancelTask(TASK_ID, "不想处理了"))
                    .message().contains("仅待接单");
        }

        @Test
        @DisplayName("管理员 - 任意非 CLOSED 状态可关闭")
        void testAdminCancelAny() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.IN_PROGRESS);
            task.setProcessInstanceId(PROCESS_INSTANCE_ID);
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);
            mockLoginUserId(USER_ID);
            when(permissionCommonApi.hasAnyRoles(eq(USER_ID),
                    eq(OpsRoleCodeConstants.BRAND_ADMIN), eq(OpsRoleCodeConstants.SUPER_ADMIN)))
                    .thenReturn(true);
            mockBpmTask(PROCESS_INSTANCE_ID, BPM_TASK_ID);

            csTaskService.cancelTask(TASK_ID, "管理员关闭");

            verify(csTaskMapper).updateById(ArgumentMatchers.<CsTaskDO>argThat(update ->
                    CsTaskStatusEnum.CLOSED.getCode().equals(update.getStatus())));
            // 验证：BPM 流程被退回
            verify(bpmTaskService).rejectTask(eq(USER_ID), any(BpmTaskRejectReqVO.class));
        }

        @Test
        @DisplayName("已 CLOSED 不可再取消（幂等保护）")
        void testAlreadyClosedCannotCancel() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.CLOSED);
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);
            mockLoginUserId(USER_ID);

            assertThatThrownBy(() -> csTaskService.cancelTask(TASK_ID, "test"))
                    .message().contains("已关闭");
        }

        @Test
        @DisplayName("经销商 - 非提单人不可取消")
        void testDealerNonCreatorCannotCancel() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.PENDING);
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);
            mockLoginUserId(OTHER_USER_ID);
            when(permissionCommonApi.hasAnyRoles(eq(OTHER_USER_ID),
                    eq(OpsRoleCodeConstants.BRAND_ADMIN), eq(OpsRoleCodeConstants.SUPER_ADMIN)))
                    .thenReturn(false);

            assertThatThrownBy(() -> csTaskService.cancelTask(TASK_ID, "test"))
                    .message().contains("非提单人");
        }
    }

    // ========== 转单测试 ==========

    @Nested
    @DisplayName("transferTask - 转单")
    class TransferTaskTests {

        @Test
        @DisplayName("流程管理员 - 可转派非自己的工单")
        void testProcessAdminCanTransferAnyTask() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.IN_PROGRESS);
            task.setProcessInstanceId(PROCESS_INSTANCE_ID);
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);
            mockLoginUserId(OTHER_USER_ID); // 非处理人
            when(permissionCommonApi.hasAnyRoles(eq(OTHER_USER_ID),
                    eq(OpsRoleCodeConstants.BRAND_ADMIN), eq(OpsRoleCodeConstants.SUPER_ADMIN),
                    eq(OpsRoleCodeConstants.PROCESS_ADMIN)))
                    .thenReturn(true);
            mockBpmTask(PROCESS_INSTANCE_ID, BPM_TASK_ID);

            CsTaskTransferReqVO reqVO = new CsTaskTransferReqVO();
            reqVO.setId(TASK_ID);
            reqVO.setNewAssigneeId(300L);
            reqVO.setReason("流程管理员转派");

            csTaskService.transferTask(reqVO);

            // 验证：处理人被更新
            verify(csTaskMapper).updateById(ArgumentMatchers.<CsTaskDO>argThat(update ->
                    TASK_ID.equals(update.getId()) && Long.valueOf(300L).equals(update.getAssigneeId())));
            // 验证：BPM 同步转派
            verify(bpmTaskService).transferTask(eq(OTHER_USER_ID), any(BpmTaskTransferReqVO.class));
        }

        @Test
        @DisplayName("执行员 - 仅可转派自己的工单")
        void testExecutorCanOnlyTransferOwnTask() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.IN_PROGRESS);
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);
            mockLoginUserId(OTHER_USER_ID); // 非处理人
            when(permissionCommonApi.hasAnyRoles(eq(OTHER_USER_ID),
                    eq(OpsRoleCodeConstants.BRAND_ADMIN), eq(OpsRoleCodeConstants.SUPER_ADMIN),
                    eq(OpsRoleCodeConstants.PROCESS_ADMIN)))
                    .thenReturn(false);

            CsTaskTransferReqVO reqVO = new CsTaskTransferReqVO();
            reqVO.setId(TASK_ID);
            reqVO.setNewAssigneeId(300L);

            assertThatThrownBy(() -> csTaskService.transferTask(reqVO))
                    .message().contains("非当前处理人");
        }
    }

    // ========== 辅助方法 ==========

    private CsTaskDO buildTask(CsTaskStatusEnum status) {
        CsTaskDO task = new CsTaskDO();
        task.setId(TASK_ID);
        task.setTaskNo("TASK-20260617-001");
        task.setStatus(status.getCode());
        task.setCreatorUserId(10L);
        task.setAssigneeId(USER_ID);
        task.setCategory(1);
        task.setUrgency(2);
        task.setSourceModule("test");
        task.setDealerCode("D001");
        task.setProductLineCode("PL001");
        return task;
    }

    private void mockLoginUserId(Long userId) {
        securityUtilsMock.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(userId);
    }

    private void mockBpmTask(String processInstanceId, String taskId) {
        Task mockTask = mock(Task.class);
        when(mockTask.getId()).thenReturn(taskId);
        when(bpmTaskService.getTasksByProcessInstanceIds(eq(Collections.singletonList(processInstanceId))))
                .thenReturn(Collections.singletonList(mockTask));
    }

}
