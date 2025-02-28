package ch.uzh.ifi.hase.soprafs24.utlis;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;

import java.util.UUID;

public class TestUtils {

    /**
     * Creates and saves a test user with a generated token.
     *
     * @param userRepository The UserRepository to save the user.
     * @param username       The username for the test user.
     * @param password       The password for the test user.
     * @return The generated token for the test user.
     */
    public static String createAndSaveTestUser(UserRepository userRepository, String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setStatus(UserStatus.ONLINE);
        user.setToken(UUID.randomUUID().toString()); // Generate a token
        userRepository.save(user);
        userRepository.flush();
        return user.getToken();
    }
}