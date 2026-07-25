package kr.esob.fdms.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.UUID;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.util.seed.seed.Seed128Cipher;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

@Slf4j
@Service
public class FileUtil {
	private static String charset = "UTF-8";
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 8;

	public static void mkdir(String dir) {
		File folder= new File(dir);
		if(!folder.exists()) {
			folder.mkdir();
		}
	}

	/**
	 * 파일의 고유한 이름을 구한다.
	 * @return
	 */
	public static String getFileUuid() {
		UUID uid = UUID.randomUUID();

		return System.currentTimeMillis() + "_" + uid.toString();
	}

	public static boolean copyFile(String orgPath, String targetPath) {
		boolean ret = true;
		try {
			BufferedInputStream source = new BufferedInputStream(new FileInputStream(new File(orgPath)));
			BufferedOutputStream destination = new BufferedOutputStream(new FileOutputStream(new File(targetPath)));
			byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

			try {
				int n = 0;
				while (-1 != (n = source.read(buffer))) {
					destination.write(buffer, 0, n);
				}
				destination.flush();
			} finally {
				if (source != null) {
					source.close();
				}
				if (destination != null) {
					destination.close();
				}
			}
		} catch (Exception e) {
			log.warn("File copy failed. cause={}", e.getClass().getSimpleName());
			ret = false;
		}
		return ret;
	}

	/**
	 *
	 * @param srcUrl
	 * @param dstUrl
	 * @param srcFilePath - 파일명을 포함한 FULL PATH
	 * @param dstFilePath - 폴더명만
	 * @param dstFileNm - 파일명만
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public static JSONObject callSender(String srcUrl, String dstUrl, String srcFilePath, String dstFilePath, String dstFileNm) throws UnsupportedEncodingException {
		int responseCode = 0;
		JSONObject result = new JSONObject();
		StringBuffer sbResponse = new StringBuffer();
		DataOutputStream osw = null;
		BufferedReader br = null;
		
		srcUrl = requireHttpsTransferBaseUrl(
				decryptRequiredTransferArgument(srcUrl, "source URL"), "source URL");
		srcUrl += "common/fileTransfer/sender";
		
		dstUrl = requireHttpsTransferBaseUrl(
				decryptRequiredTransferArgument(dstUrl, "destination URL"), "destination URL");
		dstUrl += "common/fileTransfer/receiver";
		dstUrl = Seed128Cipher.encrypt(dstUrl, Constant.legacyCryptoKeyBytes(), Constant.SEED_ENCODING);
		// The sender endpoint decrypts these three values. Decrypt a copy here
		// only to reject plaintext, malformed ciphertext and encrypted blanks.
		decryptRequiredTransferArgument(srcFilePath, "source file path");
		decryptRequiredTransferArgument(dstFilePath, "destination file path");
		decryptRequiredTransferArgument(dstFileNm, "destination file name");
		
		try {
			URLConnection connection = new URL(srcUrl).openConnection();
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(30000);

			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			// response data
			//connection.setDoOutput(true);
			//connection.setRequestProperty("Content-Type", "x-www-form-urlencoded; boundary=" + boundary);

			//			OutputStream output = connection.getOutputStream();
			//			PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
			//
			//			setSendParam(writer, "dstUrl", dstUrl);
			//			setSendParam(writer, "srcFilePath", srcFilePath);
			//			setSendParam(writer, "dstFilePath", dstFilePath);

			String param = "dstUrl=" + URLEncoder.encode(dstUrl, "UTF-8")
			+ "&srcFilePath=" + URLEncoder.encode(srcFilePath, charset)
			+ "&dstFilePath=" + URLEncoder.encode(dstFilePath, charset)
			+ "&dstFileNm=" + URLEncoder.encode(dstFileNm, charset);

			osw = new DataOutputStream(connection.getOutputStream());
			// 서버로 전송할 데이터
			osw.writeBytes(param);
			osw.flush();

			responseCode = ((HttpURLConnection) connection).getResponseCode();

			// get response
			if(200 == responseCode) {
				// success
				br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

				String line = null;

				while ((line = br.readLine()) != null) {
					sbResponse.append(line);
				}
			}
			else {
				// fail
				sbResponse.append(((HttpURLConnection) connection).getResponseMessage());
			}
		}
		catch(Exception e) {
			log.warn("Legacy file transfer failed. cause={}", e.getClass().getSimpleName());
		}
		finally {
			if(osw != null) { try { osw.close(); } catch (IOException e) { } }
			if(br != null) { try { br.close(); } catch (IOException e) { } }
		}

		if(200 == responseCode) {
			return JSONObject.fromObject(sbResponse.toString());
		}
		else {
			result.put("result", false);
			result.put("response", sbResponse.toString());
		}

		return result;
	}

	private static String requireHttpsTransferBaseUrl(String value, String fieldName) {
		try {
			URI uri = new URI(value == null ? "" : value.trim());
			if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
					|| uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null) {
				throw new IllegalArgumentException(
						"Legacy file-transfer " + fieldName + " must be an HTTPS base URL.");
			}
			String normalized = uri.toString();
			return normalized.endsWith("/") ? normalized : normalized + "/";
		} catch (java.net.URISyntaxException exception) {
			throw new IllegalArgumentException(
					"Legacy file-transfer " + fieldName + " must be an HTTPS base URL.", exception);
		}
	}

	private static String decryptRequiredTransferArgument(String encryptedValue, String fieldName)
			throws UnsupportedEncodingException {
		if (encryptedValue == null || encryptedValue.trim().isEmpty()) {
			throw new IllegalArgumentException("Legacy file-transfer " + fieldName + " is required.");
		}
		try {
			String plaintext = Seed128Cipher.decrypt(
					encryptedValue, Constant.legacyCryptoKeyBytes(), Constant.SEED_ENCODING);
			if (plaintext == null || plaintext.trim().isEmpty()) {
				throw new IllegalArgumentException("Legacy file-transfer " + fieldName + " is blank.");
			}
			String canonicalCiphertext = Seed128Cipher.encrypt(
					plaintext, Constant.legacyCryptoKeyBytes(), Constant.SEED_ENCODING);
			if (!encryptedValue.equals(canonicalCiphertext)) {
				throw new IllegalArgumentException(
						"Legacy file-transfer " + fieldName + " is not valid ciphertext.");
			}
			return plaintext;
		} catch (IllegalArgumentException exception) {
			throw exception;
		} catch (RuntimeException exception) {
			throw new IllegalArgumentException(
					"Legacy file-transfer " + fieldName + " is not valid ciphertext.", exception);
		}
	}

	/**
	 * Encrypts one argument for the legacy file-transfer contract.
	 *
	 * All five {@link #callSender(String, String, String, String, String)}
	 * arguments are ciphertext. Keeping this conversion in one place makes a
	 * plaintext call site easy to detect and prevents blank configuration values
	 * from being sent to the transfer server.
	 */
	public static String encryptTransferArgument(String value) throws UnsupportedEncodingException {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException("Legacy file-transfer argument is required.");
		}
		return Seed128Cipher.encrypt(value, Constant.legacyCryptoKeyBytes(), Constant.SEED_ENCODING);
	}

	/**
	 * Validates the sender/receiver response before a caller persists a remote
	 * path or marks its database work successful.
	 */
	public static String requireSuccessfulTransferFileName(JSONObject result) {
		if (result == null || !result.containsKey("result")
				|| !Boolean.parseBoolean(String.valueOf(result.get("result")))) {
			throw new IllegalStateException("Legacy file transfer was not completed.");
		}

		Object rawFileName = result.get("fileNm");
		String fileName = rawFileName == null ? "" : String.valueOf(rawFileName).trim();
		if (!isSafeTransferFileName(fileName)) {
			throw new IllegalStateException("Legacy file transfer returned an invalid file name.");
		}
		return fileName;
	}

	private static boolean isSafeTransferFileName(String fileName) {
		if (fileName == null || fileName.isEmpty() || fileName.length() > 255
				|| ".".equals(fileName) || "..".equals(fileName) || fileName.contains("..")) {
			return false;
		}
		return fileName.matches("[A-Za-z0-9][A-Za-z0-9._-]{0,254}");
	}


	public static JSONObject sendFile(String dstUrl, String srcFilePath, String dstFilePath, String dstFileNm) throws UnsupportedEncodingException {
		//		String url = "http://localhost:8080/common/upload/receiver";
		StringBuffer sbResponse = new StringBuffer();
		JSONObject result = new JSONObject();
		int responseCode = 0;
		String CRLF = "\r\n"; // Line separator required by multipart/form-data.
		String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
		//		File textFile = new File("/path/to/file.txt");

		dstUrl = Seed128Cipher.decrypt(dstUrl, Constant.legacyCryptoKeyBytes(), Constant.SEED_ENCODING);
		srcFilePath = Seed128Cipher.decrypt(srcFilePath, Constant.legacyCryptoKeyBytes(), Constant.SEED_ENCODING);
		
		try {
			File binaryFile = new File(srcFilePath);
			URL u = new URL(dstUrl);
			HttpURLConnection connection = (HttpURLConnection) u.openConnection();

//			URLConnection connection = new URL(dstUrl).openConnection();
//			URLConnection connection
			connection.setChunkedStreamingMode(4096);
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);


			OutputStream output = connection.getOutputStream();
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);

			// Send normal param.
			// 여러개의 Content-Disposition 동시 전송 가능.
			// 함수로 만들면 이상하게 param을 전혀 넘기지 못함
			//			setSendParam(writer, "orgFileNm", binaryFile.getName());
			//			setSendParam(writer, "dstFilePath", dstFilePath);
			writer.append("--" + boundary).append(CRLF);
			writer.append("Content-Disposition: form-data; name=\"orgFileNm\"").append(CRLF);
			writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
			writer.append(CRLF).append(Seed128Cipher.encrypt(binaryFile.getName(), Constant.legacyCryptoKeyBytes(), Constant.SEED_ENCODING)).append(CRLF).flush();

			writer.append("--" + boundary).append(CRLF);
			writer.append("Content-Disposition: form-data; name=\"dstFilePath\"").append(CRLF);
			writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
			writer.append(CRLF).append(dstFilePath).append(CRLF).flush();

			writer.append("--" + boundary).append(CRLF);
			writer.append("Content-Disposition: form-data; name=\"dstFileNm\"").append(CRLF);
			writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
			writer.append(CRLF).append(dstFileNm).append(CRLF).flush();

			// Send binary file.
			writer.append("--" + boundary).append(CRLF);
			// filename 파라메터는 꼭 있어야 함.
			// 없으면 receiver에서 filePart.getSubmittedFileName()이 null임
			writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"" + binaryFile.getName() + "\"").append(CRLF);
//			writer.append("Content-Range: bytes=0-4095").append(CRLF);
			writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
			writer.append("Content-Transfer-Encoding: binary").append(CRLF);
//			writer.append("Content-Length: 4096").append(CRLF);
			writer.append(CRLF).flush();
			Files.copy(binaryFile.toPath(), output);
			output.flush(); // Important before continuing with writer!
			writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

			// End of multipart/form-data.
			writer.append("--" + boundary + "--").append(CRLF).flush();

			// Request is lazily fired whenever you need to obtain information about response.
			responseCode = ((HttpURLConnection) connection).getResponseCode();

			// get response
			if(200 == responseCode) {
				// success
				BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

				String line = null;

				while ((line = br.readLine()) != null) {
					sbResponse.append(line);
				}
			}
			else {
				// fail
				sbResponse.append(((HttpURLConnection) connection).getResponseMessage());
			}
		}
		catch(Exception e) {
			log.warn("Legacy multipart transfer failed. cause={}", e.getClass().getSimpleName());
		}

		if(200 == responseCode) {
			return JSONObject.fromObject(sbResponse.toString());
		}
		else {
			result.put("result", false);
			result.put("response", sbResponse.toString());

			return result;
		}
	}

	//	public static JSONObject sendFile(String dstUrl, String srcFilePath, String dstFilePath) throws MalformedURLException, IOException {
	////		String url = "http://localhost:8080/common/fileTransfer/receiver";
	//		String charset = "UTF-8";
	//		// File textFile = new File("/path/to/file.txt");
	//		File binaryFile = new File("C:\\test.txt");
	//        // Just generate some unique random value.
	//		String boundary = Long.toHexString(System.currentTimeMillis());
	//		String CRLF = "\r\n"; // Line separator required by multipart/form-data.
	//
	//		URLConnection connection = new URL(dstUrl).openConnection();
	//		connection.setDoOutput(true);
	//		connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
	//
	//		try {
	//			OutputStream output = connection.getOutputStream();
	//			PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
	//
	//			// Send normal param.
	//			// 여러개의 Content-Disposition 동시 전송 가능.
	//			writer.append("--" + boundary).append(CRLF);
	//			writer.append("Content-Disposition: form-data; name=\"orgFileNm\"").append(CRLF);
	//			writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
	//			writer.append(CRLF).append(binaryFile.getName()).append(CRLF).flush();
	//
	//			writer.append("--" + boundary).append(CRLF);
	//			writer.append("Content-Disposition: form-data; name=\"dstFilePath\"").append(CRLF);
	//			writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
	//			writer.append(CRLF).append("out").append(CRLF).flush();
	//
	//			// Send text file.
	//			// writer.append("--" + boundary).append(CRLF);
	//			// writer.append("Content-Disposition: form-data; name=\"textFile\"; filename=\"" + textFile.getName() + "\"").append(CRLF);
	//			// writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
	//			// writer.append(CRLF).flush();
	//			// Files.copy(textFile.toPath(), output);
	//			// output.flush(); // Important before continuing with writer!
	//			// writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
	//
	//			// Send binary file.
	//			writer.append("--" + boundary).append(CRLF);
	//			// filename 파라메터는 꼭 있어야 함.
	//			// 없으면 receiver에서 filePart.getSubmittedFileName()이 null임
	//			writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"" + binaryFile.getName() + "\"").append(CRLF);
	//			writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
	//			writer.append("Content-Transfer-Encoding: binary").append(CRLF);
	//			writer.append(CRLF).flush();
	//			Files.copy(binaryFile.toPath(), output);
	//			output.flush(); // Important before continuing with writer!
	//			writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
	//
	//			// End of multipart/form-data.
	//			writer.append("--" + boundary + "--").append(CRLF).flush();
	//
	//			// Request is lazily fired whenever you need to obtain information about response.
	//			int responseCode = ((HttpURLConnection) connection).getResponseCode();
	//			StringBuffer sbResponse = new StringBuffer();
	//
	//			// get response
	//			if(200 == responseCode) {
	//				// success
	//				BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	//
	//				String line = null;
	//
	//				while ((line = br.readLine()) != null) {
	//					sbResponse.append(line);
	//				}
	//			}
	//			else {
	//				// fail
	//				sbResponse.append(((HttpURLConnection) connection).getResponseMessage());
	//			}
	//
	//			System.out.println(responseCode); // Should be 200
	//			System.out.println(sbResponse.toString());
	//		}
	//		catch(Exception e) {
	//			e.printStackTrace();
	//		}
	//		return null;
	//	}

	//	private static void setSendParam(PrintWriter writer, String key, String value) {
	//		String charset = "UTF-8";
	//		String CRLF = "\r\n"; // Line separator required by multipart/form-data.
	//		String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
	//
	//		writer.append("--" + boundary).append(CRLF);
	//		writer.append("Content-Disposition: form-data; name=\"" + key + "\"").append(CRLF);
	//		writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
	//		writer.append(CRLF).append(value).append(CRLF).flush();
	//	}
}
