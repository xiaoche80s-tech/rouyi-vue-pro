package cn.iocoder.yudao.module.opshub.service.excel;

import cn.iocoder.yudao.module.opshub.controller.admin.excel.vo.*;

import java.util.List;

/**
 * OpsHub Excel 批量导入 Service 接口
 */
public interface OpsExcelImportService {

    ExcelImportRespVO importDealerInfoList(List<DealerInfoImportExcelVO> list);

    ExcelImportRespVO importProductLineList(List<DealerProductLineImportExcelVO> list);

    ExcelImportRespVO importRelationList(List<DealerProductLineRelationImportExcelVO> list);

    ExcelImportRespVO importContractList(List<SigningContractImportExcelVO> list);

    ExcelImportRespVO importOrderList(List<OrderInfoImportExcelVO> list);

    ExcelImportRespVO importOrderProductList(List<OrderProductImportExcelVO> list);

    ExcelImportRespVO importOrderPaymentList(List<OrderPaymentImportExcelVO> list);

    ExcelImportRespVO importOrderInvoiceList(List<OrderInvoiceImportExcelVO> list);

    ExcelImportRespVO importOrderLogisticsList(List<OrderLogisticsImportExcelVO> list);

    ExcelImportRespVO importOrderTimelineList(List<OrderTimelineImportExcelVO> list);

    ExcelImportRespVO importAfterSaleList(List<AfterSaleInfoImportExcelVO> list);

    ExcelImportRespVO importAfterSaleProgressList(List<AfterSaleProgressImportExcelVO> list);

    ExcelImportRespVO importBasedataFileList(List<BasedataFileImportExcelVO> list);

    // ========== 政策看板 ==========

    ExcelImportRespVO importPolicyList(List<PolicyImportExcelVO> list);

    ExcelImportRespVO importPolicyIndicatorList(List<PolicyIndicatorImportExcelVO> list);

    ExcelImportRespVO importPolicyAchievementList(List<PolicyAchievementImportExcelVO> list);
}
