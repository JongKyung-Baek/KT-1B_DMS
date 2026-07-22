package kr.esob.fdms.commonlogic.fileapi;

import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class FileApiClient {
	private static final int CONNECT_TIMEOUT_MS = 10000;
	private static final int READ_TIMEOUT_MS = 60000;

	public void upload(File file, String fileName, String folder) {
		if (file == null || !file.isFile()) {
			throw new IllegalArgumentException("Upload file does not exist: " + file);
		}
		try (InputStream input = new FileInputStream(file)) {
			upload(input, file.length(), fileName, folder);
		} catch (IOException e) {
			throw new IllegalStateException("File API upload failed: " + e.getMessage(), e);
		}
	}

	public void upload(InputStream input, long contentLength, String fileName, String folder) {
		String baseUrl = trimTrailingSlash(SystemConfig.getSystemConfigValue("FILE_API_BASE_URL"));
		String apiKey = SystemConfig.getSystemConfigValue("FILE_API_KEY");
		if (baseUrl.isEmpty()) {
			throw new IllegalStateException("FILE_API_BASE_URL is empty");
		}
		if (apiKey == null || apiKey.trim().isEmpty()) {
			throw new IllegalStateException("FILE_API_KEY is empty");
		}
		if (fileName == null || fileName.trim().isEmpty()) {
			throw new IllegalArgumentException("fileName is empty");
		}
		if (input == null) {
			throw new IllegalArgumentException("input is null");
		}

		HttpURLConnection connection = null;
		try {
			String url = baseUrl + "/api/v1/files/" + encodePathSegment(fileName)
					+ "?overwrite=true"
					+ (folder == null || folder.trim().isEmpty() ? "" : "&folder=" + encodeQueryValue(folder));
			System.out.println("[FILE_API_UPLOAD] request url=" + url + ", fileName=" + fileName + ", folder=" + folder + ", size=" + contentLength);
			connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
			connection.setReadTimeout(READ_TIMEOUT_MS);
			connection.setRequestMethod("PUT");
			connection.setDoOutput(true);
			connection.setRequestProperty("X-TDDS-API-Key", apiKey);
			connection.setRequestProperty("Content-Type", "application/octet-stream");
			if (contentLength >= 0) {
				connection.setFixedLengthStreamingMode(contentLength);
			}

			try (OutputStream output = connection.getOutputStream()) {
				byte[] buffer = new byte[8192];
				int read;
				while ((read = input.read(buffer)) != -1) {
					output.write(buffer, 0, read);
				}
			}

			int status = connection.getResponseCode();
			System.out.println("[FILE_API_UPLOAD] response status=" + status + ", fileName=" + fileName + ", folder=" + folder);
			if (status < 200 || status >= 300) {
				throw new IllegalStateException("File API upload failed. status=" + status + ", body=" + readBody(connection));
			}
		} catch (IOException e) {
			System.out.println("[FILE_API_UPLOAD] failed. fileName=" + fileName + ", folder=" + folder + ", message=" + e.getMessage());
			throw new IllegalStateException("File API upload failed: " + e.getMessage(), e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	public byte[] download(String fileName, String folder) {
		String baseUrl = trimTrailingSlash(SystemConfig.getSystemConfigValue("FILE_API_BASE_URL"));
		String apiKey = SystemConfig.getSystemConfigValue("FILE_API_KEY");
		if (baseUrl.isEmpty()) {
			throw new IllegalStateException("FILE_API_BASE_URL is empty");
		}
		if (apiKey == null || apiKey.trim().isEmpty()) {
			throw new IllegalStateException("FILE_API_KEY is empty");
		}
		if (fileName == null || fileName.trim().isEmpty()) {
			throw new IllegalArgumentException("fileName is empty");
		}

		HttpURLConnection connection = null;
		try {
			String url = baseUrl + "/api/v1/files/" + encodePathSegment(fileName)
					+ (folder == null || folder.trim().isEmpty() ? "" : "?folder=" + encodeQueryValue(folder));
			System.out.println("[FILE_API_DOWNLOAD] request url=" + url);
			connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
			connection.setReadTimeout(READ_TIMEOUT_MS);
			connection.setRequestMethod("GET");
			connection.setRequestProperty("X-TDDS-API-Key", apiKey);

			int status = connection.getResponseCode();
			System.out.println("[FILE_API_DOWNLOAD] response status=" + status + ", fileName=" + fileName + ", folder=" + folder);
			if (status < 200 || status >= 300) {
				throw new IllegalStateException("File API download failed. status=" + status + ", body=" + readBody(connection));
			}

			try (InputStream input = connection.getInputStream();
				 ByteArrayOutputStream output = new ByteArrayOutputStream()) {
				byte[] buffer = new byte[8192];
				int read;
				while ((read = input.read(buffer)) != -1) {
					output.write(buffer, 0, read);
				}
				byte[] bytes = output.toByteArray();
				System.out.println("[FILE_API_DOWNLOAD] success bytes=" + bytes.length + ", fileName=" + fileName);
				return bytes;
			}
		} catch (IOException e) {
			throw new IllegalStateException("File API download failed: " + e.getMessage(), e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	private String readBody(HttpURLConnection connection) {
		InputStream stream = null;
		try {
			stream = connection.getErrorStream();
			if (stream == null) {
				stream = connection.getInputStream();
			}
			if (stream == null) {
				return "";
			}
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
				StringBuilder body = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					body.append(line);
				}
				return body.toString();
			}
		} catch (IOException e) {
			return "";
		}
	}

	private static String trimTrailingSlash(String value) {
		if (value == null) {
			return "";
		}
		String result = value.trim();
		while (result.endsWith("/")) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}

	private static String encodePathSegment(String value) throws IOException {
		return URLEncoder.encode(value, "UTF-8").replace("+", "%20");
	}

	private static String encodeQueryValue(String value) throws IOException {
		return URLEncoder.encode(value, "UTF-8").replace("+", "%20");
	}
}
