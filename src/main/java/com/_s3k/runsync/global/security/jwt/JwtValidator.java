package com._s3k.runsync.global.security.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com._s3k.runsync.global.security.jwt.dto.JwtUserInfo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtValidator {

	@Value("${jwt.secret}")
	private String secret;

	public boolean validateToken(String token) {
		try{
			Jwts.parser()
				.verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
				.build()
				.parseSignedClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public JwtUserInfo getUserIdAndRole(String token) {
		Claims claims = Jwts.parser()
			.verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
			.build()
			.parseSignedClaims(token)
			.getPayload();

		Long userId = Long.valueOf(claims.getSubject());
		String role = claims.get("role", String.class);

		return JwtUserInfo.of(userId, role);
	}

	public String extractAccessToken(HttpServletRequest request) {
		String header = request.getHeader("Authorization");

		if (header != null && header.startsWith("Bearer ")) {
			return header.substring(7);
		}
		return null;
	}
}
