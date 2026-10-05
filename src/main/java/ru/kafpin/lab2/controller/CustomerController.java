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

import java.util.List;
import java.util.Optional;

@Controller
public class CustomerController {

    @Autowired
    private  CustomerRepository customerRepository;

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
        model.addAttribute("customer", new Customer());

        return "add_customer";
    }

    @PostMapping("/customers/add")
    public String addCustomer(
            @ModelAttribute Customer customer
    ) {
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

        customerRepository.save(customer);

        return "redirect:/customers";
    }
}
