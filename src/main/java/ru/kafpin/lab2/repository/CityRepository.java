package ru.kafpin.lab2.repository;

import org.springframework.data.repository.CrudRepository;
import ru.kafpin.lab2.entity.City;

import java.util.List;

public interface CityRepository extends CrudRepository<City, Long> {

    List<City> findAll();
}