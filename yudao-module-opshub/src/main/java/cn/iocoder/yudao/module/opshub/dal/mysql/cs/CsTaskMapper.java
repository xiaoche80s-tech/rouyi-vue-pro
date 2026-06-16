package cn.iocoder.yudao.module.opshub.dal.mysql.cs;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.CsTaskPageReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsTaskDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CsTaskMapper extends BaseMapperX<CsTaskDO> {

    default PageResult<CsTaskDO> selectPage(CsTaskPageReqVO reqVO) {
        LambdaQueryWrapperX<CsTaskDO> wrapper = new LambdaQueryWrapperX<CsTaskDO>()
                .eqIfPresent(CsTaskDO::getStatus, reqVO.getStatus())
                .eqIfPresent(CsTaskDO::getUrgency, reqVO.getUrgency())
                .eqIfPresent(CsTaskDO::getCategory, reqVO.getCategory())
                .eqIfPresent(CsTaskDO::getAssigneeId, reqVO.getAssigneeId())
                .eqIfPresent(CsTaskDO::getCreatorUserId, reqVO.getCreatorUserId())
                .eqIfPresent(CsTaskDO::getDealerCode, reqVO.getDealerCode())
                .orderByDesc(CsTaskDO::getId);

        // 关键词搜索
        if (StrUtil.isNotBlank(reqVO.getKeyword())) {
            wrapper.and(w -> w
                    .like(CsTaskDO::getTaskNo, reqVO.getKeyword())
                    .or().like(CsTaskDO::getContent, reqVO.getKeyword())
                    .or().like(CsTaskDO::getRemark, reqVO.getKeyword()));
        }

        return selectPage(reqVO, wrapper);
    }

    /**
     * 查询当天最大工单序号（用于编号自动生成）
     */
    default Integer selectMaxSeqToday(String datePrefix) {
        String pattern = "TASK-" + datePrefix + "-%";
        List<CsTaskDO> list = selectList(new LambdaQueryWrapper<CsTaskDO>()
                .likeRight(CsTaskDO::getTaskNo, "TASK-" + datePrefix + "-")
                .orderByDesc(CsTaskDO::getTaskNo));
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

}
