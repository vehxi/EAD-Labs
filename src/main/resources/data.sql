INSERT INTO cities (name)
VALUES ('Казань');

INSERT INTO addresses (
    postal_code, country, region, district,
    street, house, apartment, city_id
) VALUES (
             '420111', 'Россия', 'Республика Татарстан', 'Вахитовский',
             'Баумана', '10', '12',
             (SELECT id FROM cities WHERE name = 'Казань')
         );

INSERT INTO addresses (
    postal_code, country, region, district,
    street, house, apartment, city_id
) VALUES (
             '420012', 'Россия', 'Республика Татарстан', 'Вахитовский',
             'Пушкина', '5', '8',
             (SELECT id FROM cities WHERE name = 'Казань')
         );

INSERT INTO customers (
    last_name, name, patronymic, gender, nationality,
    height, weight, birth_date, phone_number,
    credit_card_number, bank_account_number, address_id
) VALUES (
             'Иванов', 'Иван', 'Иванович', 'Мужской', 'Русский',
             180, 78.5, '1995-05-15', '+7 999 111-22-33',
             '2200 1234 5678 9012', '40817810000001234567',
             (
                 SELECT id
                 FROM addresses
                 WHERE street = 'Баумана'
                   AND house = '10'
                   AND apartment = '12'
             )
         );

INSERT INTO customers (
    last_name, name, patronymic, gender, nationality,
    height, weight, birth_date, phone_number,
    credit_card_number, bank_account_number, address_id
) VALUES (
             'Петрова', 'Мария', 'Сергеевна', 'Женский', 'Русская',
             168, 57.2, '1998-10-21', '+7 999 444-55-66',
             '2200 9876 5432 1098', '40817810000009876543',
             (
                 SELECT id
                 FROM addresses
                 WHERE street = 'Пушкина'
                   AND house = '5'
                   AND apartment = '8'
             )
         );