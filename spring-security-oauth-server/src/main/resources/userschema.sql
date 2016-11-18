CREATE TABLE IF NOT EXISTS users (
  username VARCHAR(45) NOT NULL,
  password VARCHAR(60) NOT NULL,
  enabled  TINYINT     NOT NULL DEFAULT 1,
  PRIMARY KEY (username)
);

CREATE TABLE IF NOT EXISTS user_roles (
  user_role_id INT(11)     NOT NULL AUTO_INCREMENT,
  username     VARCHAR(45) NOT NULL,
  role         VARCHAR(45) NOT NULL,
  PRIMARY KEY (user_role_id),
  UNIQUE KEY uni_username_role (role, username),
  KEY fk_username_idx (username),
  CONSTRAINT fk_username FOREIGN KEY (username) REFERENCES users (username)
);

# john password = '123'
# tom password = '111'
INSERT INTO users (username, password, enabled)
VALUES ('john', '$2a$10$aZ1q7qxS9wSZ703eDTtywe2NiXmuzTqWOr2jHjs.q4TUjntoDJRju', TRUE);
INSERT INTO users (username, password, enabled)
VALUES ('tom', '$2a$04$AyTw9jLXaSHVusWehEIg2.qh8C7X/p6AgDXFRC/RZHujCQe/77vva', TRUE);

INSERT INTO user_roles (username, role)
VALUES ('john', 'USER');
INSERT INTO user_roles (username, role)
VALUES ('tom', 'USER');
INSERT INTO user_roles (username, role)
VALUES ('tom', 'ADMIN');