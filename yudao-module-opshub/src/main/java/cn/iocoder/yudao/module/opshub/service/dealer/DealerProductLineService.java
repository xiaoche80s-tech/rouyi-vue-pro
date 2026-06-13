package cn.iocoder.yudao.module.opshub.service.dealer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo.DealerProductLinePageReqVO;
import cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo.DealerProductLineSaveReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerInfoDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerProductLineDO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 产品线 Service 接口
 */
public interface DealerProductLineService {

    Long createProductLine(@Valid DealerProductLineSaveReqVO createReqVO);

    void updateProductLine(@Valid DealerProductLineSaveReqVO updateReqVO);

    void deleteProductLine(Long id);

    DealerProductLineDO getProductLine(Long id);

    PageResult<DealerProductLineDO> getProductLinePage(DealerProductLinePageReqVO pageReqVO);

    List<DealerProductLineDO> getSimpleList();

    // ========== 经销商绑定 ==========

    void bindDealer(Long productLineId, Long dealerId);

    void unbindDealer(Long productLineId, Long dealerId);

    List<DealerInfoDO> getDealers(Long productLineId);

}
