package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.time.LocalDate;

public class UserPutDTO {

  // private String name;
  // private String password;
  private String username;
  private LocalDate birthday;

  // public String getName() {
  //   return name;
  // }

  // public void setName(String name) {
  //   this.name = name;
  // }

  // public String getPassword() {
  //   return password;
  // }

  // public void setPassword(String password) {
  //   this.password = password;
  // }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public LocalDate getBirthday() {
    return birthday;
  }

  public void setBirthday(LocalDate birthday) {
    this.birthday = birthday;
  }
}