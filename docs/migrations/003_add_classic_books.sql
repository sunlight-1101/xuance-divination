USE zhexuan_divination;
SET NAMES utf8mb4;

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
