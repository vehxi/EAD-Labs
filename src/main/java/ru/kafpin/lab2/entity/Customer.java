package ru.kafpin.lab2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Positive(message = "ID должен быть положительным")
    private Long id;

    @Column(name = "last_name", nullable = false)
    @NotBlank(message = "Укажите фамилию")
    @Size(max = 255, message = "Фамилия: не более 255 символов")
    private String lastName;

    @Column(name = "name", nullable = false)
    @NotBlank(message = "Укажите имя")
    @Size(max = 255, message = "Имя: не более 255 символов")
    private String name;

    @Column(name = "patronymic", nullable = false)
    @NotBlank(message = "Укажите отчество")
    @Size(max = 255, message = "Отчество: не более 255 символов")
    private String patronymic;

    @Column(name = "gender", nullable = false)
    @NotBlank(message = "Укажите пол")
    @Pattern(regexp = "Мужской|Женский", message = "Выберите пол из списка")
    private String gender;

    @Column(name = "nationality", nullable = false)
    @NotBlank(message = "Укажите национальность")
    @Size(max = 255, message = "Национальность: не более 255 символов")
    private String nationality;

    @Column(name = "height", nullable = false)
    @NotNull(message = "Укажите рост")
    @Min(value = 1, message = "Рост должен быть не менее 1 см")
    @Max(value = 300, message = "Рост должен быть не более 300 см")
    private Integer height;

    @Column(name = "weight", nullable = false)
    @NotNull(message = "Укажите вес")
    @DecimalMin(value = "0.1", message = "Вес должен быть не менее 0,1 кг")
    @DecimalMax(value = "1000", message = "Вес должен быть не более 1000 кг")
    private Double weight;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "birth_date", nullable = false)
    @NotNull(message = "Укажите дату рождения")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthDate;

    @Column(name = "phone_number", nullable = false)
    @NotBlank(message = "Укажите номер телефона")
    @Size(max = 30, message = "Телефон: не более 30 символов")
    @Pattern(regexp = "\\+?(?=(?:[^0-9]*[0-9]){7,15}[^0-9]*$)[0-9 ()-]+",
            message = "Телефон: от 7 до 15 цифр; допустимы +, пробелы, скобки и дефисы")
    private String phoneNumber;

    @Column(name = "credit_card_number", nullable = false)
    @NotBlank(message = "Укажите номер карты")
    @Size(max = 30, message = "Номер карты: не более 30 символов")
    @Pattern(regexp = "[0-9](?:[ -]?[0-9]){12,18}",
            message = "Номер карты: от 13 до 19 цифр, допустимы пробелы и дефисы")
    private String creditCardNumber;

    @Column(name = "bank_account_number", nullable = false)
    @NotBlank(message = "Укажите номер банковского счёта")
    @Pattern(regexp = "[0-9]{20}", message = "Номер счёта должен содержать 20 цифр")
    private String bankAccountNumber;

    public Customer() {
    }

    @Valid
    @NotNull(message = "Укажите домашний адрес")
    @ManyToOne(optional = false)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address = new Address();

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }
}
