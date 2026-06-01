package com.zhexuan.divination.vo;

import java.math.BigDecimal;

public class RechargePackageVO {
    private String code;
    private String name;
    private BigDecimal amount;
    private Integer credits;

    public RechargePackageVO(String code, String name, BigDecimal amount, Integer credits) {
        this.code = code;
        this.name = name;
        this.amount = amount;
        this.credits = credits;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
}
