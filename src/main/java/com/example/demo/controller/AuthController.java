package com.example.demo.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.ClientProtocolException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.example.demo.common.Erole;
import com.example.demo.common.JwtUtils;
import com.example.demo.common.TypeAccount;
import com.example.demo.entity.GooglePojo;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.ZaloPojo;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.GoogleUtils;
import com.example.demo.utils.FacebookUtils;
import com.example.demo.utils.ZaloUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;;

@CrossOrigin(origins = "*")
@RestController
public class AuthController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private GoogleUtils googleUtils;

	@Autowired
	private ZaloUtils zaloUtils;

	@Autowired
	private FacebookUtils restFb;

	@Autowired
	private PasswordEncoder encoder;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserRepository userRepository;

	@RequestMapping(value = "/test")
	public String test() {
		return "Test";
	}

	@RequestMapping(value = "/login/google")
	public RedirectView loginGoogle(HttpServletRequest request) throws ClientProtocolException, IOException {
		String code = request.getParameter("code");
		if (code == null || code.isEmpty()) {
			return new RedirectView("http://localhost:3000/oauth/redirect?status=false");
		}
		ObjectMapper mapper = new ObjectMapper();
		String token = googleUtils.getToken(code);
		JsonNode node = mapper.readTree(token);
		String accessToken = node.get("access_token").textValue();
		String jwt_token = node.get("id_token").textValue();

		GooglePojo googlePojo = null;
		if (accessToken != null) {
			googlePojo = googleUtils.getUserInfo(accessToken);
		}
		UserDetails userDetail = googleUtils.buildUser(googlePojo);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetail, null,
				userDetail.getAuthorities());
		authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		SecurityContextHolder.getContext().setAuthentication(authentication);

		User user = userRepository.findOneByEmail(userDetail.getUsername());

		if (user == null) {
			user = new User();
			user.setDisplay(1);
			user.setEmail(googlePojo.getEmail());
			user.setUsername(googlePojo.getEmail());
			user.setPicture(googlePojo.getPicture());
			user.setName(googlePojo.getName());
			user.setPassword(encoder.encode(googlePojo.getSocial_user_id()));
			user.setType_account(TypeAccount.GOOGLE);
			List<Role> roles = new ArrayList<Role>();
			Role userRole = roleRepository.findOneByName(Erole.ROLE_USER)
					.orElseThrow(() -> new RuntimeException("Error: Role is not found"));
			roles.add(userRole);
			user.setRoles(roles);
			userRepository.save(user);
		}
//		return ResponseEntity.ok(
//				new JwtResponse(jwt_token, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles));
		return new RedirectView("http://localhost:3000/oauth/redirect?status=true&token=" + jwt_token + "&username="
				+ user.getUsername());
	}

	@RequestMapping("/login-facebook")
	public RedirectView loginFacebook(HttpServletRequest request) throws ClientProtocolException, IOException {
		String code = request.getParameter("code");

		if (code == null || code.isEmpty()) {
			return new RedirectView("http://localhost:3000/oauth/redirect?status=false");
		}
		ObjectMapper mapper = new ObjectMapper();
		String token = restFb.getToken(code);
		JsonNode node = mapper.readTree(token);
		String accessToken = node.get("access_token").textValue();

		com.restfb.types.User user = restFb.getUserInfo(accessToken);
		System.out.println(user.getEmail() + " " + user.getName() + " " + user.getId());
		UserDetails userDetail = restFb.buildUser(user);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetail, null,
				userDetail.getAuthorities());
		authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		SecurityContextHolder.getContext().setAuthentication(authentication);

		User userEntity = userRepository.findOneByEmail(userDetail.getUsername());

		if (userEntity == null) {
			userEntity = new User();
			userEntity.setDisplay(1);
			userEntity.setEmail("facebook-3142039976019186@yopmail.com");
			userEntity.setUsername(user.getId());
			userEntity.setName(user.getName());
			userEntity.setPassword(encoder.encode(user.getId()));
			userEntity.setType_account(TypeAccount.FACEBOOK);
			List<Role> roles = new ArrayList<Role>();
			Role userRole = roleRepository.findOneByName(Erole.ROLE_USER)
					.orElseThrow(() -> new RuntimeException("Error: Role is not found"));
			roles.add(userRole);
			userEntity.setRoles(roles);
			userRepository.save(userEntity);
		}
		Authentication authen = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(userEntity.getUsername(), userEntity.getUsername()));
		String jwt = jwtUtils.generateJwtToken(authen);
		return new RedirectView("http://localhost:3000/oauth2/redirect?status=true&token=" + jwt + "&username="
				+ userEntity.getUsername());
	}

	@RequestMapping(value = "/login/zalo")
	public RedirectView loginZalo(HttpServletRequest request) throws ClientProtocolException, IOException {
		String code = request.getParameter("code");
		if (code == null || code.isEmpty()) {
			return new RedirectView("http://localhost:3000/oauth/redirect?status=false");
		}
		ObjectMapper mapper = new ObjectMapper();
		String token = zaloUtils.getToken(code);
		System.out.println(token);
		JsonNode node = mapper.readTree(token);
		String accessToken = node.get("access_token").textValue();
		ZaloPojo googlePojo = null;
		if (accessToken != null) {
			googlePojo = zaloUtils.getUserInfo(accessToken);
		}
		UserDetails userDetail = zaloUtils.buildUser(googlePojo);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetail, null,
				userDetail.getAuthorities());
		authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		User user = userRepository.findOneByUsername(userDetail.getUsername());
		if (user == null) {
			user = new User();
			user.setDisplay(1);
			user.setEmail("zalo-" + googlePojo.getId() + "@yopmail.com");
			user.setUsername(googlePojo.getId());
			user.setName(googlePojo.getName());
			user.setPassword(encoder.encode(googlePojo.getId()));
			user.setType_account(TypeAccount.ZALO);
			List<Role> roles = new ArrayList<Role>();
			Role userRole = roleRepository.findOneByName(Erole.ROLE_USER)
					.orElseThrow(() -> new RuntimeException("Error: Role is not found"));
			roles.add(userRole);
			user.setRoles(roles);
			userRepository.save(user);
		}
		Authentication authen = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getUsername()));
		String jwt = jwtUtils.generateJwtToken(authen);
		return new RedirectView(
				"http://localhost:3000/oauth/redirect?status=true&token=" + jwt + "&username=" + user.getUsername());
	}

}
