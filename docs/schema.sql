CREATE DATABASE IF NOT EXISTS xuance_divination
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE xuance_divination;

CREATE TABLE IF NOT EXISTS user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  nickname VARCHAR(50),
  email VARCHAR(100),
  avatar VARCHAR(255),
  gender VARCHAR(10),
  birth_date VARCHAR(20),
  birth_time VARCHAR(20),
  birth_place VARCHAR(100),
  birth_day_gan_zhi VARCHAR(20),
  birth_day_master VARCHAR(10),
  role VARCHAR(20) DEFAULT 'USER',
  status TINYINT DEFAULT 1,
  create_time DATETIME,
  update_time DATETIME
);

CREATE TABLE IF NOT EXISTS verification_code (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  target VARCHAR(120) NOT NULL,
  scene VARCHAR(40) NOT NULL,
  code VARCHAR(20) NOT NULL,
  used TINYINT DEFAULT 0,
  expire_time DATETIME,
  create_time DATETIME,
  INDEX idx_verification_target_scene (target, scene, create_time)
);

CREATE TABLE IF NOT EXISTS divination_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  type VARCHAR(50) NOT NULL COMMENT 'BAZI/LIUYAO/QIMEN',
  question VARCHAR(500),
  input_json LONGTEXT,
  result_json LONGTEXT,
  result_text LONGTEXT,
  knowledge_rule_ids VARCHAR(1000),
  classic_references LONGTEXT,
  create_time DATETIME,
  update_time DATETIME,
  INDEX idx_record_user_type (user_id, type),
  INDEX idx_record_create_time (create_time)
);

CREATE TABLE IF NOT EXISTS knowledge_rule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  type VARCHAR(50) NOT NULL COMMENT 'BAZI/LIUYAO/QIMEN',
  category VARCHAR(100),
  title VARCHAR(200) NOT NULL,
  keywords VARCHAR(500),
  rule_content TEXT NOT NULL,
  example TEXT,
  priority INT DEFAULT 0,
  enabled TINYINT DEFAULT 1,
  create_time DATETIME,
  update_time DATETIME,
  INDEX idx_rule_type_category (type, category),
  INDEX idx_rule_enabled_priority (enabled, priority)
);

CREATE TABLE IF NOT EXISTS classic_book (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  slug VARCHAR(100) NOT NULL UNIQUE,
  author VARCHAR(100),
  dynasty VARCHAR(50),
  focus VARCHAR(100),
  description TEXT,
  source_url VARCHAR(500),
  source_note VARCHAR(500),
  copyright_status VARCHAR(100),
  sort_order INT DEFAULT 0,
  enabled TINYINT DEFAULT 1,
  create_time DATETIME,
  update_time DATETIME,
  INDEX idx_classic_book_enabled_sort (enabled, sort_order)
);

CREATE TABLE IF NOT EXISTS classic_chapter (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  book_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  volume VARCHAR(100),
  chapter_order INT DEFAULT 0,
  original_text LONGTEXT,
  plain_text LONGTEXT,
  notes TEXT,
  source_url VARCHAR(500),
  content_status VARCHAR(50) DEFAULT 'OUTLINE',
  create_time DATETIME,
  update_time DATETIME,
  INDEX idx_classic_chapter_book_order (book_id, chapter_order),
  CONSTRAINT fk_classic_chapter_book FOREIGN KEY (book_id) REFERENCES classic_book(id)
);

CREATE TABLE IF NOT EXISTS ai_call_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  record_id BIGINT,
  model_name VARCHAR(100),
  prompt LONGTEXT,
  response LONGTEXT,
  token_count INT,
  cost DECIMAL(10,4),
  status VARCHAR(50),
  error_message TEXT,
  create_time DATETIME
);

CREATE TABLE IF NOT EXISTS user_quota (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  free_used INT DEFAULT 0,
  paid_credits INT DEFAULT 0,
  total_used INT DEFAULT 0,
  total_recharge_amount DECIMAL(10,2) DEFAULT 0.00,
  create_time DATETIME,
  update_time DATETIME,
  INDEX idx_user_quota_user (user_id)
);

CREATE TABLE IF NOT EXISTS recharge_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  package_code VARCHAR(50) NOT NULL,
  quantity INT DEFAULT 1,
  amount DECIMAL(10,2) NOT NULL,
  credits INT NOT NULL,
  status VARCHAR(30) DEFAULT 'WAIT_PAY',
  out_trade_no VARCHAR(64),
  trade_no VARCHAR(80),
  pay_channel VARCHAR(30),
  create_time DATETIME,
  paid_time DATETIME,
  update_time DATETIME,
  UNIQUE KEY uk_recharge_out_trade_no (out_trade_no),
  INDEX idx_recharge_user_time (user_id, create_time)
);

CREATE TABLE IF NOT EXISTS feedback (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  record_id BIGINT,
  score INT,
  content TEXT,
  create_time DATETIME
);
