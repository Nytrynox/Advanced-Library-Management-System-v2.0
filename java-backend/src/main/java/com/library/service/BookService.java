package com.library.service;

import com.library.model.Book;
import com.library.model.BookActivity;
import com.library.model.Review;
import com.library.repository.BookRepository;
import com.library.repository.BookActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final BookActivityRepository bookActivityRepository;
    
    @Autowired
    public BookService(BookRepository bookRepository, BookActivityRepository bookActivityRepository) {
        this.bookRepository = bookRepository;
        this.bookActivityRepository = bookActivityRepository;
    }

    public List<Book> getAllBooks(Integer page, Integer size, String sortBy) {
        if (page != null && size != null) {
            Sort sort = sortBy != null ? Sort.by(sortBy) : Sort.by("title");
            return bookRepository.findAll(PageRequest.of(page, size, sort)).getContent();
        }
        return bookRepository.findAll();
    }
    
    // Add method for analytics
    public List<Book> findAllBooks() {
        return bookRepository.findAll();
    }
    
    // Add method for recent activities
    public List<BookActivity> getRecentActivities() {
        // Return empty list if no activities found
        try {
            return bookActivityRepository.findTop10ByOrderByTimestampDesc();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public Optional<Book> getBookById(String id) {
        return bookRepository.findById(id);
    }

    public List<Book> searchBooks(String title, String author, String isbn, List<String> tags) {
        // Implement advanced search logic
        return bookRepository.findAll().stream()
                .filter(book -> (title == null || book.getTitle().toLowerCase().contains(title.toLowerCase())) &&
                        (author == null || book.getAuthor().toLowerCase().contains(author.toLowerCase())) &&
                        (isbn == null || book.getIsbn().equals(isbn)) &&
                        (tags == null || book.getTags() != null && book.getTags().containsAll(tags)))
                .collect(Collectors.toList());
    }

    public Book addBook(Book book) {
        book.setCreatedAt(LocalDateTime.now());
        book.setUpdatedAt(LocalDateTime.now());
        book.setAvailable(true);
        book.setBorrowCount(0);
        book.setRating(0.0);
        book.setTotalRatings(0);
        book.setReviews(new ArrayList<>());
        return bookRepository.save(book);
    }

    public Optional<Book> updateBook(String id, Book book) {
        return bookRepository.findById(id)
                .map(existingBook -> {
                    book.setId(id);
                    book.setUpdatedAt(LocalDateTime.now());
                    book.setCreatedAt(existingBook.getCreatedAt());
                    book.setBorrowCount(existingBook.getBorrowCount());
                    return bookRepository.save(book);
                });
    }

    public void deleteBook(String id) {
        bookRepository.deleteById(id);
    }

    public List<Book> getBooksByCategory(String category) {
        return bookRepository.findByCategory(category);
    }

    public List<Book> getAvailableBooks() {
        return bookRepository.findByAvailable(true);
    }

    public Review addReview(String bookId, Review review) {
        return bookRepository.findById(bookId)
                .map(book -> {
                    review.setCreatedAt(LocalDateTime.now());
                    review.setUpdatedAt(LocalDateTime.now());
                    review.setBookId(bookId);
                    review.setLikes(0);
                    
                    // Update book rating
                    double currentRating = book.getRating();
                    int totalRatings = book.getTotalRatings();
                    double newRating = ((currentRating * totalRatings) + review.getRating()) / (totalRatings + 1);
                    
                    book.setRating(newRating);
                    book.setTotalRatings(totalRatings + 1);
                    book.getReviews().add(review);
                    bookRepository.save(book);
                    
                    return review;
                })
                .orElseThrow(() -> new RuntimeException("Book not found"));
    }

    public List<Review> getBookReviews(String bookId) {
        return bookRepository.findById(bookId)
                .map(Book::getReviews)
                .orElse(Collections.emptyList());
    }

    public List<Book> getRecommendations(String userId, String category) {
        // Implement recommendation logic based on user's borrowing history and preferences
        List<Book> recommendations = new ArrayList<>();
        if (category != null) {
            recommendations.addAll(bookRepository.findByCategory(category));
        }
        
        // Sort by rating and popularity
        recommendations.sort((b1, b2) -> {
            double score1 = b1.getRating() * Math.log(1 + b1.getBorrowCount());
            double score2 = b2.getRating() * Math.log(1 + b2.getBorrowCount());
            return Double.compare(score2, score1);
        });
        
        return recommendations.stream()
                .limit(10)
                .collect(Collectors.toList());
    }

    public List<Book> getPopularBooks(String category, int limit) {
        List<Book> books = category != null ? 
                bookRepository.findByCategory(category) :
                bookRepository.findAll();
                
        return books.stream()
                .sorted((b1, b2) -> Integer.compare(b2.getBorrowCount(), b1.getBorrowCount()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getAnalytics(String timeFrame) {
        Map<String, Object> analytics = new HashMap<>();
        List<Book> allBooks = bookRepository.findAll();
        
        analytics.put("totalBooks", allBooks.size());
        analytics.put("availableBooks", allBooks.stream().filter(Book::isAvailable).count());
        analytics.put("totalBorrows", allBooks.stream().mapToInt(Book::getBorrowCount).sum());
        analytics.put("averageRating", allBooks.stream()
                .mapToDouble(Book::getRating)
                .average()
                .orElse(0.0));
        
        Map<String, Long> categoryDistribution = allBooks.stream()
                .collect(Collectors.groupingBy(Book::getCategory, Collectors.counting()));
        analytics.put("categoryDistribution", categoryDistribution);
        
        return analytics;
    }

    public Optional<Book> borrowBook(String id, String userId) {
        return bookRepository.findById(id)
                .map(book -> {
                    if (!book.isAvailable()) {
                        throw new RuntimeException("Book is not available");
                    }
                    book.setAvailable(false);
                    book.setBorrowCount(book.getBorrowCount() + 1);
                    book.setUpdatedAt(LocalDateTime.now());
                    return bookRepository.save(book);
                });
    }

    public Optional<Book> returnBook(String id, String userId) {
        return bookRepository.findById(id)
                .map(book -> {
                    book.setAvailable(true);
                    book.setUpdatedAt(LocalDateTime.now());
                    return bookRepository.save(book);
                });
    }
}
