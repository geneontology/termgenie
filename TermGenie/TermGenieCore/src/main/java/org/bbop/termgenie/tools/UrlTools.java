package org.bbop.termgenie.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;


public class UrlTools {

	public static String getErrorMsg(HttpURLConnection connection) {
		String errorMsg = null;
		InputStream errorStream = null;
		try {
			errorStream = connection.getErrorStream();
			if (errorStream != null) {
				errorMsg = IOUtils.toString(errorStream);
			}
			errorMsg = StringUtils.trimToNull(errorMsg);
		}
		catch (IOException e) {
			// ignore errors, while trying to retrieve the error message
		}
		finally {
			IOUtils.closeQuietly(errorStream);
		}
		return errorMsg;
	}
	
	public static IOException createStatusCodeException(int status, HttpURLConnection con) {
		// try to check error stream
		String errorMsg = UrlTools.getErrorMsg(con);
		
		// construct message for exception
		StringBuilder sb = new StringBuilder("Unexpected HTTP status code: "+status);
		
		if (errorMsg != null) {
			sb.append(" Details: ");
			sb.append(errorMsg);
		}
		return new IOException(sb.toString());
	}
	
	public static String getCharset(URLConnection connection) {
		// try to detect charset
		String contentType = connection.getHeaderField("Content-Type");
		String charset = null;

		if (contentType != null) {
			for (String param : contentType.replace(" ", "").split(";")) {
				if (param.startsWith("charset=")) {
					charset = param.split("=", 2)[1];
					break;
				}
			}
		}
		return charset;
	}
	
	public static HttpURLConnection preparePost(URL url, String data) throws IOException {
		// prepare data
		byte[] postData = data.getBytes(Charset.forName("UTF-8"));
		int postDataLength = postData.length;
		
		// prepare connection
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setInstanceFollowRedirects(false);
		con.setRequestMethod("POST");
		con.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded"); 
		con.setRequestProperty( "charset", "utf-8");
		con.setRequestProperty( "Content-Length", Integer.toString(postDataLength));
		con.setUseCaches(false);
		// POST content
		OutputStream outputStream = con.getOutputStream();
		outputStream.write(postData);
		outputStream.flush();
		
		return con;
	}
}
