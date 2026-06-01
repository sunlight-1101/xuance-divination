package com.zhexuan.divination.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("divination_record")
public class DivinationRecord {
    private Long id;
    private Long userId;
    private String type;
    private String question;
    private String inputJson;
    private String resultJson;
    private String resultText;
    private String knowledgeRuleIds;
    private String classicReferences;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getInputJson() { return inputJson; }
    public void setInputJson(String inputJson) { this.inputJson = inputJson; }
    public String getResultJson() { return resultJson; }
    public void setResultJson(String resultJson) { this.resultJson = resultJson; }
    public String getResultText() { return resultText; }
    public void setResultText(String resultText) { this.resultText = resultText; }
    public String getKnowledgeRuleIds() { return knowledgeRuleIds; }
    public void setKnowledgeRuleIds(String knowledgeRuleIds) { this.knowledgeRuleIds = knowledgeRuleIds; }
    public String getClassicReferences() { return classicReferences; }
    public void setClassicReferences(String classicReferences) { this.classicReferences = classicReferences; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
