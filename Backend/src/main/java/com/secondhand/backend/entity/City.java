package com.secondhand.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;

    @Column(nullable = false, unique = true)
    public String name;
}
