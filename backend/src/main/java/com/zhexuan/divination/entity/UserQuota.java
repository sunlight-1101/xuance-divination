package com.zhexuan.divination.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("user_quota")
public class UserQuota {
    private Long id;
    private Long userId;
    private Integer freeUsed;
    private Integer paidCredits;
    private Integer totalUsed;
    private BigDecimal totalRechargeAmount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getFreeUsed() { return freeUsed; }
    public void setFreeUsed(Integer freeUsed) { this.freeUsed = freeUsed; }
    public Integer getPaidCredits() { return paidCredits; }
    public void setPaidCredits(Integer paidCredits) { this.paidCredits = paidCredits; }
    public Integer getTotalUsed() { return totalUsed; }
    public void setTotalUsed(Integer totalUsed) { this.totalUsed = totalUsed; }
    public BigDecimal getTotalRechargeAmount() { return totalRechargeAmount; }
    public void setTotalRechargeAmount(BigDecimal totalRechargeAmount) { this.totalRechargeAmount = totalRechargeAmount; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
