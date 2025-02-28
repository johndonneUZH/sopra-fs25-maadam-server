package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;

@DataJpaTest
public class UserRepositoryIntegrationTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private UserRepository userRepository;

  @Test
  public void findById_success() {
      // given
      User user = new User();
      user.setUsername("firstname@lastname");
      user.setPassword("password");
      user.setStatus(UserStatus.ONLINE);
      user.setToken("1");
      user.setDate(LocalDate.now()); // Set the date field
  
      System.out.println("Before Persist: " + user);
  
      // Persist user without manually setting the ID
      entityManager.persist(user);
      entityManager.flush();
  
      System.out.println("After Persist: " + user);
  
      // when
      User found = userRepository.findById(user.getId()).orElse(null);
  
      // then
      assertNotNull(found);
      assertEquals(found.getUsername(), user.getUsername());
      assertEquals(found.getToken(), user.getToken());
      assertEquals(found.getStatus(), user.getStatus());
      assertEquals(found.getDate(), user.getDate()); // Verify the date field
  }
}
