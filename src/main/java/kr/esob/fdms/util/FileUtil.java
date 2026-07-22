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
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

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

	static {
		disableSslVerification();
	}

	private static void disableSslVerification() {
		try
		{
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}
				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}
				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			}
			};

			// Install the all-trusting trust manager
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};

			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
	}

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
			e.printStackTrace();
			System.out.println(e.getMessage());
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
		
		srcUrl = Seed128Cipher.decrypt(srcUrl, Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING);
		srcUrl += "common/fileTransfer/sender";
		
		dstUrl = Seed128Cipher.decrypt(dstUrl, Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING);
		dstUrl += "common/fileTransfer/receiver";
		dstUrl = Seed128Cipher.encrypt(dstUrl, Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING);
		
		try {
			URLConnection connection = new URL(srcUrl).openConnection();
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);

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
			e.printStackTrace();
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


	public static JSONObject sendFile(String dstUrl, String srcFilePath, String dstFilePath, String dstFileNm) throws UnsupportedEncodingException {
		//		String url = "http://localhost:8080/common/upload/receiver";
		StringBuffer sbResponse = new StringBuffer();
		JSONObject result = new JSONObject();
		int responseCode = 0;
		String CRLF = "\r\n"; // Line separator required by multipart/form-data.
		String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
		//		File textFile = new File("/path/to/file.txt");

		dstUrl = Seed128Cipher.decrypt(dstUrl, Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING);
		srcFilePath = Seed128Cipher.decrypt(srcFilePath, Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING);
		
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
			writer.append(CRLF).append(Seed128Cipher.encrypt(binaryFile.getName(), Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING)).append(CRLF).flush();

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
			e.printStackTrace();
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

	public static String filePath(String fileName){
		int dotIndex = fileName.lastIndexOf(".");
		String fileNameWithoutExtension = dotIndex == -1 ? fileName : fileName.substring(0, dotIndex);
		return fileNameWithoutExtension;
	}
	public static String fileName(String fileName){
		int dotIndex = fileName.lastIndexOf(".");
		String fileNameWithoutExtension = dotIndex == -1 ? fileName : fileName.substring(0, dotIndex);
		return fileNameWithoutExtension+"_wh.vizw";
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
