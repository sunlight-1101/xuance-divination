package com.zhexuan.divination.vo;

import java.math.BigDecimal;
import java.util.List;

public class QuotaVO {
    private Long userId;
    private String role;
    private Integer freeTotal;
    private Integer freeUsed;
    private Integer freeRemaining;
    private Integer paidCredits;
    private Integer availableCredits;
    private Integer totalUsed;
    private BigDecimal totalRechargeAmount;
    private List<RechargePackageVO> packages;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Integer getFreeTotal() { return freeTotal; }
    public void setFreeTotal(Integer freeTotal) { this.freeTotal = freeTotal; }
    public Integer getFreeUsed() { return freeUsed; }
    public void setFreeUsed(Integer freeUsed) { this.freeUsed = freeUsed; }
    public Integer getFreeRemaining() { return freeRemaining; }
    public void setFreeRemaining(Integer freeRemaining) { this.freeRemaining = freeRemaining; }
    public Integer getPaidCredits() { return paidCredits; }
    public void setPaidCredits(Integer paidCredits) { this.paidCredits = paidCredits; }
    public Integer getAvailableCredits() { return availableCredits; }
    public void setAvailableCredits(Integer availableCredits) { this.availableCredits = availableCredits; }
    public Integer getTotalUsed() { return totalUsed; }
    public void setTotalUsed(Integer totalUsed) { this.totalUsed = totalUsed; }
    public BigDecimal getTotalRechargeAmount() { return totalRechargeAmount; }
    public void setTotalRechargeAmount(BigDecimal totalRechargeAmount) { this.totalRechargeAmount = totalRechargeAmount; }
    public List<RechargePackageVO> getPackages() { return packages; }
    public void setPackages(List<RechargePackageVO> packages) { this.packages = packages; }
}
