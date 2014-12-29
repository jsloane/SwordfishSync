package mymedia.auth;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import mymedia.services.MyMediaLifecycle;

import org.springframework.web.filter.DelegatingFilterProxy;

public class SecurityDelegatingFilterProxy extends DelegatingFilterProxy {
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		if (MyMediaLifecycle.authEnabled) {
			// check security
			super.doFilter(request, response, filterChain);
		} else {
			// bypass security
			filterChain.doFilter(request, response);
		}
	}
	
}
