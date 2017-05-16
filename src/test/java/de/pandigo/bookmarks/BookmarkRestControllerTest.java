package de.pandigo.bookmarks;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

// At the top, we use the @SpringBootTest(classes = BookmarksApplication.class) annotation (from Spring Boot)
// to tell the SpringJUnit4ClassRunner (SpringRunner) where it should get information about the Spring application
// under test. In former versions of Spring Boot this was SpringApplicationConfiguration
@SpringBootTest(classes = BookmarksApplication.class)

// The @WebAppConfiguration annotation tells JUnit that this is a unit test for Spring MVC web components and
// should thus run under a WebApplicationContext variety, not a standard ApplicationContext implementation.
@WebAppConfiguration

// When a class is annotated with RunWith or extends a class annotated
// with RunWith JUnit will invoke the class it references to run the
// tests in that class instead of the runner built into JUnit.

// SpringRunner is a custom extension of JUnit's BlockJUnit4ClassRunner which provides functionality of the
// Spring TestContext Framework to standard JUnit tests by means of the TestContextManager and associated support
// classes and annotations.
@RunWith(SpringRunner.class)
public class BookmarkRestControllerTest {

    // We define here the content type of the payload for our POST requests and the content type we get back from
    // GET requests.
    private final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    // The MockMvc is the center piece: all tests will invariably go through the MockMvc type to mock HTTP requests
    // against the service.
    private MockMvc mockMvc;

    // Just a username we need to create and read bookmark data. This username and data is created in the @Before method.
    private final String userName = "bdussault";

    // The Jackson 2 Http message converter provides us the possibility to easily convert the Java objects into HTTP
    // messages, this includes to convert a payload Java object to put into the body as a JSON string.
    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    private Account account;
    private final List<Bookmark> bookmarkList = new ArrayList<>();

    // Access to the Repository objects for preparing the test content in our database in the @Before method.
    @Autowired
    private BookmarkRepository bookmarkRepository;
    @Autowired
    private AccountRepository accountRepository;

    // Get the reference to the WebApplicationContext which is derived from the ApplicationContext.
    @Autowired
    private WebApplicationContext webApplicationContext;


    @Autowired
    // Depending on the Converter JARs in the classpath the HttpMessageConverter Array is filled this method is called
    // from spring and we try to find the message converter we are looking for to store in our local variable.
    void setConverters(final HttpMessageConverter<?>[] converters) {
        this.mappingJackson2HttpMessageConverter = Arrays.stream(converters)
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .orElse(null);

        assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    @Before
    public void setup() throws Exception {
        // Build a MockMvc instance using the given, fully initialized refreshed WebApplicationContext. The
        // org.springframework.web.servlet.DispatcherServlet will use the context to discover Spring MVC infrastructure
        // and application controllers in it. The context must have been configured with a javax.servlet.ServletContext.
        this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

        // Drop all the database data, this provides NO solution for a case where you are unable to drop your whole test
        // content of the database!!!
        this.bookmarkRepository.deleteAllInBatch();
        this.accountRepository.deleteAllInBatch();

        // Populate the database with test content for the following tests.
        this.account = this.accountRepository.save(new Account(this.userName, "password"));
        this.bookmarkList.add(this.bookmarkRepository.save(new Bookmark(this.account, "http://bookmark.com/1/" + this.userName, "A description")));
        this.bookmarkList.add(this.bookmarkRepository.save(new Bookmark(this.account, "http://bookmark.com/2/" + this.userName, "A description")));
    }

    // Creates a HTTP string which contains the header with the media type JSON Application and as the body the given
    // object as a json string. This method is used to test the post request since they are the requests in this example
    // which require a JSON payload in their request body.
    private String populateMockHttpOutputStringWith(final Object o) throws IOException {
        final MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }

    // ############## Below here are the tests which are using all the fancy stuff we have built so far.  #############

    // The tests use the MockMvcResultMatchers#jsonPath method to validate the structure and contents of the JSON
    // responses. This, in turn, uses the Jayway JSON Path API to run X-Path-style traversals on JSON structures,
    // as we do in various places in the unit tests.

    @Test
    public void userNotFound() throws Exception {
        this.mockMvc.perform(post("/george/bookmarks/")
                .content(this.populateMockHttpOutputStringWith(new Bookmark()))
                .contentType(this.contentType))
                .andExpect(status().isNotFound());
    }

    @Test
    public void readSingleBookmark() throws Exception {
        this.mockMvc.perform(get("/" + this.userName + "/bookmarks/"
                + this.bookmarkList.get(0).getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(this.contentType))
                .andExpect(jsonPath("$.id", is(this.bookmarkList.get(0).getId().intValue())))
                .andExpect(jsonPath("$.uri", is("http://bookmark.com/1/" + this.userName)))
                .andExpect(jsonPath("$.description", is("A description")));
    }

    @Test
    public void readBookmarks() throws Exception {
        this.mockMvc.perform(get("/" + this.userName + "/bookmarks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(this.contentType))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(this.bookmarkList.get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].uri", is("http://bookmark.com/1/" + this.userName)))
                .andExpect(jsonPath("$[0].description", is("A description")))
                .andExpect(jsonPath("$[1].id", is(this.bookmarkList.get(1).getId().intValue())))
                .andExpect(jsonPath("$[1].uri", is("http://bookmark.com/2/" + this.userName)))
                .andExpect(jsonPath("$[1].description", is("A description")));
    }

    @Test
    public void createBookmark() throws Exception {
        final String bookmarkJson = populateMockHttpOutputStringWith(new Bookmark(
                this.account, "http://spring.io", "a bookmark to the best resource for Spring news and information"));

        this.mockMvc.perform(post("/" + this.userName + "/bookmarks")
                .contentType(this.contentType)
                .content(bookmarkJson))
                .andExpect(status().isCreated());
    }
}
