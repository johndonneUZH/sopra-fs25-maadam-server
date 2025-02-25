package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LogOutDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {
  private static final Logger logger = LoggerFactory.getLogger(UserController.class);
  private final UserService userService;

  UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/users")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<UserGetDTO> getAllUsers(@RequestHeader (value = "Authorization", required = false) String authToken) {

    // Validate the token
    User authenticatedUser = userService.getUserByToken(authToken);
    if (authToken == null || authenticatedUser == null) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
    }

    // fetch all users in the internal representation
    List<User> users = userService.getUsers();
    List<UserGetDTO> userGetDTOs = new ArrayList<>();

    // convert each user to the API representation
    for (User user : users) {
      userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
    }
    return userGetDTOs;
  }

  @PostMapping("/users")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
    // convert API user to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    // create user
    User createdUser = userService.createUser(userInput);
    // convert internal representation of user back to API
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
  }

  @PostMapping("/login/auth")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public UserGetDTO loginUser(@RequestBody UserPostDTO userPostDTO) {
    // convert API user to internal representation
    
    logger.info("Received request to login user: {}", userPostDTO.getUsername());

    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    // login user
    User loggedInUser = userService.loginUser(userInput);
    // convert internal representation of user back to API
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(loggedInUser);
  }

  @GetMapping("/users/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public UserGetDTO getUserById(
          @PathVariable Long id,
          @RequestHeader(value = "Authorization", required = false) String authToken) {
  
      // Validate the token
      User authenticatedUser = userService.getUserByToken(authToken);
      if (authToken == null || authenticatedUser == null) {
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
      }
  
      // Fetch user if authentication passes
      User user = userService.getUserById(id);
      if (user == null) {
          throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
      }
  
      return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
  }
  


  @PutMapping("/users/{id}")
  public ResponseEntity<Void> editUser(
          @PathVariable Long id, 
          @RequestBody UserPutDTO userPutDTO,
          @RequestHeader(value = "Authorization", required = false) String authToken) {
      
      // Validate the token
      User authenticatedUser = userService.getUserByToken(authToken);
      if (authToken == null || authenticatedUser == null) {
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
      }

      System.out.println("Received request to edit user: " + id);
      System.out.println("Received request to edit user: " + authenticatedUser.getUsername());

      if (authToken == null || authenticatedUser == null || !authenticatedUser.getId().equals(id)) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }

      try {
          // Fetch the user by ID
          User user = userService.getUserById(id);
          if (user == null) {
              return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
          }
  
          // Update the user
          userService.editUser(user, userPutDTO);
  
          // Return 204 No Content on success
          return ResponseEntity.noContent().build();
      } catch (IllegalArgumentException e) {
          return ResponseEntity.badRequest().build();
      } catch (Exception e) {
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }
  }
  
  @PutMapping("/users/logout")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<Void> logoutUser(@RequestBody LogOutDTO logOutDTO) {
      try {
          // Fetch the user by ID
          User user = userService.getUserById(logOutDTO.getId());
          System.out.println("Received request to logout user: " + logOutDTO.getId());
          if (user == null) {
              return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
          }
  
          // Validate the token
          if (!user.getToken().equals(logOutDTO.getToken())) {
              return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
          }
  
          // Update the user status to OFFLINE
          userService.logoutUser(user);
  
          return ResponseEntity.noContent().build();
      } catch (IllegalArgumentException e) {
          return ResponseEntity.badRequest().build();
      } catch (Exception e) {
          e.printStackTrace();
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }
  }
  
}