package com.library.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "reviews")
public class Review {
    @Id
    private String id;
    private String userId;
    private String bookId;
    private String username;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int likes;
    private boolean verified; // Indicates if the reviewer has actually borrowed the book
    
    // Constructors
    public Review() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.likes = 0;
        this.verified = false;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }
    
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
}
