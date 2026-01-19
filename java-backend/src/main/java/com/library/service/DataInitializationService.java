package com.library.service;

import com.library.model.Book;
import com.library.model.User;
import com.library.model.BookActivity;
import com.library.model.Review;
import com.library.repository.BookRepository;
import com.library.repository.UserRepository;
import com.library.repository.BookActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class DataInitializationService implements CommandLineRunner {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookActivityRepository activityRepository;

    @Autowired(required = false)
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Check if data already exists
        if (bookRepository.count() > 0) {
            System.out.println("✅ Database already contains data. Skipping initialization.");
            return;
        }

        System.out.println("🚀 Initializing database with realistic library data...");
        
        initializeUsers();
        initializeBooks();
        initializeActivities();
        
        System.out.println("✅ Database initialization complete!");
    }

    private void initializeUsers() {
        List<User> users = Arrays.asList(
            createUser("admin", "admin@library.com", "Admin User", "ADMIN"),
            createUser("john_doe", "john@email.com", "John Doe", "USER"),
            createUser("jane_smith", "jane@email.com", "Jane Smith", "USER"),
            createUser("mike_wilson", "mike@email.com", "Mike Wilson", "USER"),
            createUser("sarah_jones", "sarah@email.com", "Sarah Jones", "USER"),
            createUser("david_brown", "david@email.com", "David Brown", "USER"),
            createUser("emma_davis", "emma@email.com", "Emma Davis", "USER"),
            createUser("alex_miller", "alex@email.com", "Alex Miller", "USER"),
            createUser("lisa_taylor", "lisa@email.com", "Lisa Taylor", "LIBRARIAN"),
            createUser("tom_anderson", "tom@email.com", "Tom Anderson", "USER")
        );

        userRepository.saveAll(users);
        System.out.println("✅ Created " + users.size() + " users");
    }

    private User createUser(String username, String email, String fullName, String role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder != null ? passwordEncoder.encode("password123") : "password123");
        user.setFullName(fullName);
        user.setRole(role);
        user.setActive(true);
        user.setBorrowedBooks(new ArrayList<>());
        return user;
    }

    private void initializeBooks() {
        List<Book> books = new ArrayList<>();

        // Classic Literature
        books.add(createBook(
            "To Kill a Mockingbird",
            "Harper Lee",
            "9780061120084",
            "FICTION",
            "A gripping tale of racial injustice and childhood innocence in the Deep South.",
            LocalDate.of(1960, 7, 11),
            4.8,
            324
        ));

        books.add(createBook(
            "1984",
            "George Orwell",
            "9780451524935",
            "SCIENCE_FICTION",
            "A dystopian social science fiction novel and cautionary tale about the dangers of totalitarianism.",
            LocalDate.of(1949, 6, 8),
            4.7,
            328
        ));

        books.add(createBook(
            "Pride and Prejudice",
            "Jane Austen",
            "9780141439518",
            "ROMANCE",
            "A romantic novel of manners that critiques the British landed gentry at the end of the 18th century.",
            LocalDate.of(1813, 1, 28),
            4.6,
            432
        ));

        books.add(createBook(
            "The Great Gatsby",
            "F. Scott Fitzgerald",
            "9780743273565",
            "FICTION",
            "A tragic love story set in the Jazz Age, exploring themes of decadence, idealism, and excess.",
            LocalDate.of(1925, 4, 10),
            4.5,
            180
        ));

        books.add(createBook(
            "The Catcher in the Rye",
            "J.D. Salinger",
            "9780316769488",
            "FICTION",
            "A story about teenage rebellion and alienation narrated by Holden Caulfield.",
            LocalDate.of(1951, 7, 16),
            4.3,
            277
        ));

        // Science Fiction & Fantasy
        books.add(createBook(
            "Dune",
            "Frank Herbert",
            "9780441013593",
            "SCIENCE_FICTION",
            "A science fiction masterpiece about politics, religion, and ecology on a desert planet.",
            LocalDate.of(1965, 8, 1),
            4.9,
            688
        ));

        books.add(createBook(
            "The Hobbit",
            "J.R.R. Tolkien",
            "9780547928227",
            "FANTASY",
            "A fantasy adventure following Bilbo Baggins on his unexpected journey.",
            LocalDate.of(1937, 9, 21),
            4.8,
            310
        ));

        books.add(createBook(
            "Neuromancer",
            "William Gibson",
            "9780441569595",
            "SCIENCE_FICTION",
            "A groundbreaking cyberpunk novel that defined the genre.",
            LocalDate.of(1984, 7, 1),
            4.4,
            271
        ));

        books.add(createBook(
            "The Foundation",
            "Isaac Asimov",
            "9780553293357",
            "SCIENCE_FICTION",
            "A masterwork of science fiction about the fall and rise of civilizations.",
            LocalDate.of(1951, 6, 1),
            4.7,
            255
        ));

        books.add(createBook(
            "Ender's Game",
            "Orson Scott Card",
            "9780812550702",
            "SCIENCE_FICTION",
            "A military science fiction novel about a child genius trained to save humanity.",
            LocalDate.of(1985, 1, 15),
            4.6,
            324
        ));

        // Modern Fiction
        books.add(createBook(
            "The Kite Runner",
            "Khaled Hosseini",
            "9781594631931",
            "FICTION",
            "A powerful story of friendship, betrayal, and redemption set in Afghanistan.",
            LocalDate.of(2003, 5, 29),
            4.7,
            371
        ));

        books.add(createBook(
            "The Book Thief",
            "Markus Zusak",
            "9780375842207",
            "HISTORICAL_FICTION",
            "A story about a young girl living in Nazi Germany who steals books.",
            LocalDate.of(2005, 3, 14),
            4.8,
            552
        ));

        books.add(createBook(
            "Life of Pi",
            "Yann Martel",
            "9780156027328",
            "ADVENTURE",
            "A philosophical novel about a boy stranded on a lifeboat with a Bengal tiger.",
            LocalDate.of(2001, 9, 11),
            4.5,
            460
        ));

        // Mystery & Thriller
        books.add(createBook(
            "The Da Vinci Code",
            "Dan Brown",
            "9780307474278",
            "MYSTERY",
            "A thriller involving art, conspiracy theories, and religious history.",
            LocalDate.of(2003, 3, 18),
            4.2,
            689
        ));

        books.add(createBook(
            "Gone Girl",
            "Gillian Flynn",
            "9780307588371",
            "THRILLER",
            "A psychological thriller about a marriage gone terribly wrong.",
            LocalDate.of(2012, 6, 5),
            4.4,
            559
        ));

        books.add(createBook(
            "The Girl with the Dragon Tattoo",
            "Stieg Larsson",
            "9780307454546",
            "MYSTERY",
            "A gripping mystery involving murder, corruption, and family secrets.",
            LocalDate.of(2005, 8, 1),
            4.5,
            465
        ));

        // Non-Fiction
        books.add(createBook(
            "Sapiens: A Brief History of Humankind",
            "Yuval Noah Harari",
            "9780062316097",
            "NON_FICTION",
            "An exploration of the history and impact of Homo sapiens on the world.",
            LocalDate.of(2011, 1, 1),
            4.9,
            464
        ));

        books.add(createBook(
            "Educated",
            "Tara Westover",
            "9780399590504",
            "BIOGRAPHY",
            "A memoir about a young woman who grows up in a survivalist family and goes on to earn a PhD.",
            LocalDate.of(2018, 2, 20),
            4.8,
            334
        ));

        books.add(createBook(
            "Atomic Habits",
            "James Clear",
            "9780735211292",
            "SELF_HELP",
            "A practical guide to breaking bad habits and building good ones.",
            LocalDate.of(2018, 10, 16),
            4.7,
            320
        ));

        books.add(createBook(
            "Thinking, Fast and Slow",
            "Daniel Kahneman",
            "9780374533557",
            "PSYCHOLOGY",
            "An exploration of the two systems that drive the way we think.",
            LocalDate.of(2011, 10, 25),
            4.6,
            499
        ));

        // Contemporary Bestsellers
        books.add(createBook(
            "The Midnight Library",
            "Matt Haig",
            "9780525559474",
            "FICTION",
            "A novel about a library between life and death where every book is a different life you could have lived.",
            LocalDate.of(2020, 8, 13),
            4.5,
            304
        ));

        books.add(createBook(
            "Project Hail Mary",
            "Andy Weir",
            "9780593135204",
            "SCIENCE_FICTION",
            "A lone astronaut must save Earth and humanity from extinction.",
            LocalDate.of(2021, 5, 4),
            4.9,
            476
        ));

        books.add(createBook(
            "The Silent Patient",
            "Alex Michaelides",
            "9781250301697",
            "THRILLER",
            "A psychological thriller about a woman who shoots her husband and then never speaks again.",
            LocalDate.of(2019, 2, 5),
            4.4,
            336
        ));

        books.add(createBook(
            "Where the Crawdads Sing",
            "Delia Owens",
            "9780735219090",
            "FICTION",
            "A coming-of-age mystery about a girl raised in the marshlands of North Carolina.",
            LocalDate.of(2018, 8, 14),
            4.7,
            384
        ));

        books.add(createBook(
            "The Seven Husbands of Evelyn Hugo",
            "Taylor Jenkins Reid",
            "9781501161933",
            "HISTORICAL_FICTION",
            "An aging Hollywood starlet finally tells the truth about her glamorous and scandalous life.",
            LocalDate.of(2017, 6, 13),
            4.8,
            400
        ));

        // Technology & Business
        books.add(createBook(
            "The Lean Startup",
            "Eric Ries",
            "9780307887894",
            "BUSINESS",
            "A revolutionary approach to building and launching successful startups.",
            LocalDate.of(2011, 9, 13),
            4.5,
            336
        ));

        books.add(createBook(
            "Zero to One",
            "Peter Thiel",
            "9780804139298",
            "BUSINESS",
            "Notes on startups and how to build the future.",
            LocalDate.of(2014, 9, 16),
            4.6,
            224
        ));

        books.add(createBook(
            "The Innovators",
            "Walter Isaacson",
            "9781476708706",
            "TECHNOLOGY",
            "The history of the digital revolution and the hackers, geniuses, and geeks who created it.",
            LocalDate.of(2014, 10, 7),
            4.4,
            560
        ));

        books.add(createBook(
            "AI Superpowers",
            "Kai-Fu Lee",
            "9781328546395",
            "TECHNOLOGY",
            "A look at how artificial intelligence will reshape the world.",
            LocalDate.of(2018, 9, 25),
            4.5,
            272
        ));

        books.add(createBook(
            "The Code Breaker",
            "Walter Isaacson",
            "9781982115852",
            "BIOGRAPHY",
            "Jennifer Doudna, gene editing, and the future of the human race.",
            LocalDate.of(2021, 3, 9),
            4.7,
            560
        ));

        // Add more variety - Young Adult
        books.add(createBook(
            "The Hunger Games",
            "Suzanne Collins",
            "9780439023481",
            "YOUNG_ADULT",
            "In a dystopian future, a girl must fight to the death in a televised competition.",
            LocalDate.of(2008, 9, 14),
            4.6,
            374
        ));

        books.add(createBook(
            "Harry Potter and the Philosopher's Stone",
            "J.K. Rowling",
            "9780439708180",
            "FANTASY",
            "A young wizard discovers his magical heritage and attends Hogwarts School of Witchcraft and Wizardry.",
            LocalDate.of(1997, 6, 26),
            4.9,
            309
        ));

        // Add borrow counts based on popularity (more realistic distribution)
        assignRealisticBorrowCounts(books);

        bookRepository.saveAll(books);
        System.out.println("✅ Created " + books.size() + " books with realistic data");
    }

    private Book createBook(String title, String author, String isbn, String category,
                          String description, LocalDate publicationDate, double rating, int pages) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setCategory(category);
        book.setDescription(description);
        book.setPublicationDate(Date.from(publicationDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        book.setRating(rating);
        book.setAvailable(true);
        book.setBorrowed(false);
        book.setPages(pages);
        return book;
    }

    private void assignRealisticBorrowCounts(List<Book> books) {
        // Assign realistic borrow counts (following Pareto principle - 20% of books get 80% of borrows)
        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            int borrowCount;
            
            if (i < books.size() * 0.2) {
                // Top 20% - very popular (50-150 borrows)
                borrowCount = 50 + (int)(Math.random() * 100);
            } else if (i < books.size() * 0.5) {
                // Middle 30% - moderately popular (15-50 borrows)
                borrowCount = 15 + (int)(Math.random() * 35);
            } else {
                // Bottom 50% - less popular (0-15 borrows)
                borrowCount = (int)(Math.random() * 15);
            }
            
            book.setBorrowCount(borrowCount);
        }
    }

    private void initializeActivities() {
        // This would be populated as users interact with the system
        // For now, we'll leave it empty to accumulate real activities
        System.out.println("✅ Activity tracking initialized - will collect real user activities");
    }
}
