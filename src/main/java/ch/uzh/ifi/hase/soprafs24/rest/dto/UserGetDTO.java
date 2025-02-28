package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.time.LocalDate;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;

public class UserGetDTO {

  private Long id;
  private String username;
  // private String token;
  private UserStatus status;
  private LocalDate birthday;
  private LocalDate date;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  // public String getToken() {
  //   return token;
  // }

  // public void setToken(String token) {
  //   this.token = token;
  // }

  public UserStatus getStatus() {
    return status;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
  }

  public LocalDate getBirthday() {
    return birthday;
  }

  public void setBirthday(LocalDate birthday) {
    this.birthday = birthday;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }
}
