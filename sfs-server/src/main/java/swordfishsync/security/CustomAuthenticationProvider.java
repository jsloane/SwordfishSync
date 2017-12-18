package swordfishsync.security;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import swordfishsync.service.SettingService;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

	@Resource
	SettingService settingService;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = authentication.getName();
		String password = authentication.getCredentials().toString();

		String expectedUsername = settingService.getValue(SettingService.CODE_APP_SECURITY_USERNAME, String.class);
		String expectedPassword = settingService.getValue(SettingService.CODE_APP_SECURITY_PASSWORD, String.class);

		// use the credentials to try to authenticate against the third party system
		if (StringUtils.equals(username, expectedUsername) && StringUtils.equals(password, expectedPassword)) {
			List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
			grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
			return new UsernamePasswordAuthenticationToken(username, password, grantedAuthorities);
		} else {
			return null;
		}
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

}
