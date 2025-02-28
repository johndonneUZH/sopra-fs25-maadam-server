package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.boot.test.context.SpringBootTest; // Use this instead of @WebMvcTest

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  private static String ADMIN_TOKEN;
  private static User testUser;

  // Setup method to create a user for testing
  @BeforeEach
  public void setup() throws Exception {
      // Clear the database before each test
      userRepository.deleteAll();
  
      // Create the user for the first time
      User user = new User();
      user.setUsername("admin");
      user.setPassword("admin");
      user.setToken(UUID.randomUUID().toString()); // Set the token
      user.setStatus(UserStatus.ONLINE); // Set the status
      user.setDate(LocalDate.now()); // Set the date
      user.setBirthday(null); // Optional field
  
      // Save the user to the database
      User createdUser = userRepository.save(user);
      ADMIN_TOKEN = createdUser.getToken(); // Retrieve the token from the database
  
      testUser = createdUser;  // Store the created user for subsequent tests
      System.out.println("Admin Token: " + ADMIN_TOKEN);
  }

  @Test
  void POST_user_201() throws Exception {
      UserPostDTO userPostDTO = new UserPostDTO();
      userPostDTO.setUsername("testUser");
      userPostDTO.setPassword("testPassword");

      mockMvc.perform(post("/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(userPostDTO)))
          .andExpect(status().isCreated());
  }

  @Test
  void POST_user_409() throws Exception {
      // Attempt to create the same user again and expect 409 conflict
      UserPostDTO userPostDTO = new UserPostDTO();
      userPostDTO.setUsername("admin");
      userPostDTO.setPassword("admin");

      mockMvc.perform(post("/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(userPostDTO)))
          .andExpect(status().isConflict());
  }

  @Test
  void GET_user_id_200() throws Exception {
      // Use the stored test user for GET request
      mockMvc.perform(get("/users/" + testUser.getId())
              .contentType(MediaType.APPLICATION_JSON)
              .header("Authorization", ADMIN_TOKEN)) // Use the token
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.username", is(testUser.getUsername())))
          .andExpect(jsonPath("$.status", is(testUser.getStatus().toString())));
  }

  @Test
  void PUT_user_204() throws Exception {
      // Perform a PUT request to update the user profile
      UserPutDTO updatedDTO = new UserPutDTO();
      updatedDTO.setUsername("updatedUsername");

      mockMvc.perform(put("/users/" + testUser.getId())
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(updatedDTO))
              .header("Authorization", ADMIN_TOKEN)) // Use the token
          .andExpect(status().isNoContent());
  }

  @Test
  void PUT_user_404() throws Exception {
      // Test case where user doesn't exist
      UserPutDTO updatedDTO = new UserPutDTO();
      updatedDTO.setUsername("nonExistentUsername");

      mockMvc.perform(put("/users/99999")
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(updatedDTO))
              .header("Authorization", ADMIN_TOKEN)) // Use the token
          .andExpect(status().isNotFound());
  }

  @Test
  public void createUser_validInput_userCreated() throws Exception {
      // given
      User user = new User();
      user.setId(1L);
      user.setUsername("testUsername");
      user.setPassword("testPassword");
      user.setStatus(UserStatus.ONLINE);
      user.setDate(LocalDate.now());
      user.setBirthday(null);

      UserPostDTO userPostDTO = new UserPostDTO();
      userPostDTO.setUsername("testUsername");
      userPostDTO.setPassword("testPassword");

      given(userService.createUser(Mockito.any())).willReturn(user);

      // when/then -> do the request + validate the result
      MockHttpServletRequestBuilder postRequest = post("/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(userPostDTO));

      // then
      mockMvc.perform(postRequest)
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id", is(user.getId())))
          .andExpect(jsonPath("$.username", is(user.getUsername())))
          .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
  }

  /**
   * Helper Method to convert userPostDTO into a JSON string such that the input
   * can be processed
   * Input will look like this: {"name": "Test User", "username": "testUsername"}
   * 
   * @param object
   * @return string
   */
  private String asJsonString(final Object object) {
      try {
          return new ObjectMapper().writeValueAsString(object);
      } catch (JsonProcessingException e) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              String.format("The request body could not be created.%s", e.toString()));
      }
  }
}