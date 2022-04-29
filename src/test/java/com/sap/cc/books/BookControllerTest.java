package com.sap.cc.books;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BookControllerTest {

    private static final String URI_BOOK = "/api/v1/books";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private BookStorage storage;

    @BeforeEach
    public void beforeEach() {
        storage.deleteAll();
    }


    @Test
    public void getAll_noBooks_returnsEmptyList() throws Exception {
        this.mockMvc.perform(get(URI_BOOK))
                .andExpect((status().isOk()))
                .andExpect(content().string("[]"));
    }

    @Test
    public void addBook_returnsCreatedBook() throws Exception {
        // Given
        String author = "Tom";
        String payload = prepareBookObjectAsJsonString(author);
        // When
        ResultActions resultActions = this.mockMvc.perform(addBookRequest(payload));
        MockHttpServletResponse response = resultActions.andExpect(status().isCreated()).andReturn().getResponse();

        // Then
        String responseBody = response.getContentAsString();
        String locationHeader = response.getHeader("location");
        Book createdBook = objectMapper.readValue(responseBody, Book.class);

        assertThat(createdBook.getAuthor()).isEqualTo(author);
        assertThat(createdBook.getId()).isNotNull();
        assertThat(locationHeader).isEqualTo(URI_BOOK + "/" + createdBook.getId().toString());
    }

    @Test
    public void addBookAndGetSingle_returnsBook() throws Exception {
        // Given
        String author = "Tom";
        String payload = prepareBookObjectAsJsonString(author);
        // When
        ResultActions resultActions = this.mockMvc.perform(addBookRequest(payload));
        MockHttpServletResponse postResponse = resultActions.andExpect(status().isCreated()).andReturn().getResponse();

        String path = postResponse.getHeader("location");

        ResultActions resultActionsGet = this.mockMvc.perform(getSingleBookRequest(path));
        MockHttpServletResponse getResponse = resultActionsGet.andExpect(status().isOk()).andReturn().getResponse();
        // Then
        assertThat(postResponse.getContentAsString()).isEqualTo(getResponse.getContentAsString());
    }

    @Test
    public void getSingle_noBooks_returnsNotFound() throws Exception {

        this.mockMvc.perform(get(URI_BOOK + "/" + 1L)).andExpect(status().isNotFound());
    }

    @Test
    public void addMultipleAndGetAll_returnsAddedBooks() throws Exception {
        // Given
        String requestPayload = prepareBookObjectAsJsonString("Tom");
        // When
        String bookCreated = this.mockMvc.perform(addBookRequest(requestPayload)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        MockHttpServletResponse response = this.mockMvc.perform(getAllBookRequest()).andExpect(status().isOk()).andReturn().getResponse();
        // Then
        List<Book> books = objectMapper.readValue(response.getContentAsString(), new com.fasterxml.jackson.core.type.TypeReference<List<Book>>() {});
        assertThat(books.size()).isEqualTo(1);
        String returnedPayload = objectMapper.writeValueAsString(books.get(0));
        assertThat(returnedPayload).isEqualTo(bookCreated);

        // Given
        requestPayload = prepareBookObjectAsJsonString("Jerry");
        // When
        this.mockMvc.perform(addBookRequest(requestPayload)).andExpect(status().isCreated());
        response = this.mockMvc.perform(getAllBookRequest()).andExpect(status().isOk()).andReturn().getResponse();
        // Then
        books = objectMapper.readValue(response.getContentAsString(), new com.fasterxml.jackson.core.type.TypeReference<List<Book>>() {});
        assertThat(books.size()).isEqualTo(2);
    }

    @Test
    public void getSingle_idLessThanOne_returnsBadRequest() throws Exception {
        this.mockMvc.perform(get(URI_BOOK + "/" + 0L)).andExpect(status().isBadRequest());
    }

    private MockHttpServletRequestBuilder addBookRequest(String jsonBody){
        return post(URI_BOOK).content(jsonBody).contentType(MediaType.APPLICATION_JSON);
    }

    private MockHttpServletRequestBuilder getSingleBookRequest(String path) throws Exception{
        return get(path);
    }

    private MockHttpServletRequestBuilder getAllBookRequest() throws  Exception{
        return get(URI_BOOK);
    }

    private String prepareBookObjectAsJsonString(String author){
        Book book = new Book();
        book.setAuthor(author);
        try {
            return objectMapper.writeValueAsString(book);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

}
