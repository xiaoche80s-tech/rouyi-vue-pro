package cn.iocoder.yudao.module.opshub.dal.mysql.cs;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.CsTaskPageReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsTaskDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface CsTaskMapper extends BaseMapperX<CsTaskDO> {

    /**
     * 按 tabFilter 统计工单数量
     * 复用与 selectPage 相同的可见性/标签过滤逻辑
     */
    default long selectCountByTab(String tabFilter, String viewScope, Long currentUserId) {
        LambdaQueryWrapperX<CsTaskDO> wrapper = new LambdaQueryWrapperX<>();

        // tabFilter 翻译为查询条件
        if (tabFilter != null && !"all".equals(tabFilter)) {
            switch (viewScope) {
                case "creator" -> {
                    if ("pending".equals(tabFilter)) {
                        wrapper.in(CsTaskDO::getStatus, java.util.List.of(2, 4)); // DELIVERED, REJECTED
                    }
                }
                case "assignee" -> {
                    switch (tabFilter) {
                        case "claimable" -> {
                            wrapper.eq(CsTaskDO::getStatus, 0); // PENDING
                            wrapper.isNull(CsTaskDO::getAssigneeId);
                        }
                        case "pending" -> {
                            wrapper.eq(CsTaskDO::getAssigneeId, currentUserId);
                            wrapper.in(CsTaskDO::getStatus, java.util.List.of(0, 1, 4)); // PENDING, IN_PROGRESS, REJECTED
                        }
                        case "done" -> {
                            wrapper.eq(CsTaskDO::getAssigneeId, currentUserId);
                            wrapper.in(CsTaskDO::getStatus, java.util.List.of(2, 3)); // DELIVERED, CLOSED
                        }
                    }
                }
            }
        }

        // 可见性过滤
        if (viewScope != null) {
            switch (viewScope) {
                case "creator" -> wrapper.eq(CsTaskDO::getCreatorUserId, currentUserId);
                case "assignee" -> {
                    // 当 tabFilter 活跃时已设置精确条件，跳过基础 OR 过滤
                    if (tabFilter == null || "all".equals(tabFilter)) {
                        wrapper.and(w -> w
                                .eq(CsTaskDO::getStatus, 0)
                                .or().eq(CsTaskDO::getAssigneeId, currentUserId));
                    }
                }
            }
        }

        return selectCount(wrapper);
    }

    default PageResult<CsTaskDO> selectPage(CsTaskPageReqVO reqVO) {
        LambdaQueryWrapperX<CsTaskDO> wrapper = new LambdaQueryWrapperX<CsTaskDO>()
                .eqIfPresent(CsTaskDO::getUrgency, reqVO.getUrgency())
                .eqIfPresent(CsTaskDO::getCategory, reqVO.getCategory())
                .eqIfPresent(CsTaskDO::getAssigneeId, reqVO.getAssigneeId())
                .eqIfPresent(CsTaskDO::getCreatorUserId, reqVO.getCreatorUserId())
                .eqIfPresent(CsTaskDO::getDealerCode, reqVO.getDealerCode())
                .eqIfPresent(CsTaskDO::getProductLineCode, reqVO.getProductLineCode())
                .eqIfPresent(CsTaskDO::getSourceModule, reqVO.getSourceModule())
                .orderByDesc(CsTaskDO::getId);

        // 状态过滤：statusList 优先，否则用单值 status
        if (CollUtil.isNotEmpty(reqVO.getStatusList())) {
            wrapper.in(CsTaskDO::getStatus, reqVO.getStatusList());
        } else {
            wrapper.eqIfPresent(CsTaskDO::getStatus, reqVO.getStatus());
        }
        // 未分配工单过滤（可领取）
        if (Boolean.TRUE.equals(reqVO.getUnassigned())) {
            wrapper.isNull(CsTaskDO::getAssigneeId);
        }

        // 关键词搜索
        if (StrUtil.isNotBlank(reqVO.getKeyword())) {
            wrapper.and(w -> w
                    .like(CsTaskDO::getTaskNo, reqVO.getKeyword())
                    .or().like(CsTaskDO::getContent, reqVO.getKeyword())
                    .or().like(CsTaskDO::getRemark, reqVO.getKeyword()));
        }

        // 可见性过滤（由 Service 层按角色注入）
        applyViewScope(wrapper, reqVO);

        return selectPage(reqVO, wrapper);
    }

    /**
     * 应用用户级可见性过滤
     * 当 tabFilter 活跃时，assignee scope 的基础过滤被跳过，
     * 由 Service 层 applyTabFilter 提供更精确的查询条件。
     */
    private void applyViewScope(LambdaQueryWrapperX<CsTaskDO> wrapper, CsTaskPageReqVO reqVO) {
        if (reqVO.getViewScope() == null) {
            return;
        }
        switch (reqVO.getViewScope()) {
            case "creator" -> wrapper.eq(CsTaskDO::getCreatorUserId, reqVO.getCurrentUserId());
            case "assignee" -> {
                // 当 tabFilter 活跃时，跳过基础 OR 过滤，由 tabFilter 提供精确条件
                if (reqVO.getTabFilter() == null || "all".equals(reqVO.getTabFilter())) {
                    wrapper.and(w -> w
                            .eq(CsTaskDO::getStatus, 0) // PENDING
                            .or().eq(CsTaskDO::getAssigneeId, reqVO.getCurrentUserId()));
                }
            }
            // "all" 或其他 → 无额外过滤
        }
    }

    /**
     * 查询当天最大工单序号（用于编号自动生成）
     * 使用 FOR UPDATE 行锁避免并发竞态
     */
    default Integer selectMaxSeqToday(String datePrefix) {
        List<CsTaskDO> list = selectList(new LambdaQueryWrapper<CsTaskDO>()
                .likeRight(CsTaskDO::getTaskNo, "TASK-" + datePrefix + "-")
                .orderByDesc(CsTaskDO::getTaskNo)
                .last("LIMIT 1 FOR UPDATE"));
        if (CollUtil.isEmpty(list)) {
            return 0;
        }
        String maxNo = list.get(0).getTaskNo();
        String seqStr = maxNo.substring(maxNo.lastIndexOf("-") + 1);
        try {
            return Integer.parseInt(seqStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * CAS 原子状态更新：仅当当前状态匹配时才更新
     *
     * @return 受影响行数（0=状态已被其他操作变更）
     */
    default int updateStatusByIdAndStatus(Long id, Integer oldStatus, Integer newStatus) {
        return update(null, new LambdaUpdateWrapper<CsTaskDO>()
                .eq(CsTaskDO::getId, id)
                .eq(CsTaskDO::getStatus, oldStatus)
                .set(CsTaskDO::getStatus, newStatus));
    }

}
