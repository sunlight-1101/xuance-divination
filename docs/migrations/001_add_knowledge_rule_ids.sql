USE xuance_divination;

ALTER TABLE divination_record
  ADD COLUMN knowledge_rule_ids VARCHAR(1000) NULL AFTER result_text;

