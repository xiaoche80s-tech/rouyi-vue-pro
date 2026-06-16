package cn.iocoder.yudao.module.opshub.dal.mysql.cs;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.CsSessionPageReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsSessionDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
public interface CsSessionMapper extends BaseMapperX<CsSessionDO> {

    default PageResult<CsSessionDO> selectPage(CsSessionPageReqVO reqVO) {
        LambdaQueryWrapperX<CsSessionDO> wrapper = new LambdaQueryWrapperX<CsSessionDO>()
                .eqIfPresent(CsSessionDO::getConsultType, reqVO.getConsultType())
                .eqIfPresent(CsSessionDO::getDealerCode, reqVO.getDealerCode())
                .eqIfPresent(CsSessionDO::getProductLineCode, reqVO.getProductLineCode())
                .eqIfPresent(CsSessionDO::getSourceModule, reqVO.getSourceModule())
                .orderByDesc(CsSessionDO::getLastMessageTime)
                .orderByDesc(CsSessionDO::getId);

        // 状态多选筛选（逗号分隔）
        if (StrUtil.isNotBlank(reqVO.getStatus())) {
            List<Integer> statusList = Arrays.stream(reqVO.getStatus().split(","))
                    .map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
            wrapper.in(CsSessionDO::getStatus, statusList);
        }

        // 关键词搜索
        if (StrUtil.isNotBlank(reqVO.getKeyword())) {
            wrapper.and(w -> w
                    .like(CsSessionDO::getSessionNo, reqVO.getKeyword())
                    .or().like(CsSessionDO::getContext, reqVO.getKeyword())
                    .or().like(CsSessionDO::getDealerName, reqVO.getKeyword()));
        }

        // 可见性过滤（由 Service 层按角色注入）
        applyViewScope(wrapper, reqVO);

        return selectPage(reqVO, wrapper);
    }

    /**
     * 应用用户级可见性过滤
     */
    private void applyViewScope(LambdaQueryWrapperX<CsSessionDO> wrapper, CsSessionPageReqVO reqVO) {
        if (reqVO.getViewScope() == null) {
            return;
        }
        switch (reqVO.getViewScope()) {
            case "creator" -> wrapper.eq(CsSessionDO::getInitiatorId, reqVO.getCurrentUserId());
            case "assignee" -> wrapper.and(w -> w
                    .eq(CsSessionDO::getStatus, 0) // PENDING
                    .or().eq(CsSessionDO::getAssigneeId, reqVO.getCurrentUserId()));
            // "all" 或其他 → 无额外过滤
        }
    }

    /**
     * 去重查询：相同 consult_type + context_code + 活跃状态
     */
    default CsSessionDO selectDuplicateSession(String consultType, String contextCode) {
        if (StrUtil.isBlank(contextCode)) {
            return null;
        }
        return selectOne(new LambdaQueryWrapper<CsSessionDO>()
                .eq(CsSessionDO::getConsultType, consultType)
                .eq(CsSessionDO::getContextCode, contextCode)
                .in(CsSessionDO::getStatus, 0, 1) // PENDING or PROCESSING
                .last("LIMIT 1"));
    }

    /**
     * 按状态统计数量
     */
    default Map<Integer, Long> selectCountGroupByStatus() {
        List<CsSessionDO> list = selectList(new LambdaQueryWrapper<CsSessionDO>()
                .select(CsSessionDO::getStatus));
        return list.stream().collect(Collectors.groupingBy(CsSessionDO::getStatus, Collectors.counting()));
    }

    /**
     * 查询当天最大会话序号（用于编号自动生成）
     */
    default Integer selectMaxSeqToday(String datePrefix) {
        List<CsSessionDO> list = selectList(new LambdaQueryWrapper<CsSessionDO>()
                .likeRight(CsSessionDO::getSessionNo, "CS-" + datePrefix + "-")
                .orderByDesc(CsSessionDO::getSessionNo));
        if (CollUtil.isEmpty(list)) {
            return 0;
        }
        String maxNo = list.get(0).getSessionNo();
        String seqStr = maxNo.substring(maxNo.lastIndexOf("-") + 1);
        try {
            return Integer.parseInt(seqStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
