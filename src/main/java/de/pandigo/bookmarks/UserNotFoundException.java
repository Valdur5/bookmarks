package de.pandigo.bookmarks;

// Not an HTTP error anymore because the BookmarkControllerAdvice annotation will take care of that error.
class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(final String userId) {
        super("could not find user '" + userId + "'.");
    }
}