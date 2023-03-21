package com.joejoe2.demo.repository.user;

import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findById(UUID id);

  Optional<User> getByUserName(String username);

  Optional<User> getByEmail(String email);

  List<User> getByRole(Role role);

  Page<User> findAll(Pageable pageable);
}
