package com.xuance.divination.dto;

import java.util.List;

public class LiuyaoAnalyzeDTO {
    private Long userId;
    private String question;
    private String gender;
    private String birthDate;
    private String birthTime;
    private String birthPlace;
    private String birthDayGanZhi;
    private String birthDayMaster;
    private String time;
    private String dayStem;
    private String dayGanZhi;
    private String monthBranch;
    private String dayBranch;
    private String emptyBranches;
    private String mainGua;
    private String changedGua;
    private List<LiuyaoYaoDTO> yaoList;
    private String cacheKey;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public String getBirthTime() { return birthTime; }
    public void setBirthTime(String birthTime) { this.birthTime = birthTime; }
    public String getBirthPlace() { return birthPlace; }
    public void setBirthPlace(String birthPlace) { this.birthPlace = birthPlace; }
    public String getBirthDayGanZhi() { return birthDayGanZhi; }
    public void setBirthDayGanZhi(String birthDayGanZhi) { this.birthDayGanZhi = birthDayGanZhi; }
    public String getBirthDayMaster() { return birthDayMaster; }
    public void setBirthDayMaster(String birthDayMaster) { this.birthDayMaster = birthDayMaster; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getDayStem() { return dayStem; }
    public void setDayStem(String dayStem) { this.dayStem = dayStem; }
    public String getDayGanZhi() { return dayGanZhi; }
    public void setDayGanZhi(String dayGanZhi) { this.dayGanZhi = dayGanZhi; }
    public String getMonthBranch() { return monthBranch; }
    public void setMonthBranch(String monthBranch) { this.monthBranch = monthBranch; }
    public String getDayBranch() { return dayBranch; }
    public void setDayBranch(String dayBranch) { this.dayBranch = dayBranch; }
    public String getEmptyBranches() { return emptyBranches; }
    public void setEmptyBranches(String emptyBranches) { this.emptyBranches = emptyBranches; }
    public String getMainGua() { return mainGua; }
    public void setMainGua(String mainGua) { this.mainGua = mainGua; }
    public String getChangedGua() { return changedGua; }
    public void setChangedGua(String changedGua) { this.changedGua = changedGua; }
    public List<LiuyaoYaoDTO> getYaoList() { return yaoList; }
    public void setYaoList(List<LiuyaoYaoDTO> yaoList) { this.yaoList = yaoList; }
    public String getCacheKey() { return cacheKey; }
    public void setCacheKey(String cacheKey) { this.cacheKey = cacheKey; }
}
