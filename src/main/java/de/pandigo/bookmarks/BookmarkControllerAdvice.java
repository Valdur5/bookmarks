package de.pandigo.bookmarks;

import org.springframework.hateoas.VndErrors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * Our revised service introduces a new class, BookmarkControllerAdvice, that uses Spring MVC’s @ControllerAdvice
 * annotation. @ControllerAdvice are a useful way to extricate the configuration of common concerns - like error
 * handling - into a separate place, away from any individual Spring MVC controller. Spring MVC, for example,
 * defines the @ExceptionHandler method that ties a specific handler method to any incident of an Exception or a
 * HTTP status code. Here, we’re telling Spring MVC that any code that throws a UserNotFoundException, as before,
 * should eventually be handled by the userNotFoundExceptionHandler method. This method simply wraps the propagated
 * Exception in a VndErrors and returns it to the client. In this example, we’ve removed the @ResponseStatus annotation
 * from the exception itself and centralized it in the @ControllerAdvice type. @ControllerAdvice types are a convenient
 * way to centralize all sorts of logic, and - unlike annotating exception types - can be used even for exception types
 * to which you don’t have the source code.
 */
@ControllerAdvice
class BookmarkControllerAdvice {

    @ResponseBody
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    VndErrors userNotFoundExceptionHandler(final UserNotFoundException ex) {
        return new VndErrors("error", ex.getMessage());
    }
}
