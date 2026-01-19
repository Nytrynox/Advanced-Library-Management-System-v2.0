package com.library.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import java.util.Date;

@Document(collection = "book_activities")
public class BookActivity {
    @Id
    private String id;

    @DBRef
    private Book book;

    @DBRef
    private User user;

    private ActivityType action;
    private Date timestamp;
    private String status;

    public enum ActivityType {
        BORROWED, RETURNED, RESERVED, RENEWED
    }
    
    // Constructors
    public BookActivity() {
        this.timestamp = new Date();
    }
    
    public BookActivity(Book book, User user, ActivityType action) {
        this.book = book;
        this.user = user;
        this.action = action;
        this.timestamp = new Date();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ActivityType getAction() {
        return action;
    }

    public void setAction(ActivityType action) {
        this.action = action;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
