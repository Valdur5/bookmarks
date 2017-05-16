package de.pandigo.bookmarks;

import java.net.URI;
import java.security.Principal;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/{userId}/bookmarks")
class BookmarkRestController {

    private final BookmarkRepository bookmarkRepository;

    private final AccountRepository accountRepository;

    @Autowired
    BookmarkRestController(final BookmarkRepository bookmarkRepository,
                           final AccountRepository accountRepository) {
        // Letting spring auto populate the repositories.
        this.bookmarkRepository = bookmarkRepository;
        this.accountRepository = accountRepository;
    }

    @RequestMapping(method = RequestMethod.GET)
    Collection<Bookmark> readBookmarks(final Principal principal) {
        this.validateUser(principal);
        return this.bookmarkRepository.findByAccountUsername(principal.getName());
    }

    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<?> add(final Principal principal, @RequestBody final Bookmark input) {
        this.validateUser(principal);

        return this.accountRepository
                .findByUsername(principal.getName())
                .map(account -> {
                    // Create new bookmark entry.
                    Bookmark result = this.bookmarkRepository.save(new Bookmark(account,
                            input.uri, input.description));

                    // Create the URI which leads to the newly created entry.
                    URI location = ServletUriComponentsBuilder
                            .fromCurrentRequest().path("/{id}")
                            .buildAndExpand(result.getId()).toUri();

                    // Return the location AND the HTTP status for created in the ResponseEntity wrapper object.
                    return ResponseEntity.created(location).build();
                })
                // Return if value is not present the HTTP status for no content and no location.
                .orElse(ResponseEntity.noContent().build());

    }

    @RequestMapping(method = RequestMethod.GET, value = "/{bookmarkId}")
    Bookmark readBookmark(final Principal principal, @PathVariable final Long bookmarkId) {
        this.validateUser(principal);
        return this.bookmarkRepository.findOne(bookmarkId);
    }

    private void validateUser(final Principal principal) {
        // Ensures that the user is existing, otherwise throw an runtime exception.
        final String userId = principal.getName();
        this.accountRepository
                .findByUsername(userId)
                .orElseThrow(
                        () -> new UserNotFoundException(userId));
    }
}
