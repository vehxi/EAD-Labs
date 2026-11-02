package ru.kafpin.lab2.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.kafpin.lab2.entity.Address;
import ru.kafpin.lab2.entity.City;
import ru.kafpin.lab2.entity.Customer;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class CustomerRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String SELECT_CUSTOMERS = """
            SELECT
                c.id, c.last_name, c.name, c.patronymic,
                c.gender, c.nationality, c.height, c.weight,
                c.birth_date, c.phone_number,
                c.credit_card_number, c.bank_account_number,
                a.id AS address_id,
                a.postal_code, a.country, a.region, a.district,
                a.street, a.house, a.apartment,
                city.id AS city_id,
                city.name AS city_name
            FROM customers c
            JOIN addresses a ON a.id = c.address_id
            JOIN cities city ON city.id = a.city_id
            """;

    private final RowMapper<Customer> customerMapper = (rs, rowNum) -> {
        City city = new City();
        city.setId(rs.getLong("city_id"));
        city.setName(rs.getString("city_name"));

        Address address = new Address();
        address.setId(rs.getLong("address_id"));
        address.setPostalCode(rs.getString("postal_code"));
        address.setCountry(rs.getString("country"));
        address.setRegion(rs.getString("region"));
        address.setDistrict(rs.getString("district"));
        address.setStreet(rs.getString("street"));
        address.setHouse(rs.getString("house"));
        address.setApartment(rs.getString("apartment"));
        address.setCity(city);

        Customer customer = new Customer();
        customer.setId(rs.getLong("id"));
        customer.setLastName(rs.getString("last_name"));
        customer.setName(rs.getString("name"));
        customer.setPatronymic(rs.getString("patronymic"));
        customer.setGender(rs.getString("gender"));
        customer.setNationality(rs.getString("nationality"));
        customer.setHeight(rs.getInt("height"));
        customer.setWeight(rs.getDouble("weight"));
        customer.setBirthDate(
                rs.getObject("birth_date", LocalDate.class)
        );
        customer.setPhoneNumber(rs.getString("phone_number"));
        customer.setCreditCardNumber(rs.getString("credit_card_number"));
        customer.setBankAccountNumber(rs.getString("bank_account_number"));
        customer.setAddress(address);

        return customer;
    };

    public CustomerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Customer> findAll() {
        String sql = SELECT_CUSTOMERS + " ORDER BY c.id";

        return jdbcTemplate.query(sql, customerMapper);
    }

    public Optional<Customer> findById(Long id) {
        String sql = SELECT_CUSTOMERS + " WHERE c.id = ?";

        List<Customer> customers = jdbcTemplate.query(
                sql, customerMapper, id
        );

        return customers.stream().findFirst();
    }

    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM customers WHERE id = ?";

        Long count = jdbcTemplate.queryForObject(sql, Long.class, id);

        return count != null && count > 0;
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM customers WHERE id = ?";

        jdbcTemplate.update(sql, id);
    }

    public void delete(Customer customer) {
        deleteById(customer.getId());
    }

    public long count() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customers", Long.class);
        return count == null ? 0 : count;
    }

    public List<Customer> search(
            String lastName,
            String name,
            String gender,
            String nationality,
            String city,
            String street
    ) {
        String sql = SELECT_CUSTOMERS + """
                WHERE (? = '' OR LOCATE(LOWER(?), LOWER(c.last_name)) > 0)
                  AND (? = '' OR LOCATE(LOWER(?), LOWER(c.name)) > 0)
                  AND (? = '' OR LOWER(c.gender) = LOWER(?))
                  AND (? = '' OR LOCATE(LOWER(?), LOWER(c.nationality)) > 0)
                  AND (? = '' OR LOCATE(LOWER(?), LOWER(city.name)) > 0)
                  AND (? = '' OR LOCATE(LOWER(?), LOWER(a.street)) > 0)
                ORDER BY c.id
                """;

        return jdbcTemplate.query(
                sql, customerMapper,
                lastName, lastName, name, name, gender, gender,
                nationality, nationality, city, city, street, street
        );
    }

    public Customer save(Customer customer) {
        if (customer.getId() == null) {
            insert(customer);
        } else {
            update(customer);
        }

        return customer;
    }

    private void insert(Customer customer) {
        String sql = """
                INSERT INTO customers (
                    last_name, name, patronymic, gender, nationality,
                    height, weight, birth_date, phone_number,
                    credit_card_number, bank_account_number, address_id
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql, new String[]{"id"}
            );

            statement.setString(1, customer.getLastName());
            statement.setString(2, customer.getName());
            statement.setString(3, customer.getPatronymic());
            statement.setString(4, customer.getGender());
            statement.setString(5, customer.getNationality());
            statement.setInt(6, customer.getHeight());
            statement.setDouble(7, customer.getWeight());
            statement.setObject(8, customer.getBirthDate());
            statement.setString(9, customer.getPhoneNumber());
            statement.setString(10, customer.getCreditCardNumber());
            statement.setString(11, customer.getBankAccountNumber());
            statement.setLong(12, customer.getAddress().getId());

            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException(
                    "Не удалось получить ID созданного покупателя"
            );
        }

        customer.setId(key.longValue());
    }

    private void update(Customer customer) {
        String sql = """
                UPDATE customers
                SET last_name = ?, name = ?, patronymic = ?,
                    gender = ?, nationality = ?, height = ?, weight = ?,
                    birth_date = ?, phone_number = ?,
                    credit_card_number = ?, bank_account_number = ?,
                    address_id = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(
                sql,
                customer.getLastName(),
                customer.getName(),
                customer.getPatronymic(),
                customer.getGender(),
                customer.getNationality(),
                customer.getHeight(),
                customer.getWeight(),
                customer.getBirthDate(),
                customer.getPhoneNumber(),
                customer.getCreditCardNumber(),
                customer.getBankAccountNumber(),
                customer.getAddress().getId(),
                customer.getId()
        );
    }
}
