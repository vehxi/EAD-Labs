package ru.kafpin.lab2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kafpin.lab2.entity.Customer;
import ru.kafpin.lab2.repository.CustomerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.kafpin.lab2.entity.Address;
import ru.kafpin.lab2.entity.City;
import ru.kafpin.lab2.repository.AddressRepository;
import ru.kafpin.lab2.repository.CityRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import java.util.Objects;
import java.util.Map;
import java.util.LinkedHashMap;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CustomerRestController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CityRepository cityRepository;

    @GetMapping("/customers")
    public List<Customer> getCustomers() {
        return customerRepository.findAll();
    }

    @GetMapping("/customers/{id}")
    public Customer getCustomerById(@PathVariable("id") Long id) {
        if (id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID должен быть положительным"
            );
        }

        return customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Покупатель с ID " + id + " не найден"
                ));
    }

    @PostMapping("/customers")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Customer createCustomer(@Valid @RequestBody Customer customer) {
        Address address = customer.getAddress();

        if (address.getCity() == null
                || address.getCity().getId() == null
                || address.getCity().getId() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Укажите положительный ID города"
            );
        }

        Long cityId = address.getCity().getId();

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Город с ID " + cityId + " не найден"
                ));

        customer.setId(null);
        address.setId(null);
        address.setCity(city);

        Address savedAddress = addressRepository.save(address);
        customer.setAddress(savedAddress);

        return customerRepository.save(customer);
    }

    @DeleteMapping("/customers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void deleteCustomer(@PathVariable("id") Long id) {
        Customer customer = getCustomerById(id);
        customerRepository.delete(customer);
    }

    @PutMapping("/customers/{id}")
    @Transactional
    public Customer updateCustomer(
            @PathVariable("id") Long id,
            @Valid @RequestBody Customer customer) {
        Customer existingCustomer = getCustomerById(id);
        Address address = customer.getAddress();

        if (address.getCity() == null
                || address.getCity().getId() == null
                || address.getCity().getId() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Укажите положительный ID города"
            );
        }

        Long cityId = address.getCity().getId();

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Город с ID " + cityId + " не найден"
                ));

        Address currentAddress = existingCustomer.getAddress();

        if (sameAddress(currentAddress, address, cityId)) {
            customer.setAddress(currentAddress);
        } else {
            address.setId(null);
            address.setCity(city);

            Address savedAddress = addressRepository.save(address);
            customer.setAddress(savedAddress);
        }

        customer.setId(existingCustomer.getId());

        return customerRepository.save(customer);
    }

    @GetMapping("/customers/statistics")
    @Transactional(readOnly = true)
    public Map<String, Object> getCustomerStatistics() {
        List<Customer> customers = customerRepository.findAll();

        long totalHeight = 0;
        double totalWeight = 0;

        Map<String, Integer> customersByCity = new LinkedHashMap<>();

        for (Customer customer : customers) {
            totalHeight += customer.getHeight();
            totalWeight += customer.getWeight();

            String cityName = customer.getAddress().getCity().getName();

            int currentCount = customersByCity.getOrDefault(cityName, 0);
            customersByCity.put(cityName, currentCount + 1);
        }

        Double averageHeight = null;
        Double averageWeight = null;

        if (!customers.isEmpty()) {
            averageHeight =
                    Math.round(totalHeight * 100.0 / customers.size()) / 100.0;

            averageWeight =
                    Math.round(totalWeight * 100.0 / customers.size()) / 100.0;
        }

        Map<String, Object> statistics = new LinkedHashMap<>();
        statistics.put("totalCustomers", customers.size());
        statistics.put("averageHeight", averageHeight);
        statistics.put("averageWeight", averageWeight);
        statistics.put("customersByCity", customersByCity);

        return statistics;
    }

    private boolean sameAddress(
            Address current,
            Address submitted,
            Long cityId
    ) {
        return current != null
                && current.getCity() != null
                && Objects.equals(current.getCity().getId(), cityId)
                && Objects.equals(current.getPostalCode(), submitted.getPostalCode())
                && Objects.equals(current.getCountry(), submitted.getCountry())
                && Objects.equals(current.getRegion(), submitted.getRegion())
                && Objects.equals(current.getDistrict(), submitted.getDistrict())
                && Objects.equals(current.getStreet(), submitted.getStreet())
                && Objects.equals(current.getHouse(), submitted.getHouse())
                && Objects.equals(current.getApartment(), submitted.getApartment());
    }
}
