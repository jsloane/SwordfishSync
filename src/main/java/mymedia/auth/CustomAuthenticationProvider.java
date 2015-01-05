package mymedia.auth;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class CustomAuthenticationProvider implements AuthenticationProvider {
	
	public static boolean authEnabled;
	public static String username;
	public static String password;
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = authentication.getName();
		String password = authentication.getCredentials().toString();
		
		// use the credentials to try to authenticate against the third party system
		List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
		grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password) && username.equals(CustomAuthenticationProvider.username) && password.equals(CustomAuthenticationProvider.password)) {
            return new UsernamePasswordAuthenticationToken(authentication.getName(), authentication.getCredentials(), grantedAuthorities);
        } else {
            return null;
        }
	}
	
	@Override
	public boolean supports(Class<? extends Object> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
	
}
