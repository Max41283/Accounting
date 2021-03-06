package com.drugs.accounting.dao;

import java.sql.Timestamp;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.drugs.accounting.model.UserRole;
import com.drugs.accounting.model.UserRoleId;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
	
	Optional<UserRole> findByUserIdAndDateStartLessThanEqualAndDateEndGreaterThanEqual(Long userId, Timestamp start, Timestamp end);
	
	Optional<UserRole> findByUserIdAndDateStartLessThanEqualAndDateEndGreaterThanEqualAndRoleIdNot(Long userId, Timestamp start, Timestamp end, Long roleId);
	
	Optional<UserRole> findByUserIdAndRoleIdAndDateStartLessThanEqualAndDateEndGreaterThanEqual(Long userId, Long roleId, Timestamp start, Timestamp end);
	
}
