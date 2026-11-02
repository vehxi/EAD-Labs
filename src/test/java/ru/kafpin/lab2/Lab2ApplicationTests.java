package ru.kafpin.lab2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.kafpin.lab2.entity.Customer;
import ru.kafpin.lab2.repository.AddressRepository;
import ru.kafpin.lab2.repository.CityRepository;
import ru.kafpin.lab2.repository.CustomerRepository;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class Lab2ApplicationTests {

    private final MockMvc mvc;
    private final CustomerRepository customers;
    private final AddressRepository addresses;
    private final CityRepository cities;

    @Autowired
    Lab2ApplicationTests(
            MockMvc mvc,
            CustomerRepository customers,
            AddressRepository addresses,
            CityRepository cities
    ) {
        this.mvc = mvc;
        this.customers = customers;
        this.addresses = addresses;
        this.cities = cities;
    }

    @Test
    void pagesAndThreeSearchCombinationsWork() throws Exception {
        mvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("customers", hasSize(2)));
        mvc.perform(get("/customers").param("lastName", "иван").param("name", "ив"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("customers", hasSize(1)));
        mvc.perform(get("/customers").param("city", "каз").param("street", "баум"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("customers", hasSize(1)));
        mvc.perform(get("/customers").param("gender", "Женский").param("nationality", "рус"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("customers", hasSize(1)));
        mvc.perform(get("/customers").param("lastName", "Иванов").param("street", "Пушкина"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("customers", hasSize(0)));
        mvc.perform(get("/customers").param("name", "%"))
                .andExpect(model().attribute("customers", hasSize(0)));
        mvc.perform(get("/customers/add")).andExpect(status().isOk());
        Long id = customers.findAll().getFirst().getId();
        mvc.perform(get("/customers/details/{id}", id)).andExpect(status().isOk());
        mvc.perform(get("/customers/update/{id}", id)).andExpect(status().isOk());
    }

    @Test
    void createsCustomerWithAddressAndCity() throws Exception {
        long customerCount = customers.count();
        long addressCount = addresses.count();
        mvc.perform(post("/customers/add").params(validForm()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"));
        assertThat(customers.count()).isEqualTo(customerCount + 1);
        assertThat(addresses.count()).isEqualTo(addressCount + 1);
        Customer saved = customers.findAll().stream()
                .filter(c -> c.getLastName().equals("Сидоров")).findFirst().orElseThrow();
        assertThat(saved.getAddress().getCity().getName()).isEqualTo("Казань");
        assertThat(saved.getAddress().getStreet()).isEqualTo("Ленина");
    }

    @Test
    void rejectsInvalidInputWithoutWritingAnyRows() throws Exception {
        long customerCount = customers.count();
        long addressCount = addresses.count();
        Map<String, String> invalid = Map.ofEntries(
                Map.entry("lastName", "   "), Map.entry("name", ""),
                Map.entry("patronymic", ""), Map.entry("gender", "ошибка"),
                Map.entry("nationality", " "), Map.entry("height", "-1"),
                Map.entry("weight", "NaN"), Map.entry("birthDate", "2099-01-01"),
                Map.entry("phoneNumber", "abc"), Map.entry("creditCardNumber", "123"),
                Map.entry("bankAccountNumber", "123"), Map.entry("address.postalCode", "!!!"),
                Map.entry("address.country", ""), Map.entry("address.region", "x".repeat(256)),
                Map.entry("address.district", "x".repeat(256)), Map.entry("address.street", ""),
                Map.entry("address.house", ""), Map.entry("address.apartment", "x".repeat(256)),
                Map.entry("cityId", "999999"));
        for (var entry : invalid.entrySet()) {
            MultiValueMap<String, String> form = validForm();
            form.set(entry.getKey(), entry.getValue());
            String field = entry.getKey().equals("cityId") ? "address.city" : entry.getKey();
            mvc.perform(post("/customers/add").params(form))
                    .andExpect(status().isOk())
                    .andExpect(view().name("add_customer"))
                    .andExpect(model().attributeHasFieldErrors("customer", field));
        }
        for (String cityId : new String[]{"", "abc", "-1", "999999999999999999999999"}) {
            MultiValueMap<String, String> form = validForm();
            form.set("cityId", cityId);
            mvc.perform(post("/customers/add").params(form))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeHasFieldErrors("customer", "address.city"));
        }
        MultiValueMap<String, String> form = validForm();
        form.set("height", "abc");
        form.set("birthDate", "2026-02-30");
        mvc.perform(post("/customers/add").params(form))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("customer", "height", "birthDate"));
        mvc.perform(post("/customers/add"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasErrors("customer"));
        assertThat(customers.count()).isEqualTo(customerCount);
        assertThat(addresses.count()).isEqualTo(addressCount);
    }

    @Test
    void editingKeepsUnchangedAddressAndDoesNotChangeSharedAddress() throws Exception {
        Customer first = customers.findAll().getFirst();
        Customer second = customers.findAll().getLast();
        second.setAddress(first.getAddress());
        customers.save(second);
        Long oldAddressId = first.getAddress().getId();
        String oldStreet = first.getAddress().getStreet();
        long addressCount = addresses.count();
        MultiValueMap<String, String> form = validForm();
        form.set("id", first.getId().toString());
        form.set("address.postalCode", first.getAddress().getPostalCode());
        form.set("address.country", first.getAddress().getCountry());
        form.set("address.region", first.getAddress().getRegion());
        form.set("address.district", first.getAddress().getDistrict());
        form.set("address.street", oldStreet);
        form.set("address.house", first.getAddress().getHouse());
        form.set("address.apartment", first.getAddress().getApartment());
        mvc.perform(post("/customers/update").params(form))
                .andExpect(status().is3xxRedirection());
        assertThat(addresses.count()).isEqualTo(addressCount);
        assertThat(customers.findById(first.getId()).orElseThrow().getAddress().getId())
                .isEqualTo(oldAddressId);

        form.set("address.street", "Новая");
        mvc.perform(post("/customers/update").params(form))
                .andExpect(status().is3xxRedirection());
        assertThat(addresses.count()).isEqualTo(addressCount + 1);
        assertThat(customers.findById(first.getId()).orElseThrow().getAddress().getId())
                .isNotEqualTo(oldAddressId);
        assertThat(customers.findById(second.getId()).orElseThrow().getAddress().getId())
                .isEqualTo(oldAddressId);
        assertThat(addresses.findById(oldAddressId).orElseThrow().getStreet()).isEqualTo(oldStreet);
    }

    @Test
    void invalidEditAndIdentifiersDoNotChangeData() throws Exception {
        Customer first = customers.findAll().getFirst();
        String originalName = first.getName();
        MultiValueMap<String, String> form = validForm();
        form.set("id", first.getId().toString());
        form.set("name", "");
        mvc.perform(post("/customers/update").params(form))
                .andExpect(status().isOk())
                .andExpect(view().name("edit_customer"))
                .andExpect(model().attributeHasFieldErrors("customer", "name"));
        assertThat(customers.findById(first.getId()).orElseThrow().getName()).isEqualTo(originalName);
        form.set("id", "abc");
        mvc.perform(post("/customers/update").params(form))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("customer", "id"));
        for (String action : new String[]{"details", "update", "delete"}) {
            mvc.perform(get("/customers/" + action + "/-1")).andExpect(status().isBadRequest());
            mvc.perform(get("/customers/" + action + "/abc")).andExpect(status().isBadRequest());
        }
        mvc.perform(get("/customers").param("city", "x".repeat(256)))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("filter", "city"));
    }

    @Test
    void restCrudPreservesAddressAndReturnsGeneratedIdentifiers() throws Exception {
        long customerCount = customers.count();
        long addressCount = addresses.count();
        String json = validJson(cities.findAll().getFirst().getId());

        mvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.address.id").isNumber())
                .andExpect(jsonPath("$.address.city.name").value("Казань"));

        Customer saved = customers.search("Тестов", "", "", "", "", "").getFirst();
        Long id = saved.getId();
        Long oldAddressId = saved.getAddress().getId();
        assertThat(customers.count()).isEqualTo(customerCount + 1);
        assertThat(addresses.count()).isEqualTo(addressCount + 1);

        mvc.perform(get("/api/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.birthDate").value("2000-01-01"))
                .andExpect(jsonPath("$.address.street").value("Ленина"));

        mvc.perform(put("/api/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.replace("Ленина", "Новая")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address.street").value("Новая"));

        Customer updated = customers.findById(id).orElseThrow();
        assertThat(updated.getAddress().getId()).isNotEqualTo(oldAddressId);
        assertThat(addresses.findById(oldAddressId).orElseThrow().getStreet()).isEqualTo("Ленина");
        assertThat(customers.count()).isEqualTo(customerCount + 1);
        assertThat(addresses.count()).isEqualTo(addressCount + 2);

        mvc.perform(delete("/api/customers/{id}", id))
                .andExpect(status().isNoContent());
        assertThat(customers.existsById(id)).isFalse();
        assertThat(customers.count()).isEqualTo(customerCount);
        mvc.perform(get("/api/customers/{id}", id))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/customers/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void restSearchStatisticsAndInvalidInputWork() throws Exception {
        mvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/api/customers/search")
                        .param("city", "каз").param("street", "баум"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].lastName").value("Иванов"));
        mvc.perform(get("/api/customers/search").param("name", "' OR 1=1 --"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/customers/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCustomers").value(2))
                .andExpect(jsonPath("$.averageHeight").value(174.0))
                .andExpect(jsonPath("$.averageWeight").value(67.85))
                .andExpect(jsonPath("$.customersByCity.Казань").value(2));

        long customerCount = customers.count();
        long addressCount = addresses.count();
        mvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON).content(validJson(999999)))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON).content(validJson(0)))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/customers/-1"))
                .andExpect(status().isBadRequest());
        assertThat(customers.count()).isEqualTo(customerCount);
        assertThat(addresses.count()).isEqualTo(addressCount);
    }

    @Test
    void htmlDeletionKeepsAddressAndHandlesMissingCustomer() throws Exception {
        Customer customer = customers.findAll().getFirst();
        Long addressId = customer.getAddress().getId();

        mvc.perform(get("/customers/delete/{id}", customer.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"));

        assertThat(customers.findById(customer.getId())).isEmpty();
        assertThat(addresses.findById(addressId)).isPresent();
        mvc.perform(get("/customers/delete/{id}", customer.getId()))
                .andExpect(status().is3xxRedirection());
    }

    private String validJson(long cityId) {
        return """
                {
                  "lastName": "Тестов",
                  "name": "Пётр",
                  "patronymic": "Иванович",
                  "gender": "Мужской",
                  "nationality": "Русский",
                  "height": 180,
                  "weight": 75.5,
                  "birthDate": "2000-01-01",
                  "phoneNumber": "+7 999 111-22-33",
                  "creditCardNumber": "2200 1234 5678 9012",
                  "bankAccountNumber": "40817810000001234567",
                  "address": {
                    "postalCode": "420111",
                    "country": "Россия",
                    "region": "Татарстан",
                    "district": "",
                    "street": "Ленина",
                    "house": "1",
                    "apartment": "",
                    "city": {"id": %d}
                  }
                }
                """.formatted(cityId);
    }

    private MultiValueMap<String, String> validForm() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        Map.ofEntries(
                Map.entry("lastName", "Сидоров"), Map.entry("name", "Пётр"),
                Map.entry("patronymic", "Иванович"), Map.entry("gender", "Мужской"),
                Map.entry("nationality", "Русский"), Map.entry("height", "180"),
                Map.entry("weight", "75.5"), Map.entry("birthDate", "2000-01-01"),
                Map.entry("phoneNumber", "+7 999 111-22-33"),
                Map.entry("creditCardNumber", "2200 1234 5678 9012"),
                Map.entry("bankAccountNumber", "40817810000001234567"),
                Map.entry("address.postalCode", "420111"), Map.entry("address.country", "Россия"),
                Map.entry("address.region", "Татарстан"), Map.entry("address.district", ""),
                Map.entry("address.street", "Ленина"), Map.entry("address.house", "1"),
                Map.entry("address.apartment", ""),
                Map.entry("cityId", cities.findAll().getFirst().getId().toString())
        ).forEach(form::add);
        return form;
    }
}
