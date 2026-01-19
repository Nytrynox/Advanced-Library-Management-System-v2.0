package com.library.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

@Document(collection = "books")
public class Book {
    @Id
    private String id;
    private String title;
    private String author;
    private String isbn;
    private String category;
    private int quantity;
    private boolean available;
    private boolean borrowed;
    private String description;
    private String location;
    private String publisher;
    private int publishYear;
    private Date publicationDate;
    private int pages;
    private List<String> tags;
    private double rating;
    private int totalRatings;
    private List<Review> reviews;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String coverImageUrl;
    private int borrowCount;
    private Language language;
    private List<String> relatedBooks;
    
    // Constructors
    public Book() {
        this.tags = new ArrayList<>();
        this.reviews = new ArrayList<>();
        this.relatedBooks = new ArrayList<>();
        this.available = true;
        this.rating = 0.0;
        this.totalRatings = 0;
        this.borrowCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    
    public int getPublishYear() { return publishYear; }
    public void setPublishYear(int publishYear) { this.publishYear = publishYear; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    
    public int getTotalRatings() { return totalRatings; }
    public void setTotalRatings(int totalRatings) { this.totalRatings = totalRatings; }
    
    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
    
    public int getBorrowCount() { return borrowCount; }
    public void setBorrowCount(int borrowCount) { this.borrowCount = borrowCount; }
    
    public Language getLanguage() { return language; }
    public void setLanguage(Language language) { this.language = language; }
    
    public List<String> getRelatedBooks() { return relatedBooks; }
    public void setRelatedBooks(List<String> relatedBooks) { this.relatedBooks = relatedBooks; }
    
    public Date getPublicationDate() { return publicationDate; }
    public void setPublicationDate(Date publicationDate) { this.publicationDate = publicationDate; }
    
    public int getPages() { return pages; }
    public void setPages(int pages) { this.pages = pages; }
    
    public boolean isBorrowed() { return borrowed; }
    public void setBorrowed(boolean borrowed) { this.borrowed = borrowed; }
    
    // Helper methods
    public void incrementBorrowCount() { this.borrowCount++; }
}

enum Language {
    ENGLISH, SPANISH, FRENCH, GERMAN, CHINESE, JAPANESE, OTHER
}
