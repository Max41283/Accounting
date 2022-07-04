package com.drugs.accounting.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.drugs.accounting.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
	
	Optional<Role> findByRoleName(String roleName);

}
