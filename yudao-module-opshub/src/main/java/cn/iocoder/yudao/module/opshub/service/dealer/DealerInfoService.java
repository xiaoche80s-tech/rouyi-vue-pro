package cn.iocoder.yudao.module.opshub.service.dealer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo.DealerInfoPageReqVO;
import cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo.DealerInfoSaveReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerInfoDO;

import jakarta.validation.Valid;

import java.util.List;

/**
 * 经销商 Service 接口
 */
public interface DealerInfoService {

    Long createDealer(@Valid DealerInfoSaveReqVO createReqVO);

    void updateDealer(@Valid DealerInfoSaveReqVO updateReqVO);

    void deleteDealer(Long id);

    DealerInfoDO getDealer(Long id);

    PageResult<DealerInfoDO> getDealerPage(DealerInfoPageReqVO pageReqVO);

    /**
     * 获得开启状态的经销商精简列表
     */
    List<DealerInfoDO> getSimpleList();

}
