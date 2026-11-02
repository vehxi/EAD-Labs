package ru.kafpin.lab2.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.kafpin.lab2.entity.Address;
import ru.kafpin.lab2.entity.City;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class AddressRepository {

    private final JdbcTemplate jdbcTemplate;

    public AddressRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Address save(Address address) {
        if (address.getId() == null) {
            insert(address);
        } else {
            update(address);
        }

        return address;
    }

    public long count() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM addresses", Long.class);
        return count == null ? 0 : count;
    }

    public Optional<Address> findById(Long id) {
        String sql = """
                SELECT a.id, a.postal_code, a.country, a.region, a.district,
                       a.street, a.house, a.apartment,
                       city.id AS city_id, city.name AS city_name
                FROM addresses a
                JOIN cities city ON city.id = a.city_id
                WHERE a.id = ?
                """;

        List<Address> addresses = jdbcTemplate.query(sql, (rs, rowNum) -> {
            City city = new City();
            city.setId(rs.getLong("city_id"));
            city.setName(rs.getString("city_name"));

            Address address = new Address();
            address.setId(rs.getLong("id"));
            address.setPostalCode(rs.getString("postal_code"));
            address.setCountry(rs.getString("country"));
            address.setRegion(rs.getString("region"));
            address.setDistrict(rs.getString("district"));
            address.setStreet(rs.getString("street"));
            address.setHouse(rs.getString("house"));
            address.setApartment(rs.getString("apartment"));
            address.setCity(city);

            return address;
        }, id);

        return addresses.stream().findFirst();
    }

    private void insert(Address address) {
        String sql = """
                INSERT INTO addresses (
                    postal_code, country, region, district,
                    street, house, apartment, city_id
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql, new String[]{"id"}
            );

            statement.setString(1, address.getPostalCode());
            statement.setString(2, address.getCountry());
            statement.setString(3, address.getRegion());
            statement.setString(4, address.getDistrict());
            statement.setString(5, address.getStreet());
            statement.setString(6, address.getHouse());
            statement.setString(7, address.getApartment());
            statement.setLong(8, address.getCity().getId());

            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException(
                    "Не удалось получить ID созданного адреса"
            );
        }

        address.setId(key.longValue());
    }

    private void update(Address address) {
        String sql = """
                UPDATE addresses
                SET postal_code = ?, country = ?, region = ?, district = ?,
                    street = ?, house = ?, apartment = ?, city_id = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(
                sql,
                address.getPostalCode(),
                address.getCountry(),
                address.getRegion(),
                address.getDistrict(),
                address.getStreet(),
                address.getHouse(),
                address.getApartment(),
                address.getCity().getId(),
                address.getId()
        );
    }
}
