package org.bbop.termgenie.tools;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Md5Tool {

	private Md5Tool() {
		// no instances
	}

	public static String md5(String string) {
		if (string != null) {
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				md.update(string.getBytes("UTF-8"));
				byte[] digest = md.digest();
				StringBuilder sb = new StringBuilder();
				for (byte b : digest) {
					sb.append(String.format("%02x", b & 0xff));
				}
				return sb.toString();
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}
}
