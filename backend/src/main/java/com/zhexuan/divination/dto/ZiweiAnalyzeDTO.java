package com.zhexuan.divination.dto;

public class ZiweiAnalyzeDTO {
    private Long userId;
    private String gender;
    private String birthDate;
    private String birthTime;
    private String birthPlace;
    private String questionType;
    private String question;
    private String chartJson;
    private String cacheKey;

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
    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getChartJson() { return chartJson; }
    public void setChartJson(String chartJson) { this.chartJson = chartJson; }
    public String getCacheKey() { return cacheKey; }
    public void setCacheKey(String cacheKey) { this.cacheKey = cacheKey; }
}
