package cn.iocoder.yudao.module.opshub.dal.mysql.dealer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo.DealerProductLinePageReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerProductLineDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DealerProductLineMapper extends BaseMapperX<DealerProductLineDO> {

    default PageResult<DealerProductLineDO> selectPage(DealerProductLinePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DealerProductLineDO>()
                .likeIfPresent(DealerProductLineDO::getName, reqVO.getName())
                .likeIfPresent(DealerProductLineDO::getCode, reqVO.getCode())
                .eqIfPresent(DealerProductLineDO::getStatus, reqVO.getStatus())
                .orderByAsc(DealerProductLineDO::getSort));
    }

    default List<DealerProductLineDO> selectListByStatus(Integer status) {
        return selectList(DealerProductLineDO::getStatus, status);
    }

    default DealerProductLineDO selectByCode(String code) {
        return selectOne(DealerProductLineDO::getCode, code);
    }

}
