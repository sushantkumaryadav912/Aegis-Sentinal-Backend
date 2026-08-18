package com.aegis.identity.infrastructure.persistence.adapter;

import com.aegis.identity.domain.entity.User;
import com.aegis.identity.domain.repository.UserRepository;
import com.aegis.identity.infrastructure.persistence.jpa.UserJpaRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryAdapter implements UserRepository {

  private final UserJpaRepository repository;

  public UserRepositoryAdapter(UserJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<User> findById(UUID id) {
    return repository.findById(id);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return repository.findByEmail(email);
  }

  @Override
  public boolean existsByEmail(String email) {
    return repository.existsByEmail(email);
  }

  @Override
  public User save(User user) {
    return repository.save(user);
  }
}
