package com.library;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.Date;

public class LibraryService {
    private final MongoCollection<Document> userCollection;
    private final MongoCollection<Document> bookCollection;

    public LibraryService() {
        MongoDatabase db = MongoConfig.getDatabase();
        this.userCollection = db.getCollection("users");
        this.bookCollection = db.getCollection("books");
    }

    public void borrowBook(ObjectId userId, String isbn) {
        Document book = bookCollection.find(Filters.eq("isbn", isbn)).first();
        if (book == null || book.getInteger("availableCopies") <= 0) {
            System.out.println("Error: Book is unavailable!");
            return;
        }

        Document transactionLog = new Document("bookId", book.getObjectId("_id"))
                .append("title", book.getString("title"))
                .append("borrowDate", new Date())
                .append("returned", false);

        userCollection.updateOne(
                Filters.eq("_id", userId),
                Updates.push("borrowedBooks", transactionLog)
        );

        bookCollection.updateOne(
                Filters.eq("isbn", isbn),
                Updates.inc("availableCopies", -1)
        );

        System.out.println("Book borrowed successfully!");
    }
}