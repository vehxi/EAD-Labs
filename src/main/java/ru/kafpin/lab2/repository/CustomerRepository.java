package ru.kafpin.lab2.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.kafpin.lab2.entity.Customer;

import java.util.List;

public interface CustomerRepository extends CrudRepository<Customer, Long> {

    List<Customer> findAll();

    @Query("""
            select c from Customer c
            join c.address a
            join a.city city
            where (:lastName = '' or locate(lower(:lastName), lower(c.lastName)) > 0)
              and (:name = '' or locate(lower(:name), lower(c.name)) > 0)
              and (:gender = '' or lower(c.gender) = lower(:gender))
              and (:nationality = '' or locate(lower(:nationality), lower(c.nationality)) > 0)
              and (:city = '' or locate(lower(:city), lower(city.name)) > 0)
              and (:street = '' or locate(lower(:street), lower(a.street)) > 0)
            order by c.id
            """)
    List<Customer> search(
            @Param("lastName") String lastName,
            @Param("name") String name,
            @Param("gender") String gender,
            @Param("nationality") String nationality,
            @Param("city") String city,
            @Param("street") String street
    );
}
