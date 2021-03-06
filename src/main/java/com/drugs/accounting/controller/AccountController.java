package com.drugs.accounting.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.drugs.accounting.dto.ChangeRoleDto;
import com.drugs.accounting.dto.UserActiveResponseDto;
import com.drugs.accounting.dto.UserRegisterDto;
import com.drugs.accounting.dto.UserUpdateDto;
import com.drugs.accounting.security.jwt.JwtTokenUtil;
import com.drugs.accounting.service.AccountService;

@RestController
@RequestMapping("/accounting")
public class AccountController {
	
	AccountService accountService;
	JwtTokenUtil jwtTokenUtil;

	public AccountController(AccountService accountService, JwtTokenUtil jwtTokenUtil) {
		this.accountService = accountService;
		this.jwtTokenUtil = jwtTokenUtil;
	}

	@PostMapping("/registation")
	public void register(@RequestBody UserRegisterDto userRegisterDto) {
		accountService.addUser(userRegisterDto);
	}
	
	@GetMapping("/getuser")
	public ResponseEntity<?> getUser(@RequestHeader("Authorization") String token) {
		return accountService.getUser(jwtTokenUtil.getUserId(token.substring(7)));
	}
	
	@DeleteMapping("/delete")
	public ResponseEntity<?> deleteUser(@RequestHeader("Authorization") String token) {
		return accountService.removeUser(jwtTokenUtil.getUserId(token.substring(7)));
	}
	
	@PutMapping("/update")
	public ResponseEntity<?> updateUser(@RequestHeader("Authorization") String token,
			@RequestBody UserUpdateDto userUpdateDto) {
		return accountService.updateUser(jwtTokenUtil.getUserId(token.substring(7)), userUpdateDto);
	}
	
	@PutMapping("/password")
	public void changePassword(@RequestHeader("Authorization") String token, 
			@RequestHeader("X-Password") String newPassword) {
		accountService.changePassword(jwtTokenUtil.getUserId(token.substring(7)), newPassword);
	}
	
	@PutMapping("/admin/role")
	public ResponseEntity<?> changeRole(@RequestHeader("Authorization") String token,
			@RequestBody ChangeRoleDto changeRoleDto) {
		return accountService.changeRole(changeRoleDto);
	}
	
	@PutMapping("/admin/active/{user}")
	public UserActiveResponseDto deactivateUser(@PathVariable String user) {
		return accountService.toggleActiveUser(user);
	}
	
}
