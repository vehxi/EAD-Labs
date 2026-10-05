package ru.kafpin.lab2.repository;

import org.springframework.data.repository.CrudRepository;
import ru.kafpin.lab2.entity.Address;

public interface AddressRepository extends CrudRepository<Address, Long> {
}