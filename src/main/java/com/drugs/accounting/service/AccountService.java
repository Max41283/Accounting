package com.drugs.accounting.service;

import org.springframework.http.ResponseEntity;

import com.drugs.accounting.dto.ChangeRoleDto;
import com.drugs.accounting.dto.UserActiveResponseDto;
import com.drugs.accounting.dto.UserRegisterDto;
import com.drugs.accounting.dto.UserUpdateDto;

public interface AccountService {
	
	void addUser(UserRegisterDto userRegisterDto);
	
	ResponseEntity<?> getUser(Long id);
	
	ResponseEntity<?> removeUser(Long id);
	
	ResponseEntity<?> updateUser(Long id, UserUpdateDto userUpdateDto);
	
	void changePassword(Long id, String newPassword);
	
	ResponseEntity<?> changeRole(ChangeRoleDto changeRoleDto);
	
	UserActiveResponseDto toggleActiveUser(String userName);

}
