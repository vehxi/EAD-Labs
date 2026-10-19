package ru.kafpin.lab2.controller;

import jakarta.validation.constraints.Size;

public class CustomerFilter {

    @Size(max = 255, message = "Фамилия: не более 255 символов")
    private String lastName = "";

    @Size(max = 255, message = "Имя: не более 255 символов")
    private String name = "";

    @Size(max = 255, message = "Пол: не более 255 символов")
    private String gender = "";

    @Size(max = 255, message = "Национальность: не более 255 символов")
    private String nationality = "";

    @Size(max = 255, message = "Город: не более 255 символов")
    private String city = "";

    @Size(max = 255, message = "Улица: не более 255 символов")
    private String street = "";

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
}
