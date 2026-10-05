package ru.kafpin.lab2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.kafpin.lab2.entity.Customer;
import ru.kafpin.lab2.repository.CustomerRepository;
import ru.kafpin.lab2.repository.CityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import ru.kafpin.lab2.entity.Address;
import ru.kafpin.lab2.entity.City;
import ru.kafpin.lab2.repository.AddressRepository;

import java.util.List;
import java.util.Optional;

@Controller
public class CustomerController {

    @Autowired
    private  CustomerRepository customerRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private AddressRepository addressRepository;

    @GetMapping("/customers")
    public String customersPage(Model model) {
        List<Customer> allCustomers = customerRepository.findAll();

        model.addAttribute("customers", allCustomers);

        return "customers";
    }

    @GetMapping("/customers/details/{id}")
    public String detailsPage(
            Model model,
            @PathVariable("id") Long id
    ) {
        Optional<Customer> optionalCustomer =
                customerRepository.findById(id);

        if (optionalCustomer.isEmpty()) {
            return "redirect:/customers";
        }

        model.addAttribute(
                "selectedCustomer",
                optionalCustomer.get()
        );

        return "customer_details";
    }

    @GetMapping("/customers/delete/{id}")
    public String deleteCustomerById(
            @PathVariable("id") Long id
    ) {
        if (customerRepository.existsById(id)) {
            customerRepository.deleteById(id);
        }

        return "redirect:/customers";
    }

    @GetMapping("/customers/add")
    public String addCustomerPage(Model model) {
        Customer customer = new Customer();
        customer.setAddress(new Address());

        model.addAttribute("customer", customer);
        model.addAttribute("cities", cityRepository.findAll());

        return "add_customer";
    }

    @PostMapping("/customers/add")
    @Transactional
    public String addCustomer(
            @ModelAttribute Customer customer,
            @RequestParam("cityId") Long cityId
    ) {
        Optional<City> optionalCity = cityRepository.findById(cityId);

        if (optionalCity.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Выбранный город не существует"
            );
        }

        Address address = customer.getAddress();

        if (address == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Необходимо указать адрес"
            );
        }

        customer.setId(null);
        address.setId(null);

        address.setCity(optionalCity.get());

        Address savedAddress = addressRepository.save(address);
        customer.setAddress(savedAddress);

        customerRepository.save(customer);

        return "redirect:/customers";
    }

    @GetMapping("/customers/update/{id}")
    public String editCustomerPage(
            Model model,
            @PathVariable("id") Long id
    ) {
        Optional<Customer> optionalCustomer =
                customerRepository.findById(id);

        if (optionalCustomer.isEmpty()) {
            return "redirect:/customers";
        }

        model.addAttribute(
                "customer",
                optionalCustomer.get()
        );

        return "edit_customer";
    }

    @PostMapping("/customers/update")
    public String editCustomer(
            @ModelAttribute Customer customer
    ) {
        if (customer.getId() == null ||
                !customerRepository.existsById(customer.getId())) {
            return "redirect:/customers";
        }

        Customer savedCustomer = customerRepository
                .findById(customer.getId())
                .orElseThrow();

        customer.setAddress(savedCustomer.getAddress());

        customerRepository.save(customer);

        return "redirect:/customers";
    }
}
