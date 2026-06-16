package cn.iocoder.yudao.module.opshub.dal.mysql.cs;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.CsOpReqPageReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsOpReqDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CsOpReqMapper extends BaseMapperX<CsOpReqDO> {

    default PageResult<CsOpReqDO> selectPage(CsOpReqPageReqVO reqVO) {
        LambdaQueryWrapperX<CsOpReqDO> wrapper = new LambdaQueryWrapperX<CsOpReqDO>()
                .eqIfPresent(CsOpReqDO::getStatus, reqVO.getStatus())
                .eqIfPresent(CsOpReqDO::getOpType, reqVO.getOpType())
                .eqIfPresent(CsOpReqDO::getDealerCode, reqVO.getDealerCode())
                .eqIfPresent(CsOpReqDO::getProductLineCode, reqVO.getProductLineCode())
                .eqIfPresent(CsOpReqDO::getSourceModule, reqVO.getSourceModule())
                .orderByDesc(CsOpReqDO::getId);

        // 关键词搜索
        if (StrUtil.isNotBlank(reqVO.getKeyword())) {
            wrapper.and(w -> w
                    .like(CsOpReqDO::getOpreqCode, reqVO.getKeyword())
                    .or().like(CsOpReqDO::getContent, reqVO.getKeyword())
                    .or().like(CsOpReqDO::getSourceCode, reqVO.getKeyword()));
        }

        // 可见性过滤（由 Service 层按角色注入）
        applyViewScope(wrapper, reqVO);

        return selectPage(reqVO, wrapper);
    }

    /**
     * 应用用户级可见性过滤
     */
    private void applyViewScope(LambdaQueryWrapperX<CsOpReqDO> wrapper, CsOpReqPageReqVO reqVO) {
        if (reqVO.getViewScope() == null) {
            return;
        }
        switch (reqVO.getViewScope()) {
            case "creator" -> wrapper.eq(CsOpReqDO::getCreatorUserId, reqVO.getCurrentUserId());
            case "assignee" -> wrapper.and(w -> w
                    .eq(CsOpReqDO::getStatus, 0) // PENDING
                    .or().eq(CsOpReqDO::getAssigneeId, reqVO.getCurrentUserId()));
            // "all" 或其他 → 无额外过滤
        }
    }

    /**
     * 查询当天最大操作请求序号（用于编号自动生成）
     */
    default Integer selectMaxSeqToday(String datePrefix) {
        List<CsOpReqDO> list = selectList(new LambdaQueryWrapper<CsOpReqDO>()
                .likeRight(CsOpReqDO::getOpreqCode, "OPR-" + datePrefix + "-")
                .orderByDesc(CsOpReqDO::getOpreqCode));
        if (CollUtil.isEmpty(list)) {
            return 0;
        }
        String maxCode = list.get(0).getOpreqCode();
        String seqStr = maxCode.substring(maxCode.lastIndexOf("-") + 1);
        try {
            return Integer.parseInt(seqStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
