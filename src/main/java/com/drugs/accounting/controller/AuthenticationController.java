package com.drugs.accounting.controller;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.drugs.accounting.exceptions.TokenRefreshException;
import com.drugs.accounting.security.jwt.JwtTokenUtil;
import com.drugs.accounting.security.jwt.JwtUserDetailsService;
import com.drugs.accounting.security.jwt.RefreshTokenService;
import com.drugs.accounting.security.jwt.UserProfile;
import com.drugs.accounting.security.jwt.dto.JwtRequest;
import com.drugs.accounting.security.jwt.dto.JwtResponse;
import com.drugs.accounting.security.jwt.dto.TokenRefreshRequest;
import com.drugs.accounting.security.jwt.dto.TokenRefreshResponse;
import com.drugs.accounting.security.jwt.model.RefreshToken;

@RestController
@CrossOrigin
@RequestMapping("/accounting")
public class AuthenticationController {

	AuthenticationManager authenticationManager;
	JwtTokenUtil jwtTokenUtil;
	JwtUserDetailsService userDetailsService;
	RefreshTokenService refreshTokenService;

	public AuthenticationController(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil,
			JwtUserDetailsService userDetailsService, RefreshTokenService refreshTokenService) {
		this.authenticationManager = authenticationManager;
		this.jwtTokenUtil = jwtTokenUtil;
		this.userDetailsService = userDetailsService;
		this.refreshTokenService = refreshTokenService;
	}

	@PostMapping("/login")
	public ResponseEntity<?> authenticate(@RequestBody JwtRequest authenticationRequest) throws Exception {
		
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
				authenticationRequest.getUsername(),
				authenticationRequest.getPassword()
				));

		final UserProfile userDetails =
				(UserProfile) userDetailsService.loadUserByUsername(authenticationRequest.getUsername());

		final String token = jwtTokenUtil.generateToken(userDetails);
		final Set<String> routeNames = userDetails.getRouteNames();
		final RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUserId());

		return ResponseEntity.ok(new JwtResponse(token, refreshToken.getToken(), routeNames));
	}

	@PostMapping("/refreshtoken")
	public ResponseEntity<?> refreshtoken(@RequestBody TokenRefreshRequest request) {
		String requestRefreshToken = request.getRefreshToken();
		
		RefreshToken refreshToken = refreshTokenService
				.findByToken(requestRefreshToken)
				.orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token is not in database"));
		refreshTokenService.verifyExpiration(refreshToken);
		
		final UserProfile userDetails = (UserProfile) userDetailsService
				.loadUserByUsername(refreshToken.getAccount().getUserName());
		final String jwt = jwtTokenUtil.generateToken(userDetails);
		final RefreshToken refresh = refreshTokenService.createRefreshToken(userDetails.getUserId());
		
		return ResponseEntity.ok(new TokenRefreshResponse(jwt, refresh.getToken()));
	}

}
