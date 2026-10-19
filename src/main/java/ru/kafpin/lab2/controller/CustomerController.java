package ru.kafpin.lab2.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import ru.kafpin.lab2.entity.Address;
import ru.kafpin.lab2.entity.City;
import ru.kafpin.lab2.entity.Customer;
import ru.kafpin.lab2.repository.AddressRepository;
import ru.kafpin.lab2.repository.CityRepository;
import ru.kafpin.lab2.repository.CustomerRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private AddressRepository addressRepository;

    @InitBinder({"customer", "filter"})
    public void configureBinding(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
        if ("customer".equals(binder.getObjectName())) {
            binder.setAllowedFields("id", "lastName", "name", "patronymic",
                    "gender", "nationality", "height", "weight", "birthDate",
                    "phoneNumber", "creditCardNumber", "bankAccountNumber",
                    "address.postalCode", "address.country", "address.region",
                    "address.district", "address.street", "address.house",
                    "address.apartment");
        } else {
            binder.setAllowedFields("lastName", "name", "gender", "nationality",
                    "city", "street");
        }
    }

    @GetMapping("/customers")
    public String customersPage(
            @Valid @ModelAttribute("filter") CustomerFilter filter,
            BindingResult result,
            Model model
    ) {
        List<Customer> customers = result.hasErrors() ? List.of()
                : customerRepository.search(filter.getLastName(), filter.getName(),
                filter.getGender(), filter.getNationality(), filter.getCity(),
                filter.getStreet());
        model.addAttribute("customers", customers);
        return "customers";
    }

    @GetMapping("/customers/details/{id}")
    public String detailsPage(
            Model model,
            @PathVariable("id") Long id
    ) {
        checkId(id);
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
        checkId(id);
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
        model.addAttribute("selectedCityId", "");

        return "add_customer";
    }

    @PostMapping("/customers/add")
    @Transactional
    public String addCustomer(
            @Valid @ModelAttribute("customer") Customer customer,
            BindingResult result,
            @RequestParam(name = "cityId", defaultValue = "") String cityId,
            Model model
    ) {
        City city = findCity(cityId, result);
        if (result.hasErrors()) {
            return showForm("add_customer", cityId, model);
        }

        Address address = customer.getAddress();

        customer.setId(null);
        address.setId(null);

        address.setCity(city);

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
        checkId(id);
        Optional<Customer> optionalCustomer =
                customerRepository.findById(id);

        if (optionalCustomer.isEmpty()) {
            return "redirect:/customers";
        }

        Customer customer = optionalCustomer.get();

        if (customer.getAddress() == null) {
            customer.setAddress(new Address());
        }

        model.addAttribute("customer", customer);
        model.addAttribute("cities", cityRepository.findAll());
        model.addAttribute("selectedCityId", customer.getAddress().getCity() == null
                ? "" : customer.getAddress().getCity().getId().toString());

        return "edit_customer";
    }

    @PostMapping("/customers/update")
    @Transactional
    public String editCustomer(
            @Valid @ModelAttribute("customer") Customer customer,
            BindingResult result,
            @RequestParam(name = "cityId", defaultValue = "") String cityId,
            Model model
    ) {
        if (customer.getId() == null) {
            result.rejectValue("id", "id.required", "Укажите корректный ID покупателя");
        }
        City city = findCity(cityId, result);
        if (result.hasErrors()) {
            return showForm("edit_customer", cityId, model);
        }

        Optional<Customer> optionalCustomer =
                customerRepository.findById(customer.getId());

        if (optionalCustomer.isEmpty()) {
            return "redirect:/customers";
        }

        Address submittedAddress = customer.getAddress();

        Address currentAddress =
                optionalCustomer.get().getAddress();

        if (sameAddress(currentAddress, submittedAddress, city.getId())) {
            customer.setAddress(currentAddress);
        } else {
            submittedAddress.setId(null);
            submittedAddress.setCity(city);

            Address savedAddress =
                    addressRepository.save(submittedAddress);

            customer.setAddress(savedAddress);
        }

        customerRepository.save(customer);

        return "redirect:/customers";
    }

    private City findCity(String cityId, BindingResult result) {
        try {
            long id = Long.parseLong(cityId.trim());
            if (id > 0) {
                Optional<City> city = cityRepository.findById(id);
                if (city.isPresent()) {
                    return city.get();
                }
            }
        } catch (NumberFormatException ignored) {
            // Ошибка выбора города отображается в форме вместе с остальными ошибками.
        }
        result.rejectValue("address.city", "city.invalid", "Выберите существующий город");
        return null;
    }

    private String showForm(String template, String cityId, Model model) {
        model.addAttribute("cities", cityRepository.findAll());
        model.addAttribute("selectedCityId", cityId.trim());
        return template;
    }

    private void checkId(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "ID должен быть положительным");
        }
    }

    private boolean sameAddress(
            Address current,
            Address submitted,
            Long cityId
    ) {
        return current != null
                && current.getCity() != null
                && Objects.equals(current.getCity().getId(), cityId)
                && sameText(
                current.getPostalCode(),
                submitted.getPostalCode()
        )
                && sameText(
                current.getCountry(),
                submitted.getCountry()
        )
                && sameText(
                current.getRegion(),
                submitted.getRegion()
        )
                && sameText(
                current.getDistrict(),
                submitted.getDistrict()
        )
                && sameText(
                current.getStreet(),
                submitted.getStreet()
        )
                && sameText(
                current.getHouse(),
                submitted.getHouse()
        )
                && sameText(
                current.getApartment(),
                submitted.getApartment()
        );
    }

    private boolean sameText(String first, String second) {
        return Objects.toString(first, "")
                .equals(Objects.toString(second, ""));
    }
}
