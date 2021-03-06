package com.drugs.accounting.security.filters;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import com.drugs.accounting.security.jwt.JwtUserDetailsService;
import com.drugs.accounting.security.jwt.UserProfile;

@Order(20)
@Component
public class RoleAccessFilter extends GenericFilterBean {
	
	JwtUserDetailsService jwtUserDetailService;
	
	public RoleAccessFilter(JwtUserDetailsService jwtUserDetailService) {
		this.jwtUserDetailService = jwtUserDetailService;
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		Principal principal = request.getUserPrincipal();
		if (principal != null) {
			UserProfile userDetails = (UserProfile) SecurityContextHolder
					.getContext()
					.getAuthentication()
					.getPrincipal();
			List<String> roles = userDetails.getAuthorities()
					.stream()
					.map(r -> r.toString().substring(5))
					.collect(Collectors.toList());
			if (!roles.contains("ADMIN")) {
				if (!userDetails.getRouteNames().contains(request.getServletPath())) {
					responseError403(response, userDetails.getUsername() + " - access denied");
					return;
				}
			}
		}
		chain.doFilter(request, response);
	}
	
	private void responseError403(HttpServletResponse response, String errorMessage) throws IOException {
		logger.error(errorMessage);
		response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getOutputStream().println("{ \"Unauthorized error\": \"" + errorMessage + "\" }");
	}

}
