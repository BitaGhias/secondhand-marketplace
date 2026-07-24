package com.secondhand.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "ratings",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"rater_id", "item_id"})
        }
)
/**
 * JPA entity representing a "rating" record in the database.
 * <p>
 * This class defines the structure of the matching table in the SQLite database and is managed by Hibernate; relations between tables are declared with JPA annotations.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int score;

    private String comment;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne
    @JoinColumn(name = "rater_id", nullable = false)
    private User rater;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    /**
     * Creates a new {@code Rating} instance.
     */
    public Rating() {}

    public Rating(Long id, int score, String comment, Item item, User rater, User seller) {
        this.id = id;
        this.score = score;
        this.comment = comment;
        this.item = item;
        this.rater = rater;
        this.seller = seller;
    }

    public Long getId() { return id; }
    public int getScore() { return score; }
    public String getComment() { return comment; }
    public Item getItem() { return item; }
    public User getRater() { return rater; }
    public User getSeller() { return seller; }

    public void setId(Long id) { this.id = id; }
    public void setScore(int score) { this.score = score; }
    public void setComment(String comment) { this.comment = comment; }
    public void setItem(Item item) { this.item = item; }
    public void setRater(User rater) { this.rater = rater; }
    public void setSeller(User seller) { this.seller = seller; }
}