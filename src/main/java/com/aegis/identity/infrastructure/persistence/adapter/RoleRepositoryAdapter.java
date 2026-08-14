package com.aegis.identity.infrastructure.persistence.adapter;

import com.aegis.identity.domain.entity.Role;
import com.aegis.identity.domain.repository.RoleRepository;
import com.aegis.identity.infrastructure.persistence.jpa.RoleJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class RoleRepositoryAdapter implements RoleRepository {

  private final RoleJpaRepository repository;

  public RoleRepositoryAdapter(RoleJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<Role> findById(UUID id) {
    return repository.findById(id);
  }

  @Override
  public Optional<Role> findByName(String name) {
    return repository.findByName(name);
  }

  @Override
  public List<Role> findAll() {
    return repository.findAll();
  }

  @Override
  public Role save(Role role) {
    return repository.save(role);
  }
}
