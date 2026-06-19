package cn.iocoder.yudao.module.opshub.dal.mysql.basedata;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.controller.admin.basedata.vo.BasedataFilePageReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.basedata.BasedataFileDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface BasedataFileMapper extends BaseMapperX<BasedataFileDO> {

    default PageResult<BasedataFileDO> selectPage(BasedataFilePageReqVO reqVO) {
        LambdaQueryWrapperX<BasedataFileDO> wrapper = new LambdaQueryWrapperX<BasedataFileDO>()
                .eqIfPresent(BasedataFileDO::getCategory, reqVO.getCategory())
                .likeIfPresent(BasedataFileDO::getFileName, reqVO.getFileName())
                .eqIfPresent(BasedataFileDO::getDealerId, reqVO.getDealerId())
                .orderByDesc(BasedataFileDO::getId);

        // 处理有效期状态筛选
        applyExpireStatusFilter(wrapper, reqVO.getExpireStatus());

        return selectPage(reqVO, wrapper);
    }

    /**
     * 按合同编码查询附件列表
     *
     * @param dealerCode   经销商编码
     * @param contractCode 合同编码（= file_no）
     * @return 附件文件列表
     */
    default List<BasedataFileDO> selectByContractCode(String dealerCode, String contractCode) {
        return selectList(new LambdaQueryWrapperX<BasedataFileDO>()
                .eq(BasedataFileDO::getDealerCode, dealerCode)
                .eq(BasedataFileDO::getCategory, "contract")
                .eq(BasedataFileDO::getFileNo, contractCode)
                .orderByAsc(BasedataFileDO::getId));
    }

    /**
     * 按经销商编码批量查询合同类附件（用于 attachmentCount 统计）
     *
     * @param dealerCodes 经销商编码集合
     * @return category='contract' 的文件列表
     */
    default List<BasedataFileDO> selectContractFilesByDealerCodes(java.util.Collection<String> dealerCodes) {
        return selectList(new LambdaQueryWrapperX<BasedataFileDO>()
                .in(BasedataFileDO::getDealerCode, dealerCodes)
                .eq(BasedataFileDO::getCategory, "contract"));
    }

    /**
     * 按分类统计文件数量
     */
    default List<Map<String, Object>> selectCountByCategory() {
        return selectMaps(new LambdaQueryWrapperX<BasedataFileDO>()
                .select(BasedataFileDO::getCategory)
                .groupBy(BasedataFileDO::getCategory));
    }

    /**
     * 按经销商编码 + 文件名称 + 文件分类查询
     */
    default BasedataFileDO selectByDealerCodeAndFileNameAndCategory(String dealerCode, String fileName, String category) {
        return selectOne(new LambdaQueryWrapperX<BasedataFileDO>()
                .eq(BasedataFileDO::getDealerCode, dealerCode)
                .eq(BasedataFileDO::getFileName, fileName)
                .eq(BasedataFileDO::getCategory, category));
    }

    /**
     * 应用有效期状态过滤条件
     *
     * valid：expire_date > CURRENT_DATE + 30天
     * expiring_soon：expire_date <= CURRENT_DATE + 30天 AND >= CURRENT_DATE
     * expired：expire_date < CURRENT_DATE
     */
    private void applyExpireStatusFilter(LambdaQueryWrapperX<BasedataFileDO> wrapper, String expireStatus) {
        if (StrUtil.isEmpty(expireStatus)) {
            return;
        }
        LocalDate today = LocalDate.now();
        LocalDate soonThreshold = today.plusDays(30);

        switch (expireStatus) {
            case "valid":
                wrapper.gt(BasedataFileDO::getExpireDate, soonThreshold);
                break;
            case "expiring_soon":
                wrapper.le(BasedataFileDO::getExpireDate, soonThreshold);
                wrapper.ge(BasedataFileDO::getExpireDate, today);
                break;
            case "expired":
                wrapper.lt(BasedataFileDO::getExpireDate, today);
                break;
            default:
                break;
        }
    }

}
