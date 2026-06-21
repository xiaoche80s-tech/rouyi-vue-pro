package cn.iocoder.yudao.module.opshub.service.cs.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.biz.system.permission.PermissionCommonApi;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCancelReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskApproveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskRejectReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskTransferReqVO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.CsTaskPageReqVO;
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
import java.util.List;

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
 * 2. 提交审批（submitForApproval）
 * 3. 验收（verifyTask）
 * 4. 取消/关闭（cancelTask）
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
    private BpmProcessInstanceService processInstanceService;
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

            csTaskService.submitForApproval(TASK_ID, "测试审批意见");

            // 验证：BPM 审批被推动，且 reason 已传递
            verify(bpmTaskService).approveTask(eq(USER_ID), argThat(req ->
                    BPM_TASK_ID.equals(req.getId()) && "测试审批意见".equals(req.getReason())));
            // 验证：记录交付时间
            verify(csTaskMapper).updateById(ArgumentMatchers.<CsTaskDO>argThat(update ->
                    TASK_ID.equals(update.getId()) && update.getDeliverTime() != null));
            // 通知统一由 BPM 回调发出，此处不再直接发送
            verify(csWebSocketService, never()).sendTaskNotifyAsync(anyLong(), any());
        }

        @Test
        @DisplayName("非处理人不可提交审批")
        void testNonAssigneeCannotSubmit() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.IN_PROGRESS);
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);
            mockLoginUserId(OTHER_USER_ID);

            assertThatThrownBy(() -> csTaskService.submitForApproval(TASK_ID, null))
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

            // 验证：调用 BPM cancel API（提单人用 startUser 接口）
            verify(processInstanceService).cancelProcessInstanceByStartUser(
                    eq(task.getCreatorUserId()), any(BpmProcessInstanceCancelReqVO.class));
            // BPM 回调会处理状态和事件，此处不再直接调用
            verify(csTaskMapper, never()).updateById(any(CsTaskDO.class));
            verify(applicationEventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("经销商 - DELIVERED 状态不可取消")
        void testDealerCancelNonPending() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.DELIVERED);
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

            csTaskService.cancelTask(TASK_ID, "管理员关闭");

            // 验证：调用 BPM cancel API（管理员用 admin 接口）
            verify(processInstanceService).cancelProcessInstanceByAdmin(
                    eq(USER_ID), any(BpmProcessInstanceCancelReqVO.class));
            verify(csTaskMapper, never()).updateById(any(CsTaskDO.class));
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
                    eq(OpsRoleCodeConstants.SUPER_ADMIN),
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
            // 验证：BPM 同步转派（task.getAssigneeId() = USER_ID）
            verify(bpmTaskService).transferTask(eq(USER_ID), any(BpmTaskTransferReqVO.class));
        }

        @Test
        @DisplayName("执行员 - 仅可转派自己的工单")
        void testExecutorCanOnlyTransferOwnTask() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.IN_PROGRESS);
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);
            mockLoginUserId(OTHER_USER_ID); // 非处理人
            when(permissionCommonApi.hasAnyRoles(eq(OTHER_USER_ID),
                    eq(OpsRoleCodeConstants.SUPER_ADMIN),
                    eq(OpsRoleCodeConstants.PROCESS_ADMIN)))
                    .thenReturn(false);

            CsTaskTransferReqVO reqVO = new CsTaskTransferReqVO();
            reqVO.setId(TASK_ID);
            reqVO.setNewAssigneeId(300L);

            assertThatThrownBy(() -> csTaskService.transferTask(reqVO))
                    .message().contains("非当前处理人");
        }
    }

    // ========== getBpmTaskId 测试 ==========

    @Nested
    @DisplayName("getBpmTaskId - 获取 BPM 任务 ID")
    class GetBpmTaskIdTests {

        @Test
        @DisplayName("IN_PROGRESS 工单 + 存在活跃 BPM 任务 → 返回 BPM 任务 ID")
        void testInProgressTaskReturnsBpmTaskId() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.IN_PROGRESS);
            task.setProcessInstanceId(PROCESS_INSTANCE_ID);
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);
            mockBpmTask(PROCESS_INSTANCE_ID, BPM_TASK_ID);

            String result = csTaskService.getBpmTaskId(TASK_ID);

            assertThat(result).isEqualTo(BPM_TASK_ID);
        }

        @Test
        @DisplayName("非 IN_PROGRESS 状态 → 返回 null")
        void testNonInProgressReturnsNull() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.PENDING);
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);

            String result = csTaskService.getBpmTaskId(TASK_ID);

            assertThat(result).isNull();
            // 不应查询 BPM 任务
            verify(bpmTaskService, never()).getTasksByProcessInstanceIds(any());
        }

        @Test
        @DisplayName("IN_PROGRESS 但无活跃 BPM 任务 → 返回 null")
        void testInProgressButNoBpmTaskReturnsNull() {
            CsTaskDO task = buildTask(CsTaskStatusEnum.IN_PROGRESS);
            task.setProcessInstanceId(PROCESS_INSTANCE_ID);
            when(csTaskMapper.selectById(TASK_ID)).thenReturn(task);
            when(bpmTaskService.getTasksByProcessInstanceIds(Collections.singletonList(PROCESS_INSTANCE_ID)))
                    .thenReturn(Collections.emptyList());

            String result = csTaskService.getBpmTaskId(TASK_ID);

            assertThat(result).isNull();
        }
    }

    // ========== BPM 驱动待办/已办测试 ==========

    @Nested
    @DisplayName("getCsTaskPage - BPM 驱动待办/已办查询")
    class BpmDrivenPageTests {

        @Test
        @DisplayName("执行员待办 - 通过 BPM 查询 processInstanceId 过滤")
        void testAssigneePendingUsesBpm() {
            mockLoginUserId(USER_ID);
            when(permissionCommonApi.hasAnyRoles(eq(USER_ID), eq(OpsRoleCodeConstants.DEALER))).thenReturn(false);
            when(permissionCommonApi.hasAnyRoles(eq(USER_ID), eq(OpsRoleCodeConstants.SERVICE_EXECUTOR))).thenReturn(true);
            when(bpmTaskService.getTodoProcessInstanceIds(eq(USER_ID), eq("ops-cs-task")))
                    .thenReturn(List.of("pi-1", "pi-2"));
            when(csTaskMapper.selectPage(any(CsTaskPageReqVO.class))).thenReturn(new PageResult<>(Collections.emptyList(), 0L));

            CsTaskPageReqVO reqVO = new CsTaskPageReqVO();
            reqVO.setTabFilter("pending");

            csTaskService.getCsTaskPage(reqVO);

            // 验证：BPM 方法被调用
            verify(bpmTaskService).getTodoProcessInstanceIds(USER_ID, "ops-cs-task");
            // 验证：reqVO 的 processInstanceIds 被设置
            assertThat(reqVO.getProcessInstanceIds()).containsExactly("pi-1", "pi-2");
            assertThat(reqVO.getAssigneeId()).isEqualTo(USER_ID);
            // 验证：不再使用 statusList
            assertThat(reqVO.getStatusList()).isNull();
        }

        @Test
        @DisplayName("执行员已办 - 通过 BPM 查询 processInstanceId 过滤")
        void testAssigneeDoneUsesBpm() {
            mockLoginUserId(USER_ID);
            when(permissionCommonApi.hasAnyRoles(eq(USER_ID), eq(OpsRoleCodeConstants.DEALER))).thenReturn(false);
            when(permissionCommonApi.hasAnyRoles(eq(USER_ID), eq(OpsRoleCodeConstants.SERVICE_EXECUTOR))).thenReturn(true);
            when(bpmTaskService.getDoneProcessInstanceIds(eq(USER_ID), eq("ops-cs-task")))
                    .thenReturn(List.of("pi-3"));
            when(csTaskMapper.selectPage(any(CsTaskPageReqVO.class))).thenReturn(new PageResult<>(Collections.emptyList(), 0L));

            CsTaskPageReqVO reqVO = new CsTaskPageReqVO();
            reqVO.setTabFilter("done");

            csTaskService.getCsTaskPage(reqVO);

            verify(bpmTaskService).getDoneProcessInstanceIds(USER_ID, "ops-cs-task");
            assertThat(reqVO.getProcessInstanceIds()).containsExactly("pi-3");
            assertThat(reqVO.getAssigneeId()).isEqualTo(USER_ID);
            assertThat(reqVO.getStatusList()).isNull();
        }

        @Test
        @DisplayName("管理员待办 - 查询所有流程实例（userId=null）")
        void testAdminPendingQueriesAllInstances() {
            mockLoginUserId(USER_ID);
            when(permissionCommonApi.hasAnyRoles(eq(USER_ID), eq(OpsRoleCodeConstants.DEALER))).thenReturn(false);
            when(permissionCommonApi.hasAnyRoles(eq(USER_ID), eq(OpsRoleCodeConstants.SERVICE_EXECUTOR))).thenReturn(false);
            when(bpmTaskService.getTodoProcessInstanceIds(isNull(), eq("ops-cs-task")))
                    .thenReturn(List.of("pi-a", "pi-b", "pi-c"));
            when(csTaskMapper.selectPage(any(CsTaskPageReqVO.class))).thenReturn(new PageResult<>(Collections.emptyList(), 0L));

            CsTaskPageReqVO reqVO = new CsTaskPageReqVO();
            reqVO.setTabFilter("pending");

            csTaskService.getCsTaskPage(reqVO);

            // 验证：userId=null（查询所有用户）
            verify(bpmTaskService).getTodoProcessInstanceIds(null, "ops-cs-task");
            assertThat(reqVO.getProcessInstanceIds()).containsExactly("pi-a", "pi-b", "pi-c");
            // 管理员不设 assigneeId
            assertThat(reqVO.getAssigneeId()).isNull();
        }

        @Test
        @DisplayName("管理员已办 - 查询所有流程实例（userId=null）")
        void testAdminDoneQueriesAllInstances() {
            mockLoginUserId(USER_ID);
            when(permissionCommonApi.hasAnyRoles(eq(USER_ID), eq(OpsRoleCodeConstants.DEALER))).thenReturn(false);
            when(permissionCommonApi.hasAnyRoles(eq(USER_ID), eq(OpsRoleCodeConstants.SERVICE_EXECUTOR))).thenReturn(false);
            when(bpmTaskService.getDoneProcessInstanceIds(isNull(), eq("ops-cs-task")))
                    .thenReturn(List.of("pi-x"));
            when(csTaskMapper.selectPage(any(CsTaskPageReqVO.class))).thenReturn(new PageResult<>(Collections.emptyList(), 0L));

            CsTaskPageReqVO reqVO = new CsTaskPageReqVO();
            reqVO.setTabFilter("done");

            csTaskService.getCsTaskPage(reqVO);

            verify(bpmTaskService).getDoneProcessInstanceIds(null, "ops-cs-task");
            assertThat(reqVO.getProcessInstanceIds()).containsExactly("pi-x");
        }

        @Test
        @DisplayName("BPM 无待办 → 空列表，返回空结果")
        void testBpmReturnsEmptyLeadsToEmptyResult() {
            mockLoginUserId(USER_ID);
            when(permissionCommonApi.hasAnyRoles(eq(USER_ID), eq(OpsRoleCodeConstants.DEALER))).thenReturn(false);
            when(permissionCommonApi.hasAnyRoles(eq(USER_ID), eq(OpsRoleCodeConstants.SERVICE_EXECUTOR))).thenReturn(true);
            when(bpmTaskService.getTodoProcessInstanceIds(eq(USER_ID), eq("ops-cs-task")))
                    .thenReturn(Collections.emptyList());
            when(csTaskMapper.selectPage(any(CsTaskPageReqVO.class))).thenReturn(new PageResult<>(Collections.emptyList(), 0L));

            CsTaskPageReqVO reqVO = new CsTaskPageReqVO();
            reqVO.setTabFilter("pending");

            csTaskService.getCsTaskPage(reqVO);

            // processInstanceIds 为空列表（Mapper 会用永假条件）
            assertThat(reqVO.getProcessInstanceIds()).isEmpty();
        }

        @Test
        @DisplayName("可领取标签不受 BPM 影响")
        void testClaimableNotAffectedByBpm() {
            mockLoginUserId(USER_ID);
            when(permissionCommonApi.hasAnyRoles(eq(USER_ID), eq(OpsRoleCodeConstants.DEALER))).thenReturn(false);
            when(permissionCommonApi.hasAnyRoles(eq(USER_ID), eq(OpsRoleCodeConstants.SERVICE_EXECUTOR))).thenReturn(true);
            when(csTaskMapper.selectPage(any(CsTaskPageReqVO.class))).thenReturn(new PageResult<>(Collections.emptyList(), 0L));

            CsTaskPageReqVO reqVO = new CsTaskPageReqVO();
            reqVO.setTabFilter("claimable");

            csTaskService.getCsTaskPage(reqVO);

            // 不应查询 BPM
            verify(bpmTaskService, never()).getTodoProcessInstanceIds(any(), any());
            verify(bpmTaskService, never()).getDoneProcessInstanceIds(any(), any());
            assertThat(reqVO.getProcessInstanceIds()).isNull();
            assertThat(reqVO.getUnassigned()).isTrue();
        }

        @Test
        @DisplayName("经销商待办不受 BPM 影响")
        void testCreatorPendingNotAffectedByBpm() {
            mockLoginUserId(USER_ID);
            when(permissionCommonApi.hasAnyRoles(eq(USER_ID), eq(OpsRoleCodeConstants.DEALER))).thenReturn(true);
            when(csTaskMapper.selectPage(any(CsTaskPageReqVO.class))).thenReturn(new PageResult<>(Collections.emptyList(), 0L));

            CsTaskPageReqVO reqVO = new CsTaskPageReqVO();
            reqVO.setTabFilter("pending");

            csTaskService.getCsTaskPage(reqVO);

            // 不应查询 BPM
            verify(bpmTaskService, never()).getTodoProcessInstanceIds(any(), any());
            assertThat(reqVO.getProcessInstanceIds()).isNull();
            // 经销商待办仍用 statusList
            assertThat(reqVO.getStatusList()).containsExactly(2, 4); // DELIVERED, REJECTED
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
