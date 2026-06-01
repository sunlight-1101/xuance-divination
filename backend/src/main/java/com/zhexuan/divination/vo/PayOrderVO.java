package com.zhexuan.divination.vo;

import java.math.BigDecimal;

public class PayOrderVO {
    private String outTradeNo;
    private BigDecimal amount;
    private Integer credits;
    private String payForm;

    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
    public String getPayForm() { return payForm; }
    public void setPayForm(String payForm) { this.payForm = payForm; }
}
