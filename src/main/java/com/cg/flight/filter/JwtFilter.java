package com.cg.flight.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cg.flight.services.JwtUtil;
import com.cg.flight.services.LoginUserService;

import io.jsonwebtoken.JwtException;


@Component
public class JwtFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtil jwtUtil;
	
	@Autowired 
	private LoginUserService userService;
	
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		if(request.getMethod().equalsIgnoreCase("OPTIONS"))
		{
			response.setStatus(HttpServletResponse.SC_OK);
			return;
		}
		final String authHeader = request.getHeader("Authorization");
		
		String username = null;
		String jwt = null;
		
		try {
			
		
		if(authHeader!=null && authHeader.startsWith("Bearer "))
		{
			jwt = authHeader.substring(7);
			username = jwtUtil.extractUsername(jwt);
		}
		
		if(username!=null && SecurityContextHolder.getContext().getAuthentication()==null )
		{
			UserDetails userDetails = this.userService.loadUserByUsername(username);
			if(jwtUtil.validateToken(jwt, userDetails))
			{
				UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken 
				= new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
				usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
			}
		}
		
		}
		catch(JwtException e) {
			response.setContentType("application/json");
			response.getWriter().write("\"message\":\"Jwt Error\"");
			return;
		}
		filterChain.doFilter(request, response);
	}

}
