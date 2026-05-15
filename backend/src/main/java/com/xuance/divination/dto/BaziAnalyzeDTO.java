package com.xuance.divination.dto;

public class BaziAnalyzeDTO {
    private Long userId;
    private String gender;
    private String birthDate;
    private String birthTime;
    private String birthPlace;
    private String yearPillar;
    private String monthPillar;
    private String dayPillar;
    private String hourPillar;
    private String dayMaster;
    private String luckPillar;
    private String currentYearPillar;
    private String baziDetails;
    private String questionType;
    private String question;

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
    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
}
