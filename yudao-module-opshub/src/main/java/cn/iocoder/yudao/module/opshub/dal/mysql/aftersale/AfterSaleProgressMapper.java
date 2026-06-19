package cn.iocoder.yudao.module.opshub.dal.mysql.aftersale;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.dal.dataobject.aftersale.AfterSaleProgressDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AfterSaleProgressMapper extends BaseMapperX<AfterSaleProgressDO> {

    default List<AfterSaleProgressDO> selectListByAftersaleCode(String aftersaleCode) {
        return selectList(new LambdaQueryWrapperX<AfterSaleProgressDO>()
                .eq(AfterSaleProgressDO::getAftersaleCode, aftersaleCode)
                .orderByAsc(AfterSaleProgressDO::getSortOrder));
    }

    default List<AfterSaleProgressDO> selectListByAftersaleId(Long aftersaleId) {
        return selectList(new LambdaQueryWrapperX<AfterSaleProgressDO>()
                .eq(AfterSaleProgressDO::getAftersaleId, aftersaleId)
                .orderByAsc(AfterSaleProgressDO::getSortOrder));
    }

    default AfterSaleProgressDO selectByAftersaleCodeAndNodeCode(String aftersaleCode, String nodeCode) {
        return selectOne(AfterSaleProgressDO::getAftersaleCode, aftersaleCode,
                AfterSaleProgressDO::getNodeCode, nodeCode);
    }

}
