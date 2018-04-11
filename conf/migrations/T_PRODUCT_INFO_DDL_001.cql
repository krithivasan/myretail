-- description: create product_info table
-- authoredAt: 1523435800
-- up:

CREATE TABLE product_info (
    id int,
    currency_code text,
    value double,
    PRIMARY KEY (id)
);