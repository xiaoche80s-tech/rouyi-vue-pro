package cn.iocoder.yudao.module.opshub.dal.mysql.oprequest;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.controller.admin.oprequest.vo.OpRequestPageReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.oprequest.OpRequestDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作请求主表 Mapper
 */
@Mapper
public interface OpRequestMapper extends BaseMapperX<OpRequestDO> {

    default PageResult<OpRequestDO> selectPage(OpRequestPageReqVO reqVO) {
        LambdaQueryWrapperX<OpRequestDO> wrapper = new LambdaQueryWrapperX<OpRequestDO>()
                .eqIfPresent(OpRequestDO::getRequestType, reqVO.getRequestType())
                .eqIfPresent(OpRequestDO::getRequestStatus, reqVO.getRequestStatus())
                .eqIfPresent(OpRequestDO::getDealerCode, reqVO.getDealerCode())
                .orderByDesc(OpRequestDO::getId);
        // 关键词搜索
        if (StrUtil.isNotBlank(reqVO.getKeyword())) {
            wrapper.and(w -> w.like(OpRequestDO::getRequestNo, reqVO.getKeyword()));
        }
        return selectPage(reqVO, wrapper);
    }

    default OpRequestDO selectByRequestNo(String requestNo) {
        return selectOne(OpRequestDO::getRequestNo, requestNo);
    }

}
