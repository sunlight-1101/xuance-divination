ALTER TABLE divination_record
  ADD COLUMN classic_references LONGTEXT NULL AFTER knowledge_rule_ids;
