package com.sap.cc.books;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private BookStorage bookStorage;

    public BookController(BookStorage bookStorage) {
        this.bookStorage = bookStorage;
    }

    @GetMapping
    public List<Book> getAllBooks(){
        return bookStorage.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getSingle(@PathVariable("id") Long id){
        if(id < 1) throw new IllegalArgumentException("Id must not be less than 1");

        Optional<Book> optionalBook = bookStorage.get(id);
        if (optionalBook.isPresent()){
            return ResponseEntity.ok(optionalBook.get());
        }else{
            throw new NotFoundException();
        }
    }

    @PostMapping
    public ResponseEntity<Book> addBook(@RequestBody Book book, UriComponentsBuilder uriComponentsBuilder) throws URISyntaxException {

        Book createdBook = bookStorage.save(book);

        UriComponents uriComponents = uriComponentsBuilder
                .path("/api/v1/books" + "/{id}")
                .buildAndExpand(createdBook.getId());
        URI locationHeaderUri = new URI(uriComponents.getPath());

        return ResponseEntity.created(locationHeaderUri).body(createdBook);
    }
}
