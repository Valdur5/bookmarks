package de.pandigo.bookmarks;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;

// We do not need to write the implementation because Spring Data provides us with an implementation based on the
// information we provided in the specific classes.
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Collection<Bookmark> findByAccountUsername(String username);
}
