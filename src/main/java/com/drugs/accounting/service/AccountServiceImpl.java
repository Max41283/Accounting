package com.drugs.accounting.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.drugs.accounting.dao.AccountRepository;
import com.drugs.accounting.dao.RoleRepository;
import com.drugs.accounting.dao.UserRoleRepository;
import com.drugs.accounting.dto.ChangeRoleDto;
import com.drugs.accounting.dto.UserActiveResponseDto;
import com.drugs.accounting.dto.UserRegisterDto;
import com.drugs.accounting.dto.UserResponseDto;
import com.drugs.accounting.dto.UserUpdateDto;
import com.drugs.accounting.exceptions.EntityNotFoundException;
import com.drugs.accounting.exceptions.LoginExistsExeption;
import com.drugs.accounting.model.Account;
import com.drugs.accounting.model.Role;
import com.drugs.accounting.model.UserRole;

@Service
public class AccountServiceImpl implements AccountService {
	
	AccountRepository accountRepository;
	UserRoleRepository userRoleRepository;
	RoleRepository roleRepository;
	ModelMapper modelMapper;
	PasswordEncoder passwordEncoder;

	public AccountServiceImpl(AccountRepository accountRepository, UserRoleRepository userRoleRepository,
			RoleRepository roleRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
		this.accountRepository = accountRepository;
		this.userRoleRepository = userRoleRepository;
		this.roleRepository = roleRepository;
		this.modelMapper = modelMapper;
		this.passwordEncoder = passwordEncoder;
	}
	
	@Override
	@Transactional
	public void addUser(UserRegisterDto userRegisterDto) {
		if (accountRepository.findByUserName(userRegisterDto.getUserName()).orElse(null) != null) {
			throw new LoginExistsExeption("username " + userRegisterDto.getUserName());
		}
		if (accountRepository.findByEmail(userRegisterDto.getEmail()).orElse(null) != null) {
			throw new LoginExistsExeption("email " + userRegisterDto.getEmail());
		}
		Account account = modelMapper.map(userRegisterDto, Account.class);
		account.setPassword(passwordEncoder.encode(userRegisterDto.getPassword()));
		account.setActive(true);
		Long userId = accountRepository.save(account).getId();
		Role role = roleRepository.findByRoleName("USER")
				.orElseThrow(() -> new EntityNotFoundException("The role, named USER"));
		System.out.println(role + " TEST!!!!!!!!!!!!!!!!!");
		userRoleRepository.save(new UserRole(
				userId,
				role.getId(),
				Timestamp.valueOf(LocalDateTime.now()),
				Timestamp.valueOf(LocalDateTime.now().plusYears(100))
				));
	}

	@Override
	@Transactional(readOnly = true)
	public ResponseEntity<?> getUser(Long id) {
		Account account = accountRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("User " + id));
		UserRole userRole = findUserRole(id);
		return ResponseEntity.ok(new UserResponseDto(account.getUserName(),
				account.getEmail(),
				findRoleName(userRole.getRoleId()).getRoleName(),
				userRole.getDateEnd().toLocalDateTime().toLocalDate()));
	}

	@Override
	@Transactional
	public ResponseEntity<?> removeUser(Long id) {
		Account account = accountRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("User " + id));
		UserRole userRole = findUserRole(id);
		accountRepository.deleteById(id);
		return ResponseEntity.ok(new UserResponseDto(account.getUserName(),
				account.getEmail(),
				findRoleName(userRole.getRoleId()).getRoleName(),
				userRole.getDateEnd().toLocalDateTime().toLocalDate()));
	}

	@Override
	@Transactional
	public ResponseEntity<?> updateUser(Long id,UserUpdateDto userUpdateDto) {
		Account account = accountRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("User " + id));
		if (!account.getUserName().equals(userUpdateDto.getUsername())) {
			if (accountRepository.findByUserName(userUpdateDto.getUsername()).orElse(null) != null) {
				throw new LoginExistsExeption("username " + userUpdateDto.getUsername());
			} 
		}
		if (!account.getEmail().equals(userUpdateDto.getEmail())) {
			if (accountRepository.findByEmail(userUpdateDto.getEmail()).orElse(null) != null) {
				throw new LoginExistsExeption("email " + userUpdateDto.getEmail());
			} 
		}
		UserRole userRole = findUserRole(id);
		account.setUserName(userUpdateDto.getUsername());
		account.setEmail(userUpdateDto.getEmail());
		account = accountRepository.save(account);
		return ResponseEntity.ok(new UserResponseDto(account.getUserName(),
				account.getEmail(),
				findRoleName(userRole.getRoleId()).getRoleName(),
				userRole.getDateEnd().toLocalDateTime().toLocalDate()));
	}

	@Override
	@Transactional
	public void changePassword(Long id, String newPassword) {
		Account account = accountRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("User " + id));
		newPassword = passwordEncoder.encode(newPassword);
		account.setPassword(newPassword);
		accountRepository.save(account);
	}

	@Override
	@Transactional
	public ResponseEntity<?> changeRole(ChangeRoleDto changeRoleDto) {
		Timestamp now = Timestamp.valueOf(LocalDateTime.now());
		Account account = accountRepository.findByUserName(changeRoleDto.getUserName())
				.orElseThrow(() -> new EntityNotFoundException(changeRoleDto.getUserName()));
		Role role = roleRepository.findByRoleName(changeRoleDto.getRoleName())
				.orElseThrow(() -> new EntityNotFoundException("Role " + changeRoleDto.getRoleName()));
		Long userId = account.getId();
		Long roleId = role.getId();
		Timestamp startDate = Timestamp.valueOf(LocalDateTime.now());
		Timestamp endDate = Timestamp.valueOf(LocalDateTime.now().plusDays(changeRoleDto.getValidityInDays()));
		UserRole userRole = userRoleRepository
				.findByUserIdAndRoleIdAndDateStartLessThanEqualAndDateEndGreaterThanEqual(userId, roleId, now, now)
				.orElse(null);
		if (userRole != null) {
			userRoleRepository.delete(userRole);
		}
		userRole = new UserRole(userId, roleId, startDate, endDate);
		userRoleRepository.save(userRole);
		return ResponseEntity.ok(new UserResponseDto(account.getUserName(),
				account.getEmail(),
				findRoleName(userRole.getRoleId()).getRoleName(),
				userRole.getDateEnd().toLocalDateTime().toLocalDate()));
	}

	@Override
	@Transactional
	public UserActiveResponseDto toggleActiveUser(String userName) {
		Account account = accountRepository.findByUserName(userName)
				.orElseThrow(() -> new EntityNotFoundException(userName));
		account.setActive(!account.isActive());
		accountRepository.save(account);
		UserActiveResponseDto userActiveResponseDto = modelMapper.map(account, UserActiveResponseDto.class);
		UserRole userRole = findUserRole(account.getId());
		userActiveResponseDto.setExpiryDate(userRole.getDateEnd().toLocalDateTime().toLocalDate());
		userActiveResponseDto.setRole(findRoleName(userRole.getRoleId()).getRoleName());
		return userActiveResponseDto;
	}
	
	private UserRole findUserRole(Long userId) {
		Timestamp now = Timestamp.valueOf(LocalDateTime.now());
		Role role = roleRepository.findByRoleName("USER")
				.orElseThrow(() -> new EntityNotFoundException("The role, named USER"));
		Long userRoleNameId = role.getId();
		UserRole userRole = userRoleRepository
				.findByUserIdAndDateStartLessThanEqualAndDateEndGreaterThanEqualAndRoleIdNot(userId,
						now, now, userRoleNameId)
				.orElse(null);
		if (userRole == null) {
			userRole = userRoleRepository
					.findByUserIdAndDateStartLessThanEqualAndDateEndGreaterThanEqual(userId, now, now)
					.orElseThrow(() -> new EntityNotFoundException("Actual USER-role for User " + userId));
		}
		return userRole;
	}
	
	private Role findRoleName(Long roleId) {
		return roleRepository.findById(roleId)
				.orElseThrow(() -> new EntityNotFoundException("The role, identified by " + roleId));
	}

}
