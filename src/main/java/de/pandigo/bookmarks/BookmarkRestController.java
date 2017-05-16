package de.pandigo.bookmarks;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/{userId}/bookmarks")
class BookmarkRestController {

	private final BookmarkRepository bookmarkRepository;

	private final AccountRepository accountRepository;

	@Autowired
	BookmarkRestController(final BookmarkRepository bookmarkRepository, final AccountRepository accountRepository) {
		// Letting spring auto populate the repositories.
		this.bookmarkRepository = bookmarkRepository;
		this.accountRepository = accountRepository;
	}

	@RequestMapping(method = RequestMethod.GET)
    Resources<BookmarkResource> readBookmarks(@PathVariable final String userId) {
		this.validateUser(userId);
		final List<BookmarkResource> bookmarkResourceList = this.bookmarkRepository.findByAccountUsername(userId).stream().map(BookmarkResource::new)
		        .collect(Collectors.toList());

		return new Resources<>(bookmarkResourceList);
	}

	@RequestMapping(method = RequestMethod.POST)
	ResponseEntity<?> add(@PathVariable final String userId, @RequestBody final Bookmark input) {
		this.validateUser(userId);

		return this.accountRepository.findByUsername(userId).map(account -> {
			// Create new bookmark entry.
			Bookmark bookmark  = this.bookmarkRepository.save(new Bookmark(account, input.uri, input.description));

			// Get the self link from the HATEOAS implementation
            Link forOneBookmark = new BookmarkResource(bookmark).getLink("self");

			// Return the link AND the HTTP status for created in the ResponseEntity wrapper
		    // object.
			return ResponseEntity.created(URI.create(forOneBookmark.getHref())).build();
		})
		        // Return if value is not present the HTTP status for no content and no location.
		        .orElse(ResponseEntity.noContent().build());

	}

	@RequestMapping(method = RequestMethod.GET, value = "/{bookmarkId}")
    BookmarkResource readBookmark(@PathVariable final String userId, @PathVariable final Long bookmarkId) {
		this.validateUser(userId);
		return new BookmarkResource(this.bookmarkRepository.findOne(bookmarkId));
	}

	private void validateUser(final String userId) {
		// Ensures that the user is existing, otherwise throw an runtime exception.
		this.accountRepository.findByUsername(userId).orElseThrow(() -> new UserNotFoundException(userId));
	}
}
