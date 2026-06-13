package cn.iocoder.yudao.module.opshub.dal.mysql.dealer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo.DealerInfoPageReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerInfoDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DealerInfoMapper extends BaseMapperX<DealerInfoDO> {

    default PageResult<DealerInfoDO> selectPage(DealerInfoPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DealerInfoDO>()
                .likeIfPresent(DealerInfoDO::getName, reqVO.getName())
                .likeIfPresent(DealerInfoDO::getCode, reqVO.getCode())
                .eqIfPresent(DealerInfoDO::getStatus, reqVO.getStatus())
                .orderByDesc(DealerInfoDO::getId));
    }

    default DealerInfoDO selectByCode(String code) {
        return selectOne(DealerInfoDO::getCode, code);
    }

    default List<DealerInfoDO> selectListByStatus(Integer status) {
        return selectList(DealerInfoDO::getStatus, status);
    }

}
