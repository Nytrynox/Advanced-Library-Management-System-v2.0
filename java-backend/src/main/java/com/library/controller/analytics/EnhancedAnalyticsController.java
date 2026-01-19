package com.library.controller.analytics;

import com.library.model.Book;
import com.library.model.BookActivity;
import com.library.model.User;
import com.library.service.BookService;
import com.library.repository.UserRepository;
import com.library.repository.BookActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics/advanced")
@CrossOrigin(origins = "http://localhost:5001")
public class EnhancedAnalyticsController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookActivityRepository activityRepository;

    /**
     * Get comprehensive dashboard statistics
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> dashboard = new HashMap<>();
        List<Book> books = bookService.findAllBooks();
        List<User> users = userRepository.findAll();
        List<BookActivity> activities = activityRepository.findAll();

        // Basic metrics
        dashboard.put("totalBooks", books.size());
        dashboard.put("totalUsers", users.size());
        dashboard.put("activeUsers", users.stream().filter(u -> isActiveUser(u, activities)).count());
        dashboard.put("totalBorrows", books.stream().mapToInt(Book::getBorrowCount).sum());
        dashboard.put("availableBooks", books.stream().filter(Book::isAvailable).count());
        
        // Advanced metrics
        dashboard.put("averageRating", calculateAverageRating(books));
        dashboard.put("borrowRate", calculateBorrowRate(books));
        dashboard.put("mostActiveHour", getMostActiveHour(activities));
        dashboard.put("growthRate", calculateGrowthRate(activities));
        
        // Trending data
        dashboard.put("trendingBooks", getTrendingBooks(books, 5));
        dashboard.put("topCategories", getTopCategories(books, 5));
        dashboard.put("recentActivity", getRecentActivities(activities, 10));
        
        // User engagement
        dashboard.put("userEngagement", calculateUserEngagement(users, activities));
        dashboard.put("popularityTrends", getPopularityTrends(books));
        
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Get real-time trending books based on recent activity
     */
    @GetMapping("/trending")
    public ResponseEntity<List<Map<String, Object>>> getTrendingBooks() {
        List<Book> books = bookService.findAllBooks();
        List<Map<String, Object>> trending = getTrendingBooks(books, 10);
        return ResponseEntity.ok(trending);
    }

    /**
     * Get category performance analytics
     */
    @GetMapping("/categories/performance")
    public ResponseEntity<List<Map<String, Object>>> getCategoryPerformance() {
        List<Book> books = bookService.findAllBooks();
        
        Map<String, List<Book>> booksByCategory = books.stream()
            .collect(Collectors.groupingBy(Book::getCategory));
        
        List<Map<String, Object>> performance = booksByCategory.entrySet().stream()
            .map(entry -> {
                String category = entry.getKey();
                List<Book> categoryBooks = entry.getValue();
                
                Map<String, Object> stats = new HashMap<>();
                stats.put("category", category);
                stats.put("totalBooks", categoryBooks.size());
                stats.put("totalBorrows", categoryBooks.stream()
                    .mapToInt(Book::getBorrowCount).sum());
                stats.put("averageRating", categoryBooks.stream()
                    .mapToDouble(Book::getRating).average().orElse(0.0));
                stats.put("availability", (double) categoryBooks.stream()
                    .filter(Book::isAvailable).count() / categoryBooks.size() * 100);
                
                return stats;
            })
            .sorted((a, b) -> Integer.compare(
                (Integer) b.get("totalBorrows"), 
                (Integer) a.get("totalBorrows")))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(performance);
    }

    /**
     * Get hourly borrowing patterns for optimization
     */
    @GetMapping("/patterns/hourly")
    public ResponseEntity<Map<String, Object>> getHourlyPatterns() {
        List<BookActivity> activities = activityRepository.findAll();
        
        Map<Integer, Long> hourlyDistribution = activities.stream()
            .collect(Collectors.groupingBy(
                activity -> activity.getTimestamp().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .getHour(),
                Collectors.counting()
            ));
        
        Map<String, Object> result = new HashMap<>();
        result.put("hourlyData", hourlyDistribution);
        result.put("peakHour", hourlyDistribution.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(12));
        
        return ResponseEntity.ok(result);
    }

    /**
     * Get user engagement metrics
     */
    @GetMapping("/users/engagement")
    public ResponseEntity<Map<String, Object>> getUserEngagement() {
        List<User> users = userRepository.findAll();
        List<BookActivity> activities = activityRepository.findAll();
        
        Map<String, Object> engagement = calculateUserEngagement(users, activities);
        return ResponseEntity.ok(engagement);
    }

    /**
     * Get predictive insights
     */
    @GetMapping("/predictions")
    public ResponseEntity<Map<String, Object>> getPredictions() {
        List<Book> books = bookService.findAllBooks();
        List<BookActivity> activities = activityRepository.findAll();
        
        Map<String, Object> predictions = new HashMap<>();
        predictions.put("booksNeedingMaintenance", predictMaintenance(books));
        predictions.put("upcomingPopularBooks", predictUpcomingPopular(books));
        predictions.put("categoryTrends", predictCategoryTrends(books, activities));
        
        return ResponseEntity.ok(predictions);
    }

    // Helper methods

    private double calculateAverageRating(List<Book> books) {
        return books.stream()
            .mapToDouble(Book::getRating)
            .average()
            .orElse(0.0);
    }

    private double calculateBorrowRate(List<Book> books) {
        long borrowedBooks = books.stream().filter(b -> !b.isAvailable()).count();
        return books.isEmpty() ? 0.0 : (double) borrowedBooks / books.size() * 100;
    }

    private int getMostActiveHour(List<BookActivity> activities) {
        Map<Integer, Long> hourlyActivity = activities.stream()
            .collect(Collectors.groupingBy(
                activity -> activity.getTimestamp().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .getHour(),
                Collectors.counting()
            ));
        
        return hourlyActivity.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(12);
    }

    private double calculateGrowthRate(List<BookActivity> activities) {
        // Calculate weekly growth rate based on activities
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastWeek = now.minusWeeks(1);
        LocalDateTime twoWeeksAgo = now.minusWeeks(2);
        
        long thisWeek = activities.stream()
            .filter(a -> toLocalDateTime(a.getTimestamp()).isAfter(lastWeek))
            .count();
        
        long previousWeek = activities.stream()
            .filter(a -> {
                LocalDateTime activityTime = toLocalDateTime(a.getTimestamp());
                return activityTime.isAfter(twoWeeksAgo) && activityTime.isBefore(lastWeek);
            })
            .count();
        
        return previousWeek == 0 ? 100.0 : ((double) (thisWeek - previousWeek) / previousWeek) * 100;
    }

    private List<Map<String, Object>> getTrendingBooks(List<Book> books, int limit) {
        return books.stream()
            .sorted(Comparator.comparingInt(Book::getBorrowCount).reversed())
            .limit(limit)
            .map(book -> {
                Map<String, Object> bookData = new HashMap<>();
                bookData.put("id", book.getId());
                bookData.put("title", book.getTitle());
                bookData.put("author", book.getAuthor());
                bookData.put("category", book.getCategory());
                bookData.put("borrowCount", book.getBorrowCount());
                bookData.put("rating", book.getRating());
                bookData.put("trendScore", calculateTrendScore(book));
                return bookData;
            })
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getTopCategories(List<Book> books, int limit) {
        Map<String, Integer> categoryBorrows = books.stream()
            .collect(Collectors.groupingBy(
                Book::getCategory,
                Collectors.summingInt(Book::getBorrowCount)
            ));
        
        return categoryBorrows.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(limit)
            .map(entry -> {
                Map<String, Object> category = new HashMap<>();
                category.put("name", entry.getKey());
                category.put("totalBorrows", entry.getValue());
                return category;
            })
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getRecentActivities(List<BookActivity> activities, int limit) {
        return activities.stream()
            .sorted(Comparator.comparing(BookActivity::getTimestamp).reversed())
            .limit(limit)
            .map(activity -> {
                Map<String, Object> activityData = new HashMap<>();
                activityData.put("book_title", activity.getBook() != null ? activity.getBook().getTitle() : "Unknown");
                activityData.put("action", activity.getAction().toString());
                activityData.put("user", activity.getUser() != null ? activity.getUser().getUsername() : "Unknown");
                activityData.put("timestamp", activity.getTimestamp().getTime());
                activityData.put("status", activity.getStatus());
                return activityData;
            })
            .collect(Collectors.toList());
    }

    private Map<String, Object> calculateUserEngagement(List<User> users, List<BookActivity> activities) {
        Map<String, Object> engagement = new HashMap<>();
        
        long activeUsers = users.stream()
            .filter(u -> isActiveUser(u, activities))
            .count();
        
        double engagementRate = users.isEmpty() ? 0.0 : (double) activeUsers / users.size() * 100;
        
        engagement.put("totalUsers", users.size());
        engagement.put("activeUsers", activeUsers);
        engagement.put("engagementRate", engagementRate);
        engagement.put("averageActivitiesPerUser", 
            users.isEmpty() ? 0.0 : (double) activities.size() / users.size());
        
        return engagement;
    }

    private boolean isActiveUser(User user, List<BookActivity> activities) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return activities.stream()
            .anyMatch(a -> a.getUser() != null && 
                          a.getUser().getId().equals(user.getId()) &&
                          toLocalDateTime(a.getTimestamp()).isAfter(thirtyDaysAgo));
    }

    private Map<String, Object> getPopularityTrends(List<Book> books) {
        Map<String, Object> trends = new HashMap<>();
        
        // Rising stars - books with high borrow count relative to age
        List<Map<String, Object>> risingStars = books.stream()
            .filter(b -> b.getBorrowCount() > 20)
            .sorted((a, b) -> Double.compare(calculateTrendScore(b), calculateTrendScore(a)))
            .limit(5)
            .map(book -> {
                Map<String, Object> data = new HashMap<>();
                data.put("title", book.getTitle());
                data.put("trendScore", calculateTrendScore(book));
                return data;
            })
            .collect(Collectors.toList());
        
        trends.put("risingStars", risingStars);
        return trends;
    }

    private double calculateTrendScore(Book book) {
        // Simple trending algorithm: borrowCount * rating * recency_factor
        return book.getBorrowCount() * book.getRating() * 1.2;
    }

    private List<Map<String, Object>> predictMaintenance(List<Book> books) {
        // Books that have been borrowed heavily might need maintenance
        return books.stream()
            .filter(b -> b.getBorrowCount() > 50)
            .sorted(Comparator.comparingInt(Book::getBorrowCount).reversed())
            .limit(10)
            .map(book -> {
                Map<String, Object> data = new HashMap<>();
                data.put("title", book.getTitle());
                data.put("borrowCount", book.getBorrowCount());
                data.put("priority", book.getBorrowCount() > 100 ? "HIGH" : "MEDIUM");
                return data;
            })
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> predictUpcomingPopular(List<Book> books) {
        // Books with high ratings but moderate borrow counts might become popular
        return books.stream()
            .filter(b -> b.getRating() > 4.5 && b.getBorrowCount() < 30)
            .sorted(Comparator.comparingDouble(Book::getRating).reversed())
            .limit(5)
            .map(book -> {
                Map<String, Object> data = new HashMap<>();
                data.put("title", book.getTitle());
                data.put("rating", book.getRating());
                data.put("potential", "HIGH");
                return data;
            })
            .collect(Collectors.toList());
    }

    private Map<String, Object> predictCategoryTrends(List<Book> books, List<BookActivity> activities) {
        // Analyze category growth over time
        Map<String, Object> trends = new HashMap<>();
        
        Map<String, Long> categoryActivity = books.stream()
            .collect(Collectors.groupingBy(
                Book::getCategory,
                Collectors.summingLong(Book::getBorrowCount)
            ));
        
        String growingCategory = categoryActivity.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("FICTION");
        
        trends.put("growingCategory", growingCategory);
        trends.put("categoryDistribution", categoryActivity);
        
        return trends;
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
    }
}
