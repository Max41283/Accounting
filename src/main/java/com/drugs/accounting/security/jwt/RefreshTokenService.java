package com.drugs.accounting.security.jwt;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.drugs.accounting.dao.AccountRepository;
import com.drugs.accounting.exceptions.TokenRefreshException;
import com.drugs.accounting.security.jwt.dao.RefreshTokenRepository;
import com.drugs.accounting.security.jwt.model.RefreshToken;

@Service
public class RefreshTokenService {

	@Value("${jwt.refresh.expirations}")
	private Long refreshTokenDurationMs;

	private RefreshTokenRepository refreshTokenRepository;
	private AccountRepository accountRepository;
	
	public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, AccountRepository accountRepository) {
		this.refreshTokenRepository = refreshTokenRepository;
		this.accountRepository = accountRepository;
	}

	public Optional<RefreshToken> findByToken(String token) {
		return refreshTokenRepository.findByToken(token);
	}

	@Transactional
	public RefreshToken createRefreshToken(Long userId) {
		refreshTokenRepository.deleteByAccountId(userId);
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setAccount(accountRepository.findById(userId).get());
		refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
		refreshToken.setToken(UUID.randomUUID().toString());
		refreshToken = refreshTokenRepository.save(refreshToken);
		return refreshToken;
	}

	public RefreshToken verifyExpiration(RefreshToken token) {
		if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
			refreshTokenRepository.delete(token);
			throw new TokenRefreshException(token.getToken(),
					"Refresh token was expired. Please make a new signin request");
		}
		return token;
	}

}
