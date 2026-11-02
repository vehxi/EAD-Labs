package ru.kafpin.lab2.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class Address {

    private Long id;

    @NotBlank(message = "Укажите почтовый индекс")
    @Size(max = 20, message = "Почтовый индекс: не более 20 символов")
    @Pattern(regexp = "[\\p{L}0-9][\\p{L}0-9 -]*",
            message = "Индекс может содержать буквы, цифры, пробелы и дефисы")
    private String postalCode;

    @NotBlank(message = "Укажите страну")
    @Size(max = 255, message = "Страна: не более 255 символов")
    private String country;

    @Size(max = 255, message = "Область: не более 255 символов")
    private String region;

    @Size(max = 255, message = "Район: не более 255 символов")
    private String district;

    @NotBlank(message = "Укажите улицу")
    @Size(max = 255, message = "Улица: не более 255 символов")
    private String street;

    @NotBlank(message = "Укажите номер дома")
    @Size(max = 255, message = "Дом: не более 255 символов")
    private String house;

    @Size(max = 255, message = "Квартира: не более 255 символов")
    private String apartment;

    private City city;

    public Address() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }
}
