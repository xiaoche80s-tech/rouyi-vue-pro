package cn.iocoder.yudao.module.opshub.service.cs.listener;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsTaskDO;
import cn.iocoder.yudao.module.opshub.dal.mysql.cs.CsTaskMapper;
import cn.iocoder.yudao.module.opshub.enums.CsTaskStatusEnum;
import cn.iocoder.yudao.module.opshub.service.cs.websocket.CsWebSocketService;
import cn.iocoder.yudao.module.opshub.service.cs.websocket.dto.CsTaskNotification;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import org.flowable.engine.RuntimeService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.mockito.ArgumentMatchers;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link CsTaskBpmAssignedListener} 单元测试
 * <p>
 * 覆盖：
 * 1. 转单场景：IN_PROGRESS 状态 + assignee 变更 → TYPE_TASK_TRANSFERRED 通知
 * 2. 接单场景：PENDING → IN_PROGRESS → TYPE_TASK_ACCEPTED 通知
 * 3. 幂等性：assignee 未变更 → 不发通知
 */
class CsTaskBpmAssignedListenerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private CsTaskBpmAssignedListener listener;

    @Mock
    private CsTaskMapper csTaskMapper;
    @Mock
    private RuntimeService runtimeService;
    @Mock
    private CsWebSocketService csWebSocketService;
    @Mock
    private NotifyMessageSendApi notifyMessageSendApi;

    private static final Long CS_TASK_ID = 1L;
    private static final String PROCESS_INSTANCE_ID = "process-123";
    private static final String BPM_TASK_ID = "bpm-task-456";
    private static final Long NEW_ASSIGNEE_ID = 300L;
    private static final Long CREATOR_USER_ID = 10L;

    // ========== 转单场景 ==========

    @Nested
    @DisplayName("转单场景 - IN_PROGRESS + assignee 变更")
    class TransferTests {

        @Test
        @DisplayName("转单成功 → 发送 TYPE_TASK_TRANSFERRED WebSocket + 站内信")
        void testTransferSendsNotification() {
            // 准备：IN_PROGRESS 状态，assignee 变更
            CsTaskDO current = buildTask(CsTaskStatusEnum.IN_PROGRESS, 100L);
            when(csTaskMapper.selectById(CS_TASK_ID)).thenReturn(current);
            when(csTaskMapper.updateById(ArgumentMatchers.<CsTaskDO>any())).thenReturn(1);

            // 直接调用 syncAssignee（模拟 afterCompletion 回调）
            invokeSyncAssignee(CS_TASK_ID, NEW_ASSIGNEE_ID);

            // 验证：cs_task 更新（assignee 不变、status 不变）
            verify(csTaskMapper).updateById(ArgumentMatchers.<CsTaskDO>argThat(update ->
                    CS_TASK_ID.equals(update.getId()) && NEW_ASSIGNEE_ID.equals(update.getAssigneeId())
                            && update.getStatus() == null)); // IN_PROGRESS 不变

            // 验证：WebSocket 推送给新处理人
            verify(csWebSocketService).sendTaskNotifyAsync(eq(NEW_ASSIGNEE_ID),
                    argThat((CsTaskNotification n) -> CsTaskNotification.TYPE_TASK_TRANSFERRED.equals(n.getType())));

            // 验证：站内信发送给新处理人
            verify(notifyMessageSendApi).sendSingleMessageToAdmin(argThat(req ->
                    NEW_ASSIGNEE_ID.equals(req.getUserId())
                            && "cs-task-transferred".equals(req.getTemplateCode())));
        }

        @Test
        @DisplayName("assignee 未变更 → 不发通知")
        void testSameAssigneeNoNotification() {
            CsTaskDO current = buildTask(CsTaskStatusEnum.IN_PROGRESS, NEW_ASSIGNEE_ID);
            when(csTaskMapper.selectById(CS_TASK_ID)).thenReturn(current);
            when(csTaskMapper.updateById(ArgumentMatchers.<CsTaskDO>any())).thenReturn(1);

            invokeSyncAssignee(CS_TASK_ID, NEW_ASSIGNEE_ID);

            // 验证：不发通知
            verify(csWebSocketService, never()).sendTaskNotifyAsync(any(), any());
            verify(notifyMessageSendApi, never()).sendSingleMessageToAdmin(any());
        }
    }

    // ========== 接单场景 ==========

    @Nested
    @DisplayName("接单场景 - PENDING → IN_PROGRESS")
    class AcceptTests {

        @Test
        @DisplayName("初始接单 → 发送 TYPE_TASK_ACCEPTED WebSocket + 站内信给提单人")
        void testInitialAcceptSendsNotification() {
            CsTaskDO current = buildTask(CsTaskStatusEnum.PENDING, null);
            when(csTaskMapper.selectById(CS_TASK_ID)).thenReturn(current);
            when(csTaskMapper.updateById(ArgumentMatchers.<CsTaskDO>any())).thenReturn(1);

            invokeSyncAssignee(CS_TASK_ID, NEW_ASSIGNEE_ID);

            // 验证：cs_task 状态推进为 IN_PROGRESS
            verify(csTaskMapper).updateById(ArgumentMatchers.<CsTaskDO>argThat(update ->
                    CS_TASK_ID.equals(update.getId())
                            && NEW_ASSIGNEE_ID.equals(update.getAssigneeId())
                            && CsTaskStatusEnum.IN_PROGRESS.getCode().equals(update.getStatus())
                            && update.getAcceptTime() != null));

            // 验证：WebSocket 推送给提单人
            verify(csWebSocketService).sendTaskNotifyAsync(eq(CREATOR_USER_ID),
                    argThat((CsTaskNotification n) -> CsTaskNotification.TYPE_TASK_ACCEPTED.equals(n.getType())));

            // 验证：站内信发送给提单人
            verify(notifyMessageSendApi).sendSingleMessageToAdmin(argThat(req ->
                    CREATOR_USER_ID.equals(req.getUserId())
                            && "cs-task-accepted".equals(req.getTemplateCode())));
        }
    }

    // ========== 边界场景 ==========

    @Nested
    @DisplayName("边界场景")
    class EdgeCaseTests {

        @Test
        @DisplayName("工单不存在 → 跳过")
        void testTaskNotExistsSkipped() {
            when(csTaskMapper.selectById(CS_TASK_ID)).thenReturn(null);

            invokeSyncAssignee(CS_TASK_ID, NEW_ASSIGNEE_ID);

            verify(csTaskMapper, never()).updateById(ArgumentMatchers.<CsTaskDO>any());
            verify(csWebSocketService, never()).sendTaskNotifyAsync(any(), any());
        }
    }

    // ========== 辅助方法 ==========

    private CsTaskDO buildTask(CsTaskStatusEnum status, Long assigneeId) {
        CsTaskDO task = new CsTaskDO();
        task.setId(CS_TASK_ID);
        task.setTaskNo("TASK-20260620-001");
        task.setStatus(status.getCode());
        task.setCreatorUserId(CREATOR_USER_ID);
        task.setAssigneeId(assigneeId);
        task.setCategory(1);
        task.setUrgency(2);
        return task;
    }

    /**
     * 直接调用 syncAssignee 私有方法（通过反射）
     */
    private void invokeSyncAssignee(Long csTaskId, Long assigneeUserId) {
        try {
            var method = CsTaskBpmAssignedListener.class.getDeclaredMethod("syncAssignee", Long.class, Long.class);
            method.setAccessible(true);
            method.invoke(listener, csTaskId, assigneeUserId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke syncAssignee", e);
        }
    }
}
