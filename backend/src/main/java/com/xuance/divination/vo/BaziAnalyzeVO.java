package com.xuance.divination.vo;

import com.xuance.divination.entity.KnowledgeRule;
import java.util.List;

public class BaziAnalyzeVO {
    private Long recordId;
    private String resultJson;
    private String resultText;
    private List<KnowledgeRule> knowledgeRules;
    private List<String> classicReferences;

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public String getResultJson() { return resultJson; }
    public void setResultJson(String resultJson) { this.resultJson = resultJson; }
    public String getResultText() { return resultText; }
    public void setResultText(String resultText) { this.resultText = resultText; }
    public List<KnowledgeRule> getKnowledgeRules() { return knowledgeRules; }
    public void setKnowledgeRules(List<KnowledgeRule> knowledgeRules) { this.knowledgeRules = knowledgeRules; }
    public List<String> getClassicReferences() { return classicReferences; }
    public void setClassicReferences(List<String> classicReferences) { this.classicReferences = classicReferences; }
}
