package com._s3k.runsync.global.security.jwt.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com._s3k.runsync.domain.users.repository.UserRepository;
import com._s3k.runsync.global.security.jwt.JwtValidator;
import com._s3k.runsync.global.security.jwt.dto.JwtUserInfo;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtValidator jwtValidator;
	private final UserRepository userRepository;

	public JwtAuthenticationFilter(JwtValidator jwtValidator, UserRepository userRepository){
		this.jwtValidator = jwtValidator;
		this.userRepository = userRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws
		ServletException, IOException {
		String token = jwtValidator.extractAccessToken(request);

		if(token != null && jwtValidator.validateToken(token)) {
			JwtUserInfo info = jwtValidator.getUserIdAndRole(token);
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(info.getUserId(), null, List.of(new SimpleGrantedAuthority("ROLE_"+info.getRole())));
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		filterChain.doFilter(request, response);
	}
}
