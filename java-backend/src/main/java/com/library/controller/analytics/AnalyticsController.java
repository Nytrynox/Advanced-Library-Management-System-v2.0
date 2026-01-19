package com.library.controller.analytics;

import com.library.model.Book;
import com.library.model.BookActivity;
import com.library.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = {"http://localhost:5000", "http://localhost:5001"})
public class AnalyticsController {

    @Autowired
    private BookService bookService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getBasicStats() {
        Map<String, Object> stats = new HashMap<>();
        List<Book> books = bookService.findAllBooks();
        
        stats.put("totalBooks", books.size());
        stats.put("availableBooks", books.stream().filter(book -> !book.isBorrowed()).count());
        stats.put("totalBorrows", books.stream().mapToInt(Book::getBorrowCount).sum());
        
        // Calculate average rating
        double avgRating = books.stream()
                              .mapToDouble(Book::getRating)
                              .average()
                              .orElse(0.0);
        stats.put("averageRating", avgRating);
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/categories")
    public ResponseEntity<Map<String, Long>> getCategoryDistribution() {
        List<Book> books = bookService.findAllBooks();
        
        Map<String, Long> categoryCount = books.stream()
                .filter(book -> book.getCategory() != null && !book.getCategory().isEmpty())
                .collect(Collectors.groupingBy(
                    Book::getCategory,
                    Collectors.counting()
                ));
                
        return ResponseEntity.ok(categoryCount);
    }

    @GetMapping("/popular-books")
    public ResponseEntity<List<Map<String, Object>>> getPopularBooks() {
        List<Book> books = bookService.findAllBooks();
        
        List<Map<String, Object>> popularBooks = books.stream()
                .sorted(Comparator.comparingInt(Book::getBorrowCount).reversed())
                .limit(5)  // Top 5 books
                .map(book -> {
                    Map<String, Object> bookData = new HashMap<>();
                    bookData.put("title", book.getTitle());
                    bookData.put("borrowCount", book.getBorrowCount());
                    return bookData;
                })
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(popularBooks);
    }

    @GetMapping("/recent-activities")
    public ResponseEntity<List<Map<String, Object>>> getRecentActivities() {
        List<BookActivity> activities = bookService.getRecentActivities();
        
        List<Map<String, Object>> formattedActivities = activities.stream()
                .map(activity -> {
                    Map<String, Object> activityData = new HashMap<>();
                    activityData.put("book_title", activity.getBook().getTitle());
                    activityData.put("action", activity.getAction().toString());
                    activityData.put("user", activity.getUser().getUsername());
                    activityData.put("timestamp", activity.getTimestamp().getTime());
                    activityData.put("status", activity.getStatus());
                    return activityData;
                })
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(formattedActivities);
    }
}
