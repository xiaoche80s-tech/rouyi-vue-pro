package cn.iocoder.yudao.module.opshub.service.excel.impl;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.opshub.controller.admin.excel.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.aftersale.AfterSaleInfoDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.aftersale.AfterSaleProgressDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.basedata.BasedataFileDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerInfoDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerProductLineDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerProductLineRelationDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.order.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.signing.SigningContractDO;
import cn.iocoder.yudao.module.opshub.dal.mysql.aftersale.AfterSaleInfoMapper;
import cn.iocoder.yudao.module.opshub.dal.mysql.aftersale.AfterSaleProgressMapper;
import cn.iocoder.yudao.module.opshub.dal.mysql.basedata.BasedataFileMapper;
import cn.iocoder.yudao.module.opshub.dal.mysql.dealer.DealerInfoMapper;
import cn.iocoder.yudao.module.opshub.dal.mysql.dealer.DealerProductLineMapper;
import cn.iocoder.yudao.module.opshub.dal.mysql.dealer.DealerProductLineRelationMapper;
import cn.iocoder.yudao.module.opshub.dal.mysql.order.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.policy.DealerPolicyDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.policy.DealerPolicyIndicatorDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.policy.DealerPolicyAchievementDO;
import cn.iocoder.yudao.module.opshub.dal.mysql.policy.DealerPolicyMapper;
import cn.iocoder.yudao.module.opshub.dal.mysql.policy.DealerPolicyIndicatorMapper;
import cn.iocoder.yudao.module.opshub.dal.mysql.policy.DealerPolicyAchievementMapper;
import cn.iocoder.yudao.module.opshub.dal.mysql.signing.SigningContractMapper;
import cn.iocoder.yudao.module.opshub.service.dealer.DealerInfoService;
import cn.iocoder.yudao.module.opshub.service.excel.OpsExcelImportService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * OpsHub Excel 批量导入 Service 实现
 */
@Service
public class OpsExcelImportServiceImpl implements OpsExcelImportService {

    @Resource private DealerInfoMapper dealerInfoMapper;
    @Resource private DealerProductLineMapper productLineMapper;
    @Resource private DealerProductLineRelationMapper relationMapper;
    @Resource private SigningContractMapper contractMapper;
    @Resource private OrderInfoMapper orderInfoMapper;
    @Resource private OrderProductMapper orderProductMapper;
    @Resource private OrderPaymentMapper orderPaymentMapper;
    @Resource private OrderInvoiceMapper orderInvoiceMapper;
    @Resource private OrderLogisticsMapper orderLogisticsMapper;
    @Resource private OrderTimelineMapper orderTimelineMapper;
    @Resource private AfterSaleInfoMapper afterSaleInfoMapper;
    @Resource private AfterSaleProgressMapper afterSaleProgressMapper;
    @Resource private BasedataFileMapper basedataFileMapper;
    @Resource private DealerInfoService dealerInfoService;
    @Resource private DealerPolicyMapper dealerPolicyMapper;
    @Resource private DealerPolicyIndicatorMapper policyIndicatorMapper;
    @Resource private DealerPolicyAchievementMapper policyAchievementMapper;

    // ========== L0 基础主数据 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcelImportRespVO importDealerInfoList(List<DealerInfoImportExcelVO> list) {
        // 预加载已有数据到 Map
        Map<String, DealerInfoDO> existingMap = dealerInfoMapper.selectList().stream()
                .collect(Collectors.toMap(DealerInfoDO::getDealerCode, d -> d, (a, b) -> a));
        int success = 0, insert = 0, update = 0, fail = 0;
        Map<Integer, String> failureRows = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            int rowNo = i + 2; // Excel 行号 = 索引 + 2（表头 + 0-based）
            try {
                DealerInfoImportExcelVO vo = list.get(i);
                if (StrUtil.isBlank(vo.getDealerName())) throw new IllegalArgumentException("经销商名称不能为空");
                if (StrUtil.isBlank(vo.getDealerCode())) throw new IllegalArgumentException("经销商编码不能为空");
                DealerInfoDO existing = existingMap.get(vo.getDealerCode());
                if (existing != null) {
                    existing.setDealerName(vo.getDealerName()).setContactName(vo.getContactName())
                            .setContactPhone(vo.getContactPhone()).setAddress(vo.getAddress())
                            .setStatus(parseInteger(vo.getStatus())).setRemark(vo.getRemark());
                    dealerInfoMapper.updateById(existing);
                    update++;
                } else {
                    DealerInfoDO dealer = new DealerInfoDO().setDealerName(vo.getDealerName())
                            .setDealerCode(vo.getDealerCode()).setContactName(vo.getContactName())
                            .setContactPhone(vo.getContactPhone()).setAddress(vo.getAddress())
                            .setStatus(parseInteger(vo.getStatus())).setRemark(vo.getRemark());
                    dealerInfoMapper.insert(dealer);
                    existingMap.put(vo.getDealerCode(), dealer);
                    insert++;
                }
                success++;
            } catch (Exception e) {
                fail++;
                failureRows.put(rowNo, e.getMessage());
            }
        }
        return ExcelImportRespVO.builder().successCount(success).insertCount(insert).updateCount(update)
                .failureCount(fail).failureRows(failureRows).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcelImportRespVO importProductLineList(List<DealerProductLineImportExcelVO> list) {
        Map<String, DealerProductLineDO> existingMap = productLineMapper.selectList().stream()
                .collect(Collectors.toMap(DealerProductLineDO::getProductLineCode, d -> d, (a, b) -> a));
        int success = 0, insert = 0, update = 0, fail = 0;
        Map<Integer, String> failureRows = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            int rowNo = i + 2;
            try {
                DealerProductLineImportExcelVO vo = list.get(i);
                if (StrUtil.isBlank(vo.getProductLineName())) throw new IllegalArgumentException("产品线名称不能为空");
                if (StrUtil.isBlank(vo.getProductLineCode())) throw new IllegalArgumentException("产品线编码不能为空");
                DealerProductLineDO existing = existingMap.get(vo.getProductLineCode());
                if (existing != null) {
                    existing.setProductLineName(vo.getProductLineName()).setSort(vo.getSort())
                            .setStatus(parseInteger(vo.getStatus())).setRemark(vo.getRemark());
                    productLineMapper.updateById(existing);
                    update++;
                } else {
                    DealerProductLineDO pl = new DealerProductLineDO().setProductLineName(vo.getProductLineName())
                            .setProductLineCode(vo.getProductLineCode()).setSort(vo.getSort())
                            .setStatus(parseInteger(vo.getStatus())).setRemark(vo.getRemark());
                    productLineMapper.insert(pl);
                    existingMap.put(vo.getProductLineCode(), pl);
                    insert++;
                }
                success++;
            } catch (Exception e) {
                fail++;
                failureRows.put(rowNo, e.getMessage());
            }
        }
        return ExcelImportRespVO.builder().successCount(success).insertCount(insert).updateCount(update)
                .failureCount(fail).failureRows(failureRows).build();
    }

    // ========== L1 关联数据 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcelImportRespVO importRelationList(List<DealerProductLineRelationImportExcelVO> list) {
        Map<String, DealerInfoDO> dealerMap = dealerInfoMapper.selectList().stream()
                .collect(Collectors.toMap(DealerInfoDO::getDealerCode, d -> d, (a, b) -> a));
        Map<String, DealerProductLineDO> plMap = productLineMapper.selectList().stream()
                .collect(Collectors.toMap(DealerProductLineDO::getProductLineCode, d -> d, (a, b) -> a));
        Map<String, DealerProductLineRelationDO> relMap = relationMapper.selectList().stream()
                .collect(Collectors.toMap(r -> r.getDealerCode() + "|" + r.getProductLineCode(), r -> r, (a, b) -> a));
        int success = 0, insert = 0, update = 0, fail = 0;
        Map<Integer, String> failureRows = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            int rowNo = i + 2;
            try {
                DealerProductLineRelationImportExcelVO vo = list.get(i);
                if (StrUtil.isBlank(vo.getDealerCode())) throw new IllegalArgumentException("经销商编码不能为空");
                if (StrUtil.isBlank(vo.getProductLineCode())) throw new IllegalArgumentException("产品线编码不能为空");
                if (!dealerMap.containsKey(vo.getDealerCode()))
                    throw new IllegalArgumentException("经销商编码不存在: " + vo.getDealerCode());
                if (!plMap.containsKey(vo.getProductLineCode()))
                    throw new IllegalArgumentException("产品线编码不存在: " + vo.getProductLineCode());
                String key = vo.getDealerCode() + "|" + vo.getProductLineCode();
                if (relMap.containsKey(key)) {
                    // 幂等：已存在则跳过
                    update++;
                } else {
                    DealerProductLineRelationDO rel = new DealerProductLineRelationDO()
                            .setDealerCode(vo.getDealerCode()).setProductLineCode(vo.getProductLineCode());
                    relationMapper.insert(rel);
                    relMap.put(key, rel);
                    insert++;
                }
                success++;
            } catch (Exception e) {
                fail++;
                failureRows.put(rowNo, e.getMessage());
            }
        }
        return ExcelImportRespVO.builder().successCount(success).insertCount(insert).updateCount(update)
                .failureCount(fail).failureRows(failureRows).build();
    }

    // ========== L2 业务主数据 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcelImportRespVO importContractList(List<SigningContractImportExcelVO> list) {
        Map<String, DealerInfoDO> dealerMap = dealerInfoMapper.selectList().stream()
                .collect(Collectors.toMap(DealerInfoDO::getDealerCode, d -> d, (a, b) -> a));
        int success = 0, insert = 0, update = 0, fail = 0;
        Map<Integer, String> failureRows = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            int rowNo = i + 2;
            try {
                SigningContractImportExcelVO vo = list.get(i);
                if (StrUtil.isBlank(vo.getContractCode())) throw new IllegalArgumentException("合同编码不能为空");
                if (StrUtil.isBlank(vo.getDealerCode())) throw new IllegalArgumentException("经销商编码不能为空");
                if (StrUtil.isBlank(vo.getContractType())) throw new IllegalArgumentException("合同类型不能为空");
                if (StrUtil.isBlank(vo.getContractName())) throw new IllegalArgumentException("合同名称不能为空");
                DealerInfoDO dealer = dealerMap.get(vo.getDealerCode());
                if (dealer == null) throw new IllegalArgumentException("经销商编码不存在: " + vo.getDealerCode());
                SigningContractDO existing = contractMapper.selectByContractCode(vo.getContractCode());
                if (existing != null) {
                    existing.setDealerId(dealer.getId()).setDealerCode(vo.getDealerCode())
                            .setProductLineCode(vo.getProductLineCode()).setContractType(vo.getContractType())
                            .setContractName(vo.getContractName()).setIssuedDate(vo.getIssuedDate())
                            .setSignDate(vo.getSignDate()).setSummary(vo.getSummary()).setRemark(vo.getRemark());
                    contractMapper.updateById(existing);
                    update++;
                } else {
                    SigningContractDO contract = new SigningContractDO().setDealerId(dealer.getId())
                            .setDealerCode(vo.getDealerCode()).setProductLineCode(vo.getProductLineCode())
                            .setContractType(vo.getContractType()).setContractCode(vo.getContractCode())
                            .setContractName(vo.getContractName()).setStatus("unsigned").setSubStatus("pending")
                            .setIssuedDate(vo.getIssuedDate()).setSignDate(vo.getSignDate())
                            .setSummary(vo.getSummary()).setRemark(vo.getRemark());
                    contractMapper.insert(contract);
                    insert++;
                }
                success++;
            } catch (Exception e) {
                fail++;
                failureRows.put(rowNo, e.getMessage());
            }
        }
        return ExcelImportRespVO.builder().successCount(success).insertCount(insert).updateCount(update)
                .failureCount(fail).failureRows(failureRows).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcelImportRespVO importOrderList(List<OrderInfoImportExcelVO> list) {
        Map<String, DealerInfoDO> dealerMap = dealerInfoMapper.selectList().stream()
                .collect(Collectors.toMap(DealerInfoDO::getDealerCode, d -> d, (a, b) -> a));
        Map<String, DealerProductLineDO> plMap = productLineMapper.selectList().stream()
                .collect(Collectors.toMap(DealerProductLineDO::getProductLineCode, d -> d, (a, b) -> a));
        int success = 0, insert = 0, update = 0, fail = 0;
        Map<Integer, String> failureRows = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            int rowNo = i + 2;
            try {
                OrderInfoImportExcelVO vo = list.get(i);
                if (StrUtil.isBlank(vo.getOrderCode())) throw new IllegalArgumentException("订单号不能为空");
                if (StrUtil.isBlank(vo.getDealerCode())) throw new IllegalArgumentException("经销商编码不能为空");
                DealerInfoDO dealer = dealerMap.get(vo.getDealerCode());
                if (dealer == null) throw new IllegalArgumentException("经销商编码不存在: " + vo.getDealerCode());
                DealerProductLineDO pl = null;
                if (StrUtil.isNotBlank(vo.getProductLineCode())) {
                    pl = plMap.get(vo.getProductLineCode());
                    if (pl == null) throw new IllegalArgumentException("产品线编码不存在: " + vo.getProductLineCode());
                }
                OrderInfoDO existing = orderInfoMapper.selectByOrderCode(vo.getOrderCode());
                if (existing != null) {
                    existing.setDealerId(dealer.getId()).setDealerCode(vo.getDealerCode())
                            .setDealerName(dealer.getDealerName())
                            .setProductLineCode(vo.getProductLineCode())
                            .setProductLineName(pl != null ? pl.getProductLineName() : null)
                            .setTotalAmount(vo.getTotalAmount()).setOrderDate(vo.getOrderDate())
                            .setProgressStatus(vo.getProgressStatus())
                            .setPayStatus(StrUtil.isNotBlank(vo.getPayStatus()) ? vo.getPayStatus() : existing.getPayStatus())
                            .setInvStatus(StrUtil.isNotBlank(vo.getInvStatus()) ? vo.getInvStatus() : existing.getInvStatus())
                            .setRemark(vo.getRemark());
                    orderInfoMapper.updateById(existing);
                    update++;
                } else {
                    OrderInfoDO order = new OrderInfoDO().setOrderCode(vo.getOrderCode())
                            .setDealerId(dealer.getId()).setDealerCode(vo.getDealerCode())
                            .setDealerName(dealer.getDealerName())
                            .setProductLineCode(vo.getProductLineCode())
                            .setProductLineName(pl != null ? pl.getProductLineName() : null)
                            .setTotalAmount(vo.getTotalAmount()).setOrderDate(vo.getOrderDate())
                            .setProgressStatus(vo.getProgressStatus())
                            .setPayStatus(StrUtil.isNotBlank(vo.getPayStatus()) ? vo.getPayStatus() : "unpaid")
                            .setInvStatus(StrUtil.isNotBlank(vo.getInvStatus()) ? vo.getInvStatus() : "uninvoiced")
                            .setRemark(vo.getRemark());
                    orderInfoMapper.insert(order);
                    insert++;
                }
                success++;
            } catch (Exception e) {
                fail++;
                failureRows.put(rowNo, e.getMessage());
            }
        }
        return ExcelImportRespVO.builder().successCount(success).insertCount(insert).updateCount(update)
                .failureCount(fail).failureRows(failureRows).build();
    }

    // ========== L3 业务子表 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcelImportRespVO importOrderProductList(List<OrderProductImportExcelVO> list) {
        // 按 orderCode 分组
        Map<String, List<OrderProductImportExcelVO>> grouped = list.stream()
                .collect(Collectors.groupingBy(vo -> vo.getOrderCode() != null ? vo.getOrderCode() : ""));
        int success = 0, insert = 0, update = 0, fail = 0;
        Map<Integer, String> failureRows = new LinkedHashMap<>();
        int globalIdx = 0;
        for (Map.Entry<String, List<OrderProductImportExcelVO>> entry : grouped.entrySet()) {
            String orderCode = entry.getKey();
            if (StrUtil.isBlank(orderCode)) {
                for (OrderProductImportExcelVO vo : entry.getValue()) {
                    failureRows.put(globalIdx + 2, "订单号不能为空");
                    fail++;
                    globalIdx++;
                }
                continue;
            }
            OrderInfoDO order = orderInfoMapper.selectByOrderCode(orderCode);
            if (order == null) {
                for (OrderProductImportExcelVO vo : entry.getValue()) {
                    failureRows.put(globalIdx + 2, "订单号不存在: " + orderCode);
                    fail++;
                    globalIdx++;
                }
                continue;
            }
            // 物理删除该订单的所有产品明细后重建
            orderProductMapper.deletePhysicalByOrderCode(orderCode);
            for (OrderProductImportExcelVO vo : entry.getValue()) {
                int rowNo = globalIdx + 2;
                try {
                    if (StrUtil.isBlank(vo.getProductName())) throw new IllegalArgumentException("产品名称不能为空");
                    if (vo.getUnitPrice() == null) throw new IllegalArgumentException("单价不能为空");
                    if (vo.getQuantity() == null) throw new IllegalArgumentException("数量不能为空");
                    OrderProductDO product = new OrderProductDO().setOrderId(order.getId())
                            .setOrderCode(orderCode).setProductCode(vo.getProductCode())
                            .setProductName(vo.getProductName()).setSpecModel(vo.getSpecModel())
                            .setUnitPrice(vo.getUnitPrice()).setQuantity(vo.getQuantity())
                            .setUnit(vo.getUnit())
                            .setAmount(vo.getUnitPrice().multiply(new java.math.BigDecimal(vo.getQuantity())))
                            .setReturnableQty(vo.getQuantity());
                    orderProductMapper.insert(product);
                    insert++;
                    success++;
                } catch (Exception e) {
                    fail++;
                    failureRows.put(rowNo, e.getMessage());
                }
                globalIdx++;
            }
        }
        return ExcelImportRespVO.builder().successCount(success).insertCount(insert).updateCount(update)
                .failureCount(fail).failureRows(failureRows).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcelImportRespVO importOrderPaymentList(List<OrderPaymentImportExcelVO> list) {
        int success = 0, insert = 0, update = 0, fail = 0;
        Map<Integer, String> failureRows = new LinkedHashMap<>();
        // 缓存已查过的 order
        Map<String, OrderInfoDO> orderCache = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            int rowNo = i + 2;
            try {
                OrderPaymentImportExcelVO vo = list.get(i);
                if (StrUtil.isBlank(vo.getOrderCode())) throw new IllegalArgumentException("订单号不能为空");
                OrderInfoDO order = orderCache.computeIfAbsent(vo.getOrderCode(),
                        code -> orderInfoMapper.selectByOrderCode(code));
                if (order == null) throw new IllegalArgumentException("订单号不存在: " + vo.getOrderCode());
                OrderPaymentDO existing = null;
                if (StrUtil.isNotBlank(vo.getVoucherNo())) {
                    existing = orderPaymentMapper.selectByOrderCodeAndVoucherNo(vo.getOrderCode(), vo.getVoucherNo());
                }
                if (existing != null) {
                    existing.setPayAmount(vo.getPayAmount()).setPayDate(vo.getPayDate())
                            .setPayMethod(vo.getPayMethod()).setStatus(
                                    StrUtil.isNotBlank(vo.getStatus()) ? vo.getStatus() : existing.getStatus())
                            .setRemark(vo.getRemark());
                    orderPaymentMapper.updateById(existing);
                    update++;
                } else {
                    OrderPaymentDO payment = new OrderPaymentDO().setOrderId(order.getId())
                            .setOrderCode(vo.getOrderCode()).setPayAmount(vo.getPayAmount())
                            .setPayDate(vo.getPayDate()).setPayMethod(vo.getPayMethod())
                            .setVoucherNo(vo.getVoucherNo())
                            .setStatus(StrUtil.isNotBlank(vo.getStatus()) ? vo.getStatus() : "pending")
                            .setRemark(vo.getRemark());
                    orderPaymentMapper.insert(payment);
                    insert++;
                }
                success++;
            } catch (Exception e) {
                fail++;
                failureRows.put(rowNo, e.getMessage());
            }
        }
        return ExcelImportRespVO.builder().successCount(success).insertCount(insert).updateCount(update)
                .failureCount(fail).failureRows(failureRows).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcelImportRespVO importOrderInvoiceList(List<OrderInvoiceImportExcelVO> list) {
        int success = 0, insert = 0, update = 0, fail = 0;
        Map<Integer, String> failureRows = new LinkedHashMap<>();
        Map<String, OrderInfoDO> orderCache = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            int rowNo = i + 2;
            try {
                OrderInvoiceImportExcelVO vo = list.get(i);
                if (StrUtil.isBlank(vo.getOrderCode())) throw new IllegalArgumentException("订单号不能为空");
                OrderInfoDO order = orderCache.computeIfAbsent(vo.getOrderCode(),
                        code -> orderInfoMapper.selectByOrderCode(code));
                if (order == null) throw new IllegalArgumentException("订单号不存在: " + vo.getOrderCode());
                OrderInvoiceDO existing = null;
                if (StrUtil.isNotBlank(vo.getInvoiceNo())) {
                    existing = orderInvoiceMapper.selectByOrderCodeAndInvoiceNo(vo.getOrderCode(), vo.getInvoiceNo());
                }
                if (existing != null) {
                    existing.setInvoiceAmount(vo.getInvoiceAmount()).setInvoiceDate(vo.getInvoiceDate())
                            .setInvoiceType(vo.getInvoiceType()).setCompanyName(vo.getCompanyName())
                            .setTaxNo(vo.getTaxNo()).setSpecialRequest(vo.getSpecialRequest())
                            .setStatus(StrUtil.isNotBlank(vo.getStatus()) ? vo.getStatus() : existing.getStatus())
                            .setRemark(vo.getRemark());
                    orderInvoiceMapper.updateById(existing);
                    update++;
                } else {
                    OrderInvoiceDO invoice = new OrderInvoiceDO().setOrderId(order.getId())
                            .setOrderCode(vo.getOrderCode()).setInvoiceAmount(vo.getInvoiceAmount())
                            .setInvoiceNo(vo.getInvoiceNo()).setInvoiceDate(vo.getInvoiceDate())
                            .setInvoiceType(vo.getInvoiceType()).setCompanyName(vo.getCompanyName())
                            .setTaxNo(vo.getTaxNo()).setSpecialRequest(vo.getSpecialRequest())
                            .setStatus(StrUtil.isNotBlank(vo.getStatus()) ? vo.getStatus() : "pending")
                            .setRemark(vo.getRemark());
                    orderInvoiceMapper.insert(invoice);
                    insert++;
                }
                success++;
            } catch (Exception e) {
                fail++;
                failureRows.put(rowNo, e.getMessage());
            }
        }
        return ExcelImportRespVO.builder().successCount(success).insertCount(insert).updateCount(update)
                .failureCount(fail).failureRows(failureRows).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcelImportRespVO importOrderLogisticsList(List<OrderLogisticsImportExcelVO> list) {
        int success = 0, insert = 0, update = 0, fail = 0;
        Map<Integer, String> failureRows = new LinkedHashMap<>();
        Map<String, OrderInfoDO> orderCache = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            int rowNo = i + 2;
            try {
                OrderLogisticsImportExcelVO vo = list.get(i);
                if (StrUtil.isBlank(vo.getOrderCode())) throw new IllegalArgumentException("订单号不能为空");
                if (StrUtil.isBlank(vo.getNodeDesc())) throw new IllegalArgumentException("节点描述不能为空");
                OrderInfoDO order = orderCache.computeIfAbsent(vo.getOrderCode(),
                        code -> orderInfoMapper.selectByOrderCode(code));
                if (order == null) throw new IllegalArgumentException("订单号不存在: " + vo.getOrderCode());
                OrderLogisticsDO existing = null;
                if (StrUtil.isNotBlank(vo.getTrackingNo()) && vo.getSortOrder() != null) {
                    existing = orderLogisticsMapper.selectByOrderCodeAndTrackingNoAndSortOrder(
                            vo.getOrderCode(), vo.getTrackingNo(), vo.getSortOrder());
                }
                if (existing != null) {
                    existing.setLogisticsCompany(vo.getLogisticsCompany()).setNodeDesc(vo.getNodeDesc())
                            .setNodeTime(vo.getNodeTime()).setIsCompleted(parseBoolean(vo.getIsCompleted()))
                            .setSortOrder(vo.getSortOrder());
                    orderLogisticsMapper.updateById(existing);
                    update++;
                } else {
                    OrderLogisticsDO logistics = new OrderLogisticsDO().setOrderId(order.getId())
                            .setOrderCode(vo.getOrderCode()).setLogisticsCompany(vo.getLogisticsCompany())
                            .setTrackingNo(vo.getTrackingNo()).setNodeDesc(vo.getNodeDesc())
                            .setNodeTime(vo.getNodeTime()).setIsCompleted(parseBoolean(vo.getIsCompleted()))
                            .setSortOrder(vo.getSortOrder());
                    orderLogisticsMapper.insert(logistics);
                    insert++;
                }
                success++;
            } catch (Exception e) {
                fail++;
                failureRows.put(rowNo, e.getMessage());
            }
        }
        return ExcelImportRespVO.builder().successCount(success).insertCount(insert).updateCount(update)
                .failureCount(fail).failureRows(failureRows).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcelImportRespVO importOrderTimelineList(List<OrderTimelineImportExcelVO> list) {
        int success = 0, insert = 0, update = 0, fail = 0;
        Map<Integer, String> failureRows = new LinkedHashMap<>();
        Map<String, OrderInfoDO> orderCache = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            int rowNo = i + 2;
            try {
                OrderTimelineImportExcelVO vo = list.get(i);
                if (StrUtil.isBlank(vo.getOrderCode())) throw new IllegalArgumentException("订单号不能为空");
                if (StrUtil.isBlank(vo.getNodeCode())) throw new IllegalArgumentException("节点编码不能为空");
                OrderInfoDO order = orderCache.computeIfAbsent(vo.getOrderCode(),
                        code -> orderInfoMapper.selectByOrderCode(code));
                if (order == null) throw new IllegalArgumentException("订单号不存在: " + vo.getOrderCode());
                OrderTimelineDO existing = orderTimelineMapper.selectByOrderCodeAndNodeCode(
                        vo.getOrderCode(), vo.getNodeCode());
                if (existing != null) {
                    existing.setNodeName(vo.getNodeName()).setNodeTime(vo.getNodeTime())
                            .setIsCompleted(parseBoolean(vo.getIsCompleted())).setSortOrder(vo.getSortOrder());
                    orderTimelineMapper.updateById(existing);
                    update++;
                } else {
                    OrderTimelineDO timeline = new OrderTimelineDO().setOrderId(order.getId())
                            .setOrderCode(vo.getOrderCode()).setNodeCode(vo.getNodeCode())
                            .setNodeName(vo.getNodeName()).setNodeTime(vo.getNodeTime())
                            .setIsCompleted(parseBoolean(vo.getIsCompleted())).setSortOrder(vo.getSortOrder());
                    orderTimelineMapper.insert(timeline);
                    insert++;
                }
                success++;
            } catch (Exception e) {
                fail++;
                failureRows.put(rowNo, e.getMessage());
            }
        }
        return ExcelImportRespVO.builder().successCount(success).insertCount(insert).updateCount(update)
                .failureCount(fail).failureRows(failureRows).build();
    }

    // ========== L4 售后 + 基础数据文件 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcelImportRespVO importAfterSaleList(List<AfterSaleInfoImportExcelVO> list) {
        Map<String, DealerInfoDO> dealerMap = dealerInfoMapper.selectList().stream()
                .collect(Collectors.toMap(DealerInfoDO::getDealerCode, d -> d, (a, b) -> a));
        Map<String, DealerProductLineDO> plMap = productLineMapper.selectList().stream()
                .collect(Collectors.toMap(DealerProductLineDO::getProductLineCode, d -> d, (a, b) -> a));
        int success = 0, insert = 0, update = 0, fail = 0;
        Map<Integer, String> failureRows = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            int rowNo = i + 2;
            try {
                AfterSaleInfoImportExcelVO vo = list.get(i);
                if (StrUtil.isBlank(vo.getAftersaleCode())) throw new IllegalArgumentException("售后单号不能为空");
                if (StrUtil.isBlank(vo.getDealerCode())) throw new IllegalArgumentException("经销商编码不能为空");
                if (StrUtil.isBlank(vo.getHandlingMethod())) throw new IllegalArgumentException("处理方式不能为空");
                if (StrUtil.isBlank(vo.getReason())) throw new IllegalArgumentException("售后原因不能为空");
                DealerInfoDO dealer = dealerMap.get(vo.getDealerCode());
                if (dealer == null) throw new IllegalArgumentException("经销商编码不存在: " + vo.getDealerCode());
                DealerProductLineDO pl = null;
                if (StrUtil.isNotBlank(vo.getProductLineCode())) {
                    pl = plMap.get(vo.getProductLineCode());
                }
                AfterSaleInfoDO existing = afterSaleInfoMapper.selectByAftersaleCode(vo.getAftersaleCode());
                if (existing != null) {
                    existing.setDealerId(dealer.getId()).setDealerCode(vo.getDealerCode())
                            .setDealerName(dealer.getDealerName())
                            .setProductLineCode(vo.getProductLineCode())
                            .setProductLineName(pl != null ? pl.getProductLineName() : null)
                            .setOrderCode(vo.getOrderCode()).setHandlingMethod(vo.getHandlingMethod())
                            .setReason(vo.getReason())
                            .setProgressStatus(StrUtil.isNotBlank(vo.getProgressStatus()) ? vo.getProgressStatus() : existing.getProgressStatus())
                            .setProductName(vo.getProductName()).setProductSpec(vo.getProductSpec())
                            .setQuantity(vo.getQuantity()).setRefundAmount(vo.getRefundAmount())
                            .setLogisticsCompany(vo.getLogisticsCompany()).setLogisticsNo(vo.getLogisticsNo())
                            .setExchangeLogisticsCompany(vo.getExchangeLogisticsCompany())
                            .setExchangeLogisticsNo(vo.getExchangeLogisticsNo()).setRemark(vo.getRemark());
                    afterSaleInfoMapper.updateById(existing);
                    update++;
                } else {
                    AfterSaleInfoDO as = new AfterSaleInfoDO().setAftersaleCode(vo.getAftersaleCode())
                            .setDealerId(dealer.getId()).setDealerCode(vo.getDealerCode())
                            .setDealerName(dealer.getDealerName())
                            .setProductLineCode(vo.getProductLineCode())
                            .setProductLineName(pl != null ? pl.getProductLineName() : null)
                            .setOrderCode(vo.getOrderCode()).setHandlingMethod(vo.getHandlingMethod())
                            .setReason(vo.getReason())
                            .setProgressStatus(StrUtil.isNotBlank(vo.getProgressStatus()) ? vo.getProgressStatus() : "pending")
                            .setCurrentStep(0).setProductName(vo.getProductName()).setProductSpec(vo.getProductSpec())
                            .setQuantity(vo.getQuantity()).setRefundAmount(vo.getRefundAmount())
                            .setLogisticsCompany(vo.getLogisticsCompany()).setLogisticsNo(vo.getLogisticsNo())
                            .setExchangeLogisticsCompany(vo.getExchangeLogisticsCompany())
                            .setExchangeLogisticsNo(vo.getExchangeLogisticsNo()).setRemark(vo.getRemark());
                    afterSaleInfoMapper.insert(as);
                    insert++;
                }
                success++;
            } catch (Exception e) {
                fail++;
                failureRows.put(rowNo, e.getMessage());
            }
        }
        return ExcelImportRespVO.builder().successCount(success).insertCount(insert).updateCount(update)
                .failureCount(fail).failureRows(failureRows).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcelImportRespVO importAfterSaleProgressList(List<AfterSaleProgressImportExcelVO> list) {
        int success = 0, insert = 0, update = 0, fail = 0;
        Map<Integer, String> failureRows = new LinkedHashMap<>();
        Map<String, AfterSaleInfoDO> asCache = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            int rowNo = i + 2;
            try {
                AfterSaleProgressImportExcelVO vo = list.get(i);
                if (StrUtil.isBlank(vo.getAftersaleCode())) throw new IllegalArgumentException("售后单号不能为空");
                if (StrUtil.isBlank(vo.getNodeCode())) throw new IllegalArgumentException("节点编码不能为空");
                AfterSaleInfoDO as = asCache.computeIfAbsent(vo.getAftersaleCode(),
                        code -> afterSaleInfoMapper.selectByAftersaleCode(code));
                if (as == null) throw new IllegalArgumentException("售后单号不存在: " + vo.getAftersaleCode());
                AfterSaleProgressDO existing = afterSaleProgressMapper.selectByAftersaleCodeAndNodeCode(
                        vo.getAftersaleCode(), vo.getNodeCode());
                if (existing != null) {
                    existing.setNodeName(vo.getNodeName()).setNodeTime(vo.getNodeTime())
                            .setIsCompleted(parseBoolean(vo.getIsCompleted())).setSortOrder(vo.getSortOrder())
                            .setRemark(vo.getRemark());
                    afterSaleProgressMapper.updateById(existing);
                    update++;
                } else {
                    AfterSaleProgressDO progress = new AfterSaleProgressDO().setAftersaleId(as.getId())
                            .setAftersaleCode(vo.getAftersaleCode()).setNodeCode(vo.getNodeCode())
                            .setNodeName(vo.getNodeName()).setNodeTime(vo.getNodeTime())
                            .setIsCompleted(parseBoolean(vo.getIsCompleted())).setSortOrder(vo.getSortOrder())
                            .setRemark(vo.getRemark());
                    afterSaleProgressMapper.insert(progress);
                    insert++;
                }
                success++;
            } catch (Exception e) {
                fail++;
                failureRows.put(rowNo, e.getMessage());
            }
        }
        return ExcelImportRespVO.builder().successCount(success).insertCount(insert).updateCount(update)
                .failureCount(fail).failureRows(failureRows).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcelImportRespVO importBasedataFileList(List<BasedataFileImportExcelVO> list) {
        Map<String, DealerInfoDO> dealerMap = dealerInfoMapper.selectList().stream()
                .collect(Collectors.toMap(DealerInfoDO::getDealerCode, d -> d, (a, b) -> a));
        int success = 0, insert = 0, update = 0, fail = 0;
        Map<Integer, String> failureRows = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            int rowNo = i + 2;
            try {
                BasedataFileImportExcelVO vo = list.get(i);
                if (StrUtil.isBlank(vo.getDealerCode())) throw new IllegalArgumentException("经销商编码不能为空");
                if (StrUtil.isBlank(vo.getCategory())) throw new IllegalArgumentException("文件分类不能为空");
                if (StrUtil.isBlank(vo.getFileName())) throw new IllegalArgumentException("文件名称不能为空");
                if (StrUtil.isBlank(vo.getFileUrl())) throw new IllegalArgumentException("文件地址不能为空");
                DealerInfoDO dealer = dealerMap.get(vo.getDealerCode());
                if (dealer == null) throw new IllegalArgumentException("经销商编码不存在: " + vo.getDealerCode());
                BasedataFileDO existing = basedataFileMapper.selectByDealerCodeAndFileNameAndCategory(
                        vo.getDealerCode(), vo.getFileName(), vo.getCategory());
                if (existing != null) {
                    existing.setDealerId(dealer.getId()).setFileType(vo.getFileType())
                            .setFileUrl(vo.getFileUrl()).setExpireDate(vo.getExpireDate())
                            .setStatus(parseInteger(vo.getStatus())).setDescription(vo.getDescription())
                            .setRemark(vo.getRemark());
                    basedataFileMapper.updateById(existing);
                    update++;
                } else {
                    BasedataFileDO file = new BasedataFileDO().setDealerId(dealer.getId())
                            .setDealerCode(vo.getDealerCode()).setCategory(vo.getCategory())
                            .setFileName(vo.getFileName()).setFileType(vo.getFileType())
                            .setFileUrl(vo.getFileUrl()).setExpireDate(vo.getExpireDate())
                            .setStatus(parseInteger(vo.getStatus()) != null ? parseInteger(vo.getStatus()) : 0)
                            .setDescription(vo.getDescription()).setRemark(vo.getRemark());
                    basedataFileMapper.insert(file);
                    insert++;
                }
                success++;
            } catch (Exception e) {
                fail++;
                failureRows.put(rowNo, e.getMessage());
            }
        }
        return ExcelImportRespVO.builder().successCount(success).insertCount(insert).updateCount(update)
                .failureCount(fail).failureRows(failureRows).build();
    }

    // ========== 政策看板 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcelImportRespVO importPolicyList(List<PolicyImportExcelVO> list) {
        Map<String, DealerPolicyDO> existingMap = dealerPolicyMapper.selectList().stream()
                .collect(Collectors.toMap(DealerPolicyDO::getPolicyCode, d -> d, (a, b) -> a));
        Map<String, DealerInfoDO> dealerMap = dealerInfoMapper.selectList().stream()
                .collect(Collectors.toMap(DealerInfoDO::getDealerCode, d -> d, (a, b) -> a));
        Map<String, SigningContractDO> contractMap = contractMapper.selectList().stream()
                .filter(c -> StrUtil.isNotBlank(c.getContractCode()))
                .collect(Collectors.toMap(SigningContractDO::getContractCode, c -> c, (a, b) -> a));
        int success = 0, insert = 0, update = 0, fail = 0;
        Map<Integer, String> failureRows = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            int rowNo = i + 2;
            try {
                PolicyImportExcelVO vo = list.get(i);
                if (StrUtil.isBlank(vo.getPolicyCode())) throw new IllegalArgumentException("政策编码不能为空");
                if (StrUtil.isBlank(vo.getPolicyName())) throw new IllegalArgumentException("政策名称不能为空");
                if (StrUtil.isBlank(vo.getDealerCode())) throw new IllegalArgumentException("经销商编码不能为空");
                DealerInfoDO dealer = dealerMap.get(vo.getDealerCode());
                if (dealer == null) throw new IllegalArgumentException("经销商编码不存在: " + vo.getDealerCode());
                Long sourceContractId = null;
                if (StrUtil.isNotBlank(vo.getContractCode())) {
                    SigningContractDO contract = contractMap.get(vo.getContractCode());
                    if (contract == null) throw new IllegalArgumentException("来源合同编码不存在: " + vo.getContractCode());
                    sourceContractId = contract.getId();
                }
                DealerPolicyDO existing = existingMap.get(vo.getPolicyCode());
                if (existing != null) {
                    existing.setDealerId(dealer.getId()).setDealerCode(vo.getDealerCode())
                            .setProductLineCode(vo.getProductLineCode()).setProductLineName(vo.getProductLineName())
                            .setPolicyName(vo.getPolicyName()).setPolicyType(vo.getPolicyType())
                            .setAchievementType(vo.getAchievementType()).setPolicyStatus(vo.getPolicyStatus())
                            .setContractCode(vo.getContractCode()).setContractName(vo.getContractName())
                            .setPolicyDesc(vo.getPolicyDesc()).setSourceContractId(sourceContractId);
                    dealerPolicyMapper.updateById(existing);
                    update++;
                } else {
                    DealerPolicyDO policy = new DealerPolicyDO();
                    policy.setDealerId(dealer.getId()).setDealerCode(vo.getDealerCode())
                            .setProductLineCode(vo.getProductLineCode()).setProductLineName(vo.getProductLineName())
                            .setPolicyCode(vo.getPolicyCode()).setPolicyName(vo.getPolicyName())
                            .setPolicyType(vo.getPolicyType()).setAchievementType(vo.getAchievementType())
                            .setPolicyStatus(vo.getPolicyStatus()).setContractCode(vo.getContractCode())
                            .setContractName(vo.getContractName()).setPolicyDesc(vo.getPolicyDesc())
                            .setSourceContractId(sourceContractId);
                    dealerPolicyMapper.insert(policy);
                    existingMap.put(vo.getPolicyCode(), policy);
                    insert++;
                }
                success++;
            } catch (Exception e) {
                fail++;
                failureRows.put(rowNo, e.getMessage());
            }
        }
        return ExcelImportRespVO.builder().successCount(success).insertCount(insert).updateCount(update)
                .failureCount(fail).failureRows(failureRows).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcelImportRespVO importPolicyIndicatorList(List<PolicyIndicatorImportExcelVO> list) {
        Map<String, DealerPolicyDO> policyMap = dealerPolicyMapper.selectList().stream()
                .collect(Collectors.toMap(DealerPolicyDO::getPolicyCode, d -> d, (a, b) -> a));
        Map<String, DealerPolicyIndicatorDO> existingMap = policyIndicatorMapper.selectList().stream()
                .collect(Collectors.toMap(
                        ind -> ind.getPolicyCode() + "|" + ind.getIndicatorName() + "|" + ind.getTargetYear() + "|" + ind.getTargetMonth(),
                        d -> d, (a, b) -> a));
        int success = 0, insert = 0, update = 0, fail = 0;
        Map<Integer, String> failureRows = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            int rowNo = i + 2;
            try {
                PolicyIndicatorImportExcelVO vo = list.get(i);
                if (StrUtil.isBlank(vo.getPolicyCode())) throw new IllegalArgumentException("政策编码不能为空");
                if (StrUtil.isBlank(vo.getIndicatorName())) throw new IllegalArgumentException("指标名称不能为空");
                if (vo.getTargetYear() == null) throw new IllegalArgumentException("年度不能为空");
                if (vo.getTargetMonth() == null) throw new IllegalArgumentException("月份不能为空");
                if (vo.getTargetValue() == null) throw new IllegalArgumentException("目标值不能为空");
                DealerPolicyDO policy = policyMap.get(vo.getPolicyCode());
                if (policy == null) throw new IllegalArgumentException("政策编码不存在: " + vo.getPolicyCode());
                String key = vo.getPolicyCode() + "|" + vo.getIndicatorName() + "|" + vo.getTargetYear() + "|" + vo.getTargetMonth();
                DealerPolicyIndicatorDO existing = existingMap.get(key);
                if (existing != null) {
                    existing.setPolicyId(policy.getId()).setPolicyCode(vo.getPolicyCode())
                            .setIndicatorName(vo.getIndicatorName()).setTargetYear(vo.getTargetYear()).setTargetMonth(vo.getTargetMonth())
                            .setTargetValue(vo.getTargetValue())
                            .setAchievedValue(vo.getAchievedValue() != null ? vo.getAchievedValue() : existing.getAchievedValue())
                            .setUnit(vo.getUnit());
                    policyIndicatorMapper.updateById(existing);
                    update++;
                } else {
                    DealerPolicyIndicatorDO ind = new DealerPolicyIndicatorDO();
                    ind.setPolicyId(policy.getId()).setPolicyCode(vo.getPolicyCode())
                            .setIndicatorName(vo.getIndicatorName()).setTargetYear(vo.getTargetYear()).setTargetMonth(vo.getTargetMonth())
                            .setTargetValue(vo.getTargetValue())
                            .setAchievedValue(vo.getAchievedValue() != null ? vo.getAchievedValue() : java.math.BigDecimal.ZERO)
                            .setUnit(vo.getUnit());
                    policyIndicatorMapper.insert(ind);
                    existingMap.put(key, ind);
                    insert++;
                }
                success++;
            } catch (Exception e) {
                fail++;
                failureRows.put(rowNo, e.getMessage());
            }
        }
        return ExcelImportRespVO.builder().successCount(success).insertCount(insert).updateCount(update)
                .failureCount(fail).failureRows(failureRows).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcelImportRespVO importPolicyAchievementList(List<PolicyAchievementImportExcelVO> list) {
        // 预加载指标（按 policyCode+indicatorName+targetYear+targetMonth 定位）
        Map<String, DealerPolicyIndicatorDO> indicatorMap = policyIndicatorMapper.selectList().stream()
                .collect(Collectors.toMap(
                        ind -> ind.getPolicyCode() + "|" + ind.getIndicatorName() + "|" + ind.getTargetYear() + "|" + ind.getTargetMonth(),
                        d -> d, (a, b) -> a));
        Map<String, DealerPolicyAchievementDO> existingMap = policyAchievementMapper.selectList().stream()
                .collect(Collectors.toMap(
                        a -> a.getTargetYear() + "|" + a.getIndicatorId() + "|" + a.getAchieveLevel() + "|" +
                                StrUtil.nullToEmpty(a.getProvinceCode()) + "|" +
                                StrUtil.nullToEmpty(a.getHospitalCode()) + "|" +
                                StrUtil.nullToEmpty(a.getProductName()),
                        d -> d, (a, b) -> a));
        int success = 0, insert = 0, update = 0, fail = 0;
        Map<Integer, String> failureRows = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            int rowNo = i + 2;
            try {
                PolicyAchievementImportExcelVO vo = list.get(i);
                if (StrUtil.isBlank(vo.getPolicyCode())) throw new IllegalArgumentException("政策编码不能为空");
                if (StrUtil.isBlank(vo.getIndicatorName())) throw new IllegalArgumentException("指标名称不能为空");
                if (vo.getTargetYear() == null) throw new IllegalArgumentException("年度不能为空");
                if (vo.getTargetMonth() == null) throw new IllegalArgumentException("月份不能为空");
                if (StrUtil.isBlank(vo.getAchieveLevel())) throw new IllegalArgumentException("层级不能为空");
                if (vo.getAchievedValue() == null) throw new IllegalArgumentException("达成值不能为空");
                String indKey = vo.getPolicyCode() + "|" + vo.getIndicatorName() + "|" + vo.getTargetYear() + "|" + vo.getTargetMonth();
                DealerPolicyIndicatorDO indicator = indicatorMap.get(indKey);
                if (indicator == null) throw new IllegalArgumentException("未找到对应指标: " + indKey);
                String existKey = vo.getTargetYear() + "|" + indicator.getId() + "|" + vo.getAchieveLevel() + "|" +
                        StrUtil.nullToEmpty(vo.getProvinceCode()) + "|" +
                        StrUtil.nullToEmpty(vo.getHospitalCode()) + "|" +
                        StrUtil.nullToEmpty(vo.getProductName());
                DealerPolicyAchievementDO existing = existingMap.get(existKey);
                if (existing != null) {
                    existing.setProvince(vo.getProvince()).setProvinceCode(vo.getProvinceCode())
                            .setHospital(vo.getHospital()).setHospitalCode(vo.getHospitalCode())
                            .setProductName(vo.getProductName()).setAchievedValue(vo.getAchievedValue());
                    policyAchievementMapper.updateById(existing);
                    update++;
                } else {
                    DealerPolicyAchievementDO ach = new DealerPolicyAchievementDO();
                    ach.setIndicatorId(indicator.getId()).setIndicatorName(vo.getIndicatorName())
                            .setTargetYear(vo.getTargetYear())
                            .setAchieveLevel(vo.getAchieveLevel())
                            .setProvince(vo.getProvince()).setProvinceCode(vo.getProvinceCode())
                            .setHospital(vo.getHospital()).setHospitalCode(vo.getHospitalCode())
                            .setProductName(vo.getProductName()).setAchievedValue(vo.getAchievedValue());
                    policyAchievementMapper.insert(ach);
                    existingMap.put(existKey, ach);
                    insert++;
                }
                success++;
            } catch (Exception e) {
                fail++;
                failureRows.put(rowNo, e.getMessage());
            }
        }
        return ExcelImportRespVO.builder().successCount(success).insertCount(insert).updateCount(update)
                .failureCount(fail).failureRows(failureRows).build();
    }

    // ========== 辅助方法 ==========

    private Integer parseInteger(String value) {
        if (StrUtil.isBlank(value)) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Boolean parseBoolean(String value) {
        if (StrUtil.isBlank(value)) return false;
        return "true".equalsIgnoreCase(value);
    }
}
