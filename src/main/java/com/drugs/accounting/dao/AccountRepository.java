package com.drugs.accounting.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.drugs.accounting.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
	
	Optional<Account> findByUserName(String userName);
	
	Optional<Account> findByEmail(String email);

}
