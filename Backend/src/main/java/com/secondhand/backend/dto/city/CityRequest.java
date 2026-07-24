package com.secondhand.backend.dto.city;

/**
 * Data Transfer Object carrying "city request" data between client and server.
 * <p>
 * This class is used purely for transferring data between client and server and is not mapped to the database directly, keeping the internal structure of the entities hidden from the client.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class CityRequest {
    private String name;

    public CityRequest() {}
    public CityRequest(String name) { this.name = name; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}