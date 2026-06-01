USE zhexuan_divination;

ALTER TABLE user
  ADD COLUMN gender VARCHAR(10) NULL AFTER avatar,
  ADD COLUMN birth_date VARCHAR(20) NULL AFTER gender,
  ADD COLUMN birth_time VARCHAR(20) NULL AFTER birth_date,
  ADD COLUMN birth_place VARCHAR(100) NULL AFTER birth_time,
  ADD COLUMN birth_day_gan_zhi VARCHAR(20) NULL AFTER birth_place,
  ADD COLUMN birth_day_master VARCHAR(10) NULL AFTER birth_day_gan_zhi;

