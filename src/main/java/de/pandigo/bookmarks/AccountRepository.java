package de.pandigo.bookmarks;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// We do not need to write the implementation because Spring Data provides us with an implementation based on the
// information we provided in the specific classes.
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUsername(String username);
}