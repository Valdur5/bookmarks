package de.pandigo.bookmarks;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

// This class contains @SpringBootApplication, which activate autoconfiguration, component scanning,
// and also allows bean definitions.
@SpringBootApplication
public class BookmarksApplication {

	public static void main(final String[] args) {
		SpringApplication.run(BookmarksApplication.class, args);
	}

	@Bean
	// Populate the database when the program starts.
	CommandLineRunner init(final AccountRepository accountRepository,
						   final BookmarkRepository bookmarkRepository) {
		return (evt) -> Arrays.asList(
				"jhoeller,dsyer,pwebb,ogierke,rwinch,mfisher,mpollack,jlong".split(","))
				.forEach(
						a -> {
							final Account account = accountRepository.save(new Account(a,
									"password"));
							bookmarkRepository.save(new Bookmark(account,
									"http://bookmark.com/1/" + a, "A description"));
							bookmarkRepository.save(new Bookmark(account,
									"http://bookmark.com/2/" + a, "A description"));
						});
	}
}
