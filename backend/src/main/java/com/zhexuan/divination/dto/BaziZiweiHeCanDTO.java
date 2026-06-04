package com.zhexuan.divination.dto;

/**
 * 八字紫微合参分析请求 DTO
 */
public class BaziZiweiHeCanDTO {
    private Long userId;

    // --- 基本信息 ---
    private String gender;
    private String birthDate;
    private String birthTime;
    private String birthPlace;

    // --- 八字信息（前端计算后传入）---
    private String yearPillar;
    private String monthPillar;
    private String dayPillar;
    private String hourPillar;
    private String dayMaster;
    private String luckPillar;
    private String currentYearPillar;
    private String baziDetails;
    private String luckCycles;

    // --- 紫微信息（后端排盘后前端传回）---
    private String chartJson;

    // --- 问题 ---
    private String questionType;
    private String question;
    private String cacheKey;

    // --- getters/setters ---
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getBirthTime() { return birthTime; }
    public void setBirthTime(String birthTime) { this.birthTime = birthTime; }

    public String getBirthPlace() { return birthPlace; }
    public void setBirthPlace(String birthPlace) { this.birthPlace = birthPlace; }

    public String getYearPillar() { return yearPillar; }
    public void setYearPillar(String yearPillar) { this.yearPillar = yearPillar; }

    public String getMonthPillar() { return monthPillar; }
    public void setMonthPillar(String monthPillar) { this.monthPillar = monthPillar; }

    public String getDayPillar() { return dayPillar; }
    public void setDayPillar(String dayPillar) { this.dayPillar = dayPillar; }

    public String getHourPillar() { return hourPillar; }
    public void setHourPillar(String hourPillar) { this.hourPillar = hourPillar; }

    public String getDayMaster() { return dayMaster; }
    public void setDayMaster(String dayMaster) { this.dayMaster = dayMaster; }

    public String getLuckPillar() { return luckPillar; }
    public void setLuckPillar(String luckPillar) { this.luckPillar = luckPillar; }

    public String getCurrentYearPillar() { return currentYearPillar; }
    public void setCurrentYearPillar(String currentYearPillar) { this.currentYearPillar = currentYearPillar; }

    public String getBaziDetails() { return baziDetails; }
    public void setBaziDetails(String baziDetails) { this.baziDetails = baziDetails; }

    public String getLuckCycles() { return luckCycles; }
    public void setLuckCycles(String luckCycles) { this.luckCycles = luckCycles; }

    public String getChartJson() { return chartJson; }
    public void setChartJson(String chartJson) { this.chartJson = chartJson; }

    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getCacheKey() { return cacheKey; }
    public void setCacheKey(String cacheKey) { this.cacheKey = cacheKey; }
}
