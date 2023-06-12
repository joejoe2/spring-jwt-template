package com.joejoe2.demo.model.auth;

import com.joejoe2.demo.model.Base;
import java.time.Instant;
import java.util.Objects;
import javax.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "account_user")
public class User extends Base {
  @Version
  @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT now()")
  private Instant version;

  @Column(unique = true, length = 32, nullable = false)
  private String userName;

  @Column(length = 64, nullable = false)
  private String password;

  @Column(unique = true, length = 128, nullable = false)
  private String email;

  @Column(length = 32, nullable = false)
  @Enumerated(EnumType.STRING)
  private Role role = Role.NORMAL; // code level default

  @Column(nullable = false, columnDefinition = "boolean default true") // db level default
  private boolean isActive = true; // code level default

  @CreationTimestamp private Instant createAt;

  @UpdateTimestamp private Instant updateAt;

  @Column(nullable = true)
  private Instant authAt;

  @Embedded LoginAttempt loginAttempt = new LoginAttempt();

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User)) return false;
    User user = (User) o;
    return isActive() == user.isActive()
        && getId().equals(user.getId())
        && getUserName().equals(user.getUserName())
        && getEmail().equals(user.getEmail())
        && getRole() == user.getRole();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getUserName(), getEmail(), getRole(), isActive());
  }
}
