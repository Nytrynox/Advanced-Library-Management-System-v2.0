package com.library.repository;

import com.library.model.BookActivity;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Date;
import java.util.List;

public interface BookActivityRepository extends MongoRepository<BookActivity, String> {
    List<BookActivity> findByTimestampAfter(Date date);
    List<BookActivity> findTop10ByOrderByTimestampDesc();
}
