package ru.kafpin.lab2.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.kafpin.lab2.entity.City;

import java.util.List;
import java.util.Optional;

@Repository
public class CityRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<City> cityMapper = (rs, rowNum) -> {
        City city = new City();
        city.setId(rs.getLong("id"));
        city.setName(rs.getString("name"));
        return city;
    };

    public CityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<City> findAll() {
        String sql = "SELECT id, name FROM cities ORDER BY id";

        return jdbcTemplate.query(sql, cityMapper);
    }

    public Optional<City> findById(Long id) {
        String sql = "SELECT id, name FROM cities WHERE id = ?";

        try {
            City city = jdbcTemplate.queryForObject(sql, cityMapper, id);
            return Optional.ofNullable(city);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }
}
