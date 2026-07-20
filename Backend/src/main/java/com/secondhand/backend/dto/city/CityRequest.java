package com.secondhand.backend.dto.city;

public class CityRequest {
    private String name;

    public CityRequest() {}
    public CityRequest(String name) { this.name = name; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}