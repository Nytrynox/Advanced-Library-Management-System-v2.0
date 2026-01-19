package com.library.controller;

import com.library.model.Book;
import com.library.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@CrossOrigin
public class BookController {
    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<Book> getAllBooks(@RequestParam(required = false) Integer page,
                                  @RequestParam(required = false) Integer size,
                                  @RequestParam(required = false) String sortBy) {
        return bookService.getAllBooks(page, size, sortBy);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable String id) {
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<Book> searchBooks(@RequestParam(required = false) String title,
                                 @RequestParam(required = false) String author,
                                 @RequestParam(required = false) String isbn,
                                 @RequestParam(required = false) List<String> tags) {
        return bookService.searchBooks(title, author, isbn, tags);
    }

    @GetMapping("/category/{category}")
    public List<Book> getBooksByCategory(@PathVariable String category) {
        return bookService.getBooksByCategory(category);
    }

    @GetMapping("/available")
    public List<Book> getAvailableBooks() {
        return bookService.getAvailableBooks();
    }

    @PostMapping
    public Book addBook(@RequestBody Book book) {
        return bookService.addBook(book);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable String id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/borrow")
    public ResponseEntity<Book> borrowBook(@PathVariable String id, @RequestParam(required = false, defaultValue = "default") String userId) {
        try {
            return bookService.borrowBook(id, userId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<Book> returnBook(@PathVariable String id, @RequestParam(required = false, defaultValue = "default") String userId) {
        try {
            return bookService.returnBook(id, userId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
