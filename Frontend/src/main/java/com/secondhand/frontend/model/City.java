package com.secondhand.frontend.model;

/**
 * Client-side model representing "city" data returned by the server.
 * <p>
 * This class is the client-side representation of data received from the server and is deserialized from JSON by Jackson.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class City {
    private Long id;
    private String name;

    /**
     * Creates a new {@code City} instance.
     */
    public City() {}

    public City(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Gets id.
     *
     * @return the resulting numeric value
     */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return name != null ? name : "";
    }
}