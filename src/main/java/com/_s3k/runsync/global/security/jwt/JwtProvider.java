package com._s3k.runsync.global.security.jwt;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com._s3k.runsync.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtProvider {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.access-token-validity}")
	private long ACCESS_TOKEN_VALIDITY_MILLIS;

	@Value("${jwt.refresh-token-validity}")
	private long REFRESH_TOKEN_VALIDITY_MILLIS;

	@Value("${jwt.cookie.domain}")
	private String cookieDomain;

	@Value("${jwt.cookie.secure}")
	private boolean isSecure;

	@Value("${jwt.cookie.same-site}")
	private String sameSite;

	public String createAccessToken(User user) {
		return Jwts.builder()
			.subject(String.valueOf(user.getId()))
			.claim("role", user.getRole().name())
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_MILLIS))
			.signWith(Keys.hmacShaKeyFor(secret.getBytes()))
			.compact();
	}

	public String createRefreshToken(User user) {
		return Jwts.builder()
			.subject(String.valueOf(user.getId()))
			.claim("role", user.getRole().name())
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY_MILLIS))
			.signWith(Keys.hmacShaKeyFor(secret.getBytes()))
			.compact();
	}

	public void addAccessTokenHeader(HttpServletResponse response, String token){
		response.setHeader("Authorization", "Bearer " + token);
	}

	public void addRefreshTokenCookie(HttpServletResponse response, String token){
		ResponseCookie cookie = ResponseCookie.from("refreshToken", token)
			.path("/")
			.httpOnly(true)
			.secure(isSecure)
			.maxAge(REFRESH_TOKEN_VALIDITY_MILLIS / 1000)
			.sameSite(sameSite)
			.domain(cookieDomain.isBlank() ? null : cookieDomain)
			.build();

		response.addHeader("Set-Cookie", cookie.toString());
	}

	public void deleteRefreshTokenCookie(HttpServletResponse response){
		ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
			.path("/")
			.httpOnly(true)
			.secure(isSecure)
			.maxAge(0)
			.sameSite(sameSite)
			.domain(cookieDomain.isBlank() ? null : cookieDomain)
			.build();

		response.addHeader("Set-Cookie", cookie.toString());
	}

	public LocalDateTime getRefreshTokenExpiry(String token){
		Claims claims = Jwts.parser()
			.verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
			.build()
			.parseSignedClaims(token)
			.getPayload();

		return LocalDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneId.systemDefault());
	}
}
