CREATE TABLE coupon (
  id int(11) NOT NULL AUTO_INCREMENT,
  name varchar(255) DEFAULT NULL,
  promoteMoney float DEFAULT NULL,
  get_Date datetime DEFAULT NULL,
  expire_Date datetime DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;