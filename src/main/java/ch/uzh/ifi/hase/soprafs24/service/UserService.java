package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;

  public UserService(@Qualifier("userRepository") UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<User> getUsers() {
    return this.userRepository.findAll();
  }

  public User createUser(User newUser) {
    newUser.setStatus(UserStatus.ONLINE);
    newUser.setDate(LocalDate.now());

    User responeUser = newUser;
    
    newUser.setToken(UUID.randomUUID().toString());

    checkIfUserExists(newUser);
    // saves the given entity but data is only persisted in the database once
    // flush() is called
    newUser = userRepository.save(newUser);
    userRepository.flush();

    System.out.println("Saved User: " + responeUser.toString());

    log.debug("Created Information for User: {}", responeUser);
    return responeUser;
  }

  /**
   * This is a helper method that will check the uniqueness criteria of the
   * username and the name
   * defined in the User entity. The method will do nothing if the input is unique
   * and throw an error otherwise.
   *
   * @param userToBeCreated
   * @throws org.springframework.web.server.ResponseStatusException
   * @see User
   */
  private void checkIfUserExists(User userToBeCreated) {
    User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());

    String baseErrorMessage = "Creation of user %s failed because username already exists";
    if (userByUsername != null) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, 
            String.format(baseErrorMessage, userToBeCreated.getUsername()));
    }
}
  
  public User getUserById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }

  public User getUserByToken(String token) {
    System.out.println("Searching for token: [" + token + "]");
    return userRepository.findByToken(token.trim());
}


  public User getUserByUsername(String username) {
    return userRepository.findByUsername(username);
  }

  public User loginUser(User userToBeLoggedIn) {
    User userByUsername = userRepository.findByUsername(userToBeLoggedIn.getUsername());

    if (userByUsername == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }

    if (!userByUsername.getPassword().equals(userToBeLoggedIn.getPassword())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Password is incorrect");
    }

    userByUsername.setStatus(UserStatus.ONLINE);
    userRepository.saveAndFlush(userByUsername);

    return userByUsername;
  }

  public void logoutUser(User userToBeLoggedOut) {
    userToBeLoggedOut.setStatus(UserStatus.OFFLINE);
    userRepository.saveAndFlush(userToBeLoggedOut);
  }

  public User editUser(User userToBeEdited, UserPutDTO userPutDTO) {
    if (userPutDTO == null) {
        throw new IllegalArgumentException("User data cannot be null");
    }

    if (userPutDTO.getUsername() != null && !userPutDTO.getUsername().equals(userToBeEdited.getUsername())) {
        User existingUser = userRepository.findByUsername(userPutDTO.getUsername());
        if (existingUser != null && !existingUser.getId().equals(userToBeEdited.getId())) {
            throw new IllegalArgumentException("Username already exists");
        }
    }

    // Validate username is not null or empty
    if (userPutDTO.getUsername() == null || userPutDTO.getUsername().trim().isEmpty()) {
        throw new IllegalArgumentException("Username cannot be empty");
    }

    // Validate birthday is not in the future
    if (userPutDTO.getBirthday() != null && userPutDTO.getBirthday().isAfter(LocalDate.now())) {
        throw new IllegalArgumentException("Birthday cannot be in the future");
    }

    // Update the user's details
    if (userPutDTO.getUsername() != null) {
        userToBeEdited.setUsername(userPutDTO.getUsername());
    }

    if (userPutDTO.getBirthday() != null) {
        userToBeEdited.setBirthday(userPutDTO.getBirthday());
    }

    // Save the updated user
    return userRepository.save(userToBeEdited);
  }
}


