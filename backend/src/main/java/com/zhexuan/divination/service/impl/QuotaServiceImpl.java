package com.zhexuan.divination.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.zhexuan.divination.common.BizException;
import com.zhexuan.divination.dto.RechargeDTO;
import com.zhexuan.divination.entity.RechargeRecord;
import com.zhexuan.divination.entity.User;
import com.zhexuan.divination.entity.UserQuota;
import com.zhexuan.divination.mapper.RechargeRecordMapper;
import com.zhexuan.divination.mapper.UserMapper;
import com.zhexuan.divination.mapper.UserQuotaMapper;
import com.zhexuan.divination.service.QuotaService;
import com.zhexuan.divination.vo.PayOrderVO;
import com.zhexuan.divination.vo.QuotaVO;
import com.zhexuan.divination.vo.RechargePackageVO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class QuotaServiceImpl implements QuotaService {
    private static final int FREE_TOTAL = 5;
    private static final List<RechargePackageVO> PACKAGES = Arrays.asList(
            new RechargePackageVO("SINGLE_025", "0.25元/1次", new BigDecimal("0.25"), 1),
            new RechargePackageVO("P1_5", "1元/5次", new BigDecimal("1.00"), 5),
            new RechargePackageVO("P3_18", "3元/18次", new BigDecimal("3.00"), 18),
            new RechargePackageVO("P5_30", "5元/30次", new BigDecimal("5.00"), 30),
            new RechargePackageVO("P10_70", "10元/70次", new BigDecimal("10.00"), 70)
    );

    private final UserMapper userMapper;
    private final UserQuotaMapper quotaMapper;
    private final RechargeRecordMapper rechargeRecordMapper;

    @Value("${alipay.enabled:false}")
    private boolean alipayEnabled;
    @Value("${alipay.gateway-url:https://openapi.alipay.com/gateway.do}")
    private String alipayGatewayUrl;
    @Value("${alipay.app-id:}")
    private String alipayAppId;
    @Value("${alipay.private-key:}")
    private String alipayPrivateKey;
    @Value("${alipay.alipay-public-key:}")
    private String alipayPublicKey;
    @Value("${alipay.notify-url:}")
    private String alipayNotifyUrl;
    @Value("${alipay.return-url:}")
    private String alipayReturnUrl;
    @Value("${alipay.sign-type:RSA2}")
    private String alipaySignType;
    @Value("${alipay.charset:utf-8}")
    private String alipayCharset;

    public QuotaServiceImpl(UserMapper userMapper, UserQuotaMapper quotaMapper, RechargeRecordMapper rechargeRecordMapper) {
        this.userMapper = userMapper;
        this.quotaMapper = quotaMapper;
        this.rechargeRecordMapper = rechargeRecordMapper;
    }

    @Override
    public QuotaVO getQuota(Long userId) {
        User user = requireUser(userId);
        UserQuota quota = ensureQuota(userId);
        return toVO(user, quota);
    }

    @Override
    @Transactional
    public QuotaVO recharge(RechargeDTO dto) {
        throw new BizException("请使用支付宝支付，到账后系统会自动增加次数");
    }

    @Override
    @Transactional
    public void consumeForAnalysis(Long userId, String type) {
        User user = requireUser(userId);
        UserQuota quota = ensureQuota(userId);
        if (!"ADMIN".equalsIgnoreCase(user.getRole()) && value(quota.getFreeUsed()) < FREE_TOTAL) {
            quota.setFreeUsed(value(quota.getFreeUsed()) + 1);
        }
        quota.setTotalUsed(value(quota.getTotalUsed()) + 1);
        quota.setUpdateTime(LocalDateTime.now());
        quotaMapper.updateById(quota);
    }

    @Override
    @Transactional
    public PayOrderVO createAlipayOrder(RechargeDTO dto) {
        if (!alipayEnabled) {
            throw new BizException("支付宝支付暂未启用");
        }
        if (!StringUtils.hasText(alipayAppId) || !StringUtils.hasText(alipayPrivateKey)
                || !StringUtils.hasText(alipayPublicKey) || !StringUtils.hasText(alipayNotifyUrl)) {
            throw new BizException("支付宝配置不完整");
        }
        if (dto == null || dto.getUserId() == null) {
            throw new BizException("请先登录后再充值");
        }
        User user = requireUser(dto.getUserId());
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new BizException("管理员账号无需充值");
        }
        RechargePackageVO pack = findPackage(dto.getPackageCode());
        int quantity = normalizeQuantity(dto.getQuantity());
        int credits = pack.getCredits() * quantity;
        BigDecimal amount = pack.getAmount().multiply(new BigDecimal(quantity)).setScale(2, RoundingMode.HALF_UP);

        RechargeRecord record = new RechargeRecord();
        record.setUserId(dto.getUserId());
        record.setPackageCode(pack.getCode());
        record.setQuantity(quantity);
        record.setAmount(amount);
        record.setCredits(credits);
        record.setStatus("WAIT_PAY");
        record.setOutTradeNo(buildOutTradeNo());
        record.setPayChannel("ALIPAY");
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        rechargeRecordMapper.insert(record);

        PayOrderVO vo = new PayOrderVO();
        vo.setOutTradeNo(record.getOutTradeNo());
        vo.setAmount(amount);
        vo.setCredits(credits);
        vo.setPayForm(buildAlipayForm(record, pack));
        return vo;
    }

    @Override
    @Transactional
    public String handleAlipayNotify(Map<String, String> params) {
        try {
            boolean verified = AlipaySignature.rsaCheckV1(params, alipayPublicKey, alipayCharset, alipaySignType);
            if (!verified) {
                return "failure";
            }
        } catch (AlipayApiException e) {
            return "failure";
        }
        String outTradeNo = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");
        if (!StringUtils.hasText(outTradeNo) || (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus))) {
            return "success";
        }
        RechargeRecord record = rechargeRecordMapper.selectOne(new LambdaQueryWrapper<RechargeRecord>()
                .eq(RechargeRecord::getOutTradeNo, outTradeNo));
        if (record == null) {
            return "failure";
        }
        if ("PAID".equalsIgnoreCase(record.getStatus())) {
            return "success";
        }
        BigDecimal paidAmount = parseAmount(params.get("total_amount"));
        if (paidAmount == null || paidAmount.compareTo(record.getAmount().setScale(2, RoundingMode.HALF_UP)) != 0) {
            record.setStatus("AMOUNT_MISMATCH");
            record.setUpdateTime(LocalDateTime.now());
            rechargeRecordMapper.updateById(record);
            return "failure";
        }
        UserQuota quota = ensureQuota(record.getUserId());
        quota.setPaidCredits(value(quota.getPaidCredits()) + value(record.getCredits()));
        quota.setTotalRechargeAmount(amount(quota.getTotalRechargeAmount()).add(record.getAmount()));
        quota.setUpdateTime(LocalDateTime.now());
        quotaMapper.updateById(quota);

        record.setStatus("PAID");
        record.setTradeNo(params.get("trade_no"));
        record.setPaidTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        rechargeRecordMapper.updateById(record);
        return "success";
    }

    private User requireUser(Long userId) {
        if (userId == null) {
            throw new BizException("请先登录后再分析");
        }
        User user = userMapper.selectById(userId);
        if (user == null || (user.getStatus() != null && user.getStatus() == 0)) {
            throw new BizException("账号不存在或已停用");
        }
        return user;
    }

    private UserQuota ensureQuota(Long userId) {
        UserQuota quota = quotaMapper.selectOne(new LambdaQueryWrapper<UserQuota>().eq(UserQuota::getUserId, userId));
        if (quota != null) {
            return quota;
        }
        quota = new UserQuota();
        quota.setUserId(userId);
        quota.setFreeUsed(0);
        quota.setPaidCredits(0);
        quota.setTotalUsed(0);
        quota.setTotalRechargeAmount(BigDecimal.ZERO);
        quota.setCreateTime(LocalDateTime.now());
        quota.setUpdateTime(LocalDateTime.now());
        quotaMapper.insert(quota);
        return quota;
    }

    private RechargePackageVO findPackage(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BizException("请选择充值套餐");
        }
        return PACKAGES.stream()
                .filter(item -> item.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new BizException("充值套餐不存在"));
    }

    private int normalizeQuantity(Integer quantityValue) {
        int quantity = quantityValue == null || quantityValue < 1 ? 1 : quantityValue;
        if (quantity > 99) {
            throw new BizException("单次充值数量过大");
        }
        return quantity;
    }

    private String buildOutTradeNo() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        return "ZX" + time + random;
    }

    private String buildAlipayForm(RechargeRecord record, RechargePackageVO pack) {
        AlipayClient client = new DefaultAlipayClient(
                alipayGatewayUrl,
                alipayAppId,
                alipayPrivateKey,
                "json",
                alipayCharset,
                alipayPublicKey,
                alipaySignType);
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setNotifyUrl(alipayNotifyUrl);
        if (StringUtils.hasText(alipayReturnUrl)) {
            request.setReturnUrl(alipayReturnUrl);
        }
        String subject = "哲玄额度充值-" + pack.getName();
        String bizContent = "{"
                + "\"out_trade_no\":\"" + record.getOutTradeNo() + "\","
                + "\"total_amount\":\"" + record.getAmount().setScale(2, RoundingMode.HALF_UP) + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"product_code\":\"QUICK_WAP_WAY\""
                + "}";
        request.setBizContent(bizContent);
        try {
            AlipayTradeWapPayResponse response = client.pageExecute(request);
            if (!response.isSuccess() && !StringUtils.hasText(response.getBody())) {
                throw new BizException("支付宝下单失败：" + response.getSubMsg());
            }
            return response.getBody();
        } catch (AlipayApiException e) {
            throw new BizException("支付宝下单失败：" + e.getMessage());
        }
    }

    private BigDecimal parseAmount(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private QuotaVO toVO(User user, UserQuota quota) {
        int freeUsed = value(quota.getFreeUsed());
        int freeRemaining = Math.max(0, FREE_TOTAL - freeUsed);
        int paidCredits = value(quota.getPaidCredits());
        QuotaVO vo = new QuotaVO();
        vo.setUserId(user.getId());
        vo.setRole(user.getRole());
        vo.setFreeTotal(FREE_TOTAL);
        vo.setFreeUsed(freeUsed);
        vo.setFreeRemaining(freeRemaining);
        vo.setPaidCredits(paidCredits);
        vo.setAvailableCredits(999999);
        vo.setTotalUsed(value(quota.getTotalUsed()));
        vo.setTotalRechargeAmount(amount(quota.getTotalRechargeAmount()));
        vo.setPackages(PACKAGES);
        return vo;
    }

    private int value(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal amount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
