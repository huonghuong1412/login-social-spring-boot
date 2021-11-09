package com.example.demo.common;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class PkceUtil {

	public static String genCodeVerifier() {
		SecureRandom sr = new SecureRandom();
		byte[] code = new byte[32];
		sr.nextBytes(code);
		String verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(code);
		return verifier;
	}

	public static String genCodeChallenge(String codeVerifier) {
		String result = null;
		try {
			byte[] bytes = codeVerifier.getBytes("US-ASCII");
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(bytes, 0, bytes.length);
			byte[] digest = md.digest();
			result = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
		} catch (Exception ex) {
			ex.getMessage();
		}
		return result;
	}

	public static void main(String[] args) {
		String codeVerifier = genCodeVerifier();
		System.out.println("Code verifier = " + codeVerifier);
		String codeChallenge = genCodeChallenge(codeVerifier);
		System.out.println("Code challenge = " + codeChallenge);
	}

}