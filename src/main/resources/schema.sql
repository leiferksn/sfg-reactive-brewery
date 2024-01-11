CREATE TABLE IF NOT EXISTS beer (
id integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
beer_name varchar2(255),
beer_style varchar2(255),
upc varchar2(25),
quantity_on_hand integer,
price decimal,
created_date timestamp,
last_modified_date timestamp
);