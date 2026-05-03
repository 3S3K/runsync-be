package com._s3k.runsync.domain.users.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com._s3k.runsync.domain.users.api.KakaoOauthClient;
import com._s3k.runsync.domain.users.dto.response.KakaoUserRes;
import com._s3k.runsync.domain.users.exception.AuthErrorCode;
import com._s3k.runsync.domain.users.exception.UserErrorCode;
import com._s3k.runsync.domain.users.repository.TokenRepository;
import com._s3k.runsync.domain.users.repository.UserRepository;
import com._s3k.runsync.entity.Token;
import com._s3k.runsync.entity.User;
import com._s3k.runsync.entity.enums.Provider;
import com._s3k.runsync.global.exception.GlobalException;
import com._s3k.runsync.global.security.jwt.JwtProvider;
import com._s3k.runsync.global.security.jwt.JwtValidator;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final TokenRepository tokenRepository;
	private final JwtProvider jwtProvider;
	private final JwtValidator jwtValidator;
	private final KakaoOauthClient kakaoOauthClient;

	@Transactional
	public User login(String code, HttpServletResponse response){
		//카카오 서버에서 access token 받아 user 정보 가져오기
		String kakaoAccessToken = kakaoOauthClient.getAccessToken(code);
		KakaoUserRes userInfo = kakaoOauthClient.getUserInfo(kakaoAccessToken);

		String kakaoId = String.valueOf(userInfo.getId());
		String nickname = userInfo.getKakao_account().getProfile().getNickname();
		String profileImage = userInfo.getKakao_account().getProfile().getProfile_image_url();

		User user = userRepository.findByProviderId(kakaoId)
			.orElseGet(() -> userRepository.save(User.createTmpUser(Provider.KAKAO, kakaoId, nickname, profileImage)));

		if(user.getIsDeleted()) {
			user.reactivate();
		}

		//access token, refresh token 생성 후 추가
		String accessToken = jwtProvider.createAccessToken(user);
		String refreshToken = jwtProvider.createRefreshToken(user);
		LocalDateTime expiryTime = jwtProvider.getRefreshTokenExpiry(refreshToken);

		tokenRepository.findByUser(user)
			.ifPresentOrElse(
				token -> token.updateRefreshToken(refreshToken, expiryTime),
				() -> tokenRepository.save(Token.of(user, refreshToken, expiryTime))
			);

		jwtProvider.addAccessTokenHeader(response, accessToken);
		jwtProvider.addRefreshTokenCookie(response, refreshToken);

		return user;
	}

	public void reissue(String refreshToken, HttpServletResponse response){
		try{
			if(!jwtValidator.validateToken(refreshToken)){
				throw new GlobalException(AuthErrorCode.INVALID_TOKEN);
			}
			Long userId = jwtValidator.getUserIdAndRole(refreshToken).getUserId();

			User user = userRepository.findById(userId)
				.orElseThrow(() -> new GlobalException(UserErrorCode.USER_NOT_FOUND));

			Token token = tokenRepository.findByRefreshToken(refreshToken)
				.orElseThrow(() -> new GlobalException(AuthErrorCode.INVALID_TOKEN));

			String newAccessToken = jwtProvider.createAccessToken(user);
			String newRefreshToken = jwtProvider.createRefreshToken(user);
			LocalDateTime newExpiry = jwtProvider.getRefreshTokenExpiry(newRefreshToken);

			token.updateRefreshToken(newRefreshToken, newExpiry);
			tokenRepository.save(token);

			jwtProvider.addAccessTokenHeader(response, newAccessToken);
			jwtProvider.addRefreshTokenCookie(response, newRefreshToken);

		} catch (GlobalException e){
			jwtProvider.deleteRefreshTokenCookie(response);
			throw e;
		}
	}

	@Transactional
	public void logout(String refreshToken, HttpServletResponse response){
		if(!jwtValidator.validateToken(refreshToken)){
			throw new GlobalException(AuthErrorCode.INVALID_TOKEN);
		}

		Long userId = jwtValidator.getUserIdAndRole(refreshToken).getUserId();
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new GlobalException(UserErrorCode.USER_NOT_FOUND));

		tokenRepository.deleteByUser(user);

		jwtProvider.deleteRefreshTokenCookie(response);
	}

}
