package kr.esob.fdms.commonlogic.filecache;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Slf4j
@Component
public class ExternalFileApiClient {
    private final RestTemplate restTemplate = new RestTemplate();

    public byte[] requestOriginalBySeq(String apiUrl, String seq) {
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("sequence", seq)
                .toUriString();
        log.info("[REST CALL][DOWNLOAD] {}", url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> res = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
        log.info("[REST RESP][DOWNLOAD] status={}, bodyLength={}, contentDisposition={}, filenameHeader={}, contentType={}",
                res.getStatusCodeValue(),
                res.getBody() == null ? 0 : res.getBody().length,
                res.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION),
                res.getHeaders().getFirst("filename"),
                res.getHeaders().getContentType());
        validateFileResponse("DOWNLOAD", url, res);
        return res.getBody();
    }

    public byte[] requestPdfBySeq(String apiUrl, String seq) {
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("sequence", seq)
                .toUriString();
        log.info("[REST CALL][VIEW] {}", url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> res = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
        log.info("[REST RESP][VIEW] status={}, bodyLength={}", res.getStatusCodeValue(), res.getBody() == null ? 0 : res.getBody().length);
        validateFileResponse("VIEW", url, res);
        return res.getBody();
    }

    private void validateFileResponse(String apiType, String url, ResponseEntity<byte[]> res) {
        if (res == null) {
            throw new IllegalStateException("REST " + apiType + " response is null.");
        }

        if (!res.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("REST " + apiType + " failed. status=" + res.getStatusCodeValue());
        }

        byte[] body = res.getBody();
        if (body == null || body.length == 0) {
            throw new IllegalStateException("REST " + apiType + " returned empty body.");
        }

        MediaType contentType = res.getHeaders().getContentType();
        if (isErrorContentType(contentType)) {
            throw new IllegalStateException("REST " + apiType + " returned non-file contentType="
                    + String.valueOf(contentType) + ", url=" + url);
        }

        if (isLikelyErrorPayload(body)) {
            throw new IllegalStateException("REST " + apiType + " returned error payload body, url=" + url);
        }
    }

    private boolean isErrorContentType(MediaType contentType) {
        if (contentType == null) {
            return false;
        }

        String type = contentType.getType() == null ? "" : contentType.getType().toLowerCase(Locale.ROOT);
        String subtype = contentType.getSubtype() == null ? "" : contentType.getSubtype().toLowerCase(Locale.ROOT);

        if ("text".equals(type)) {
            return true;
        }

        return subtype.contains("html")
                || subtype.contains("json")
                || subtype.contains("xml");
    }

    private boolean isLikelyErrorPayload(byte[] body) {
        int len = Math.min(body.length, 200);
        String prefix = new String(body, 0, len, StandardCharsets.UTF_8)
                .trim()
                .toLowerCase(Locale.ROOT);

        return prefix.startsWith("<!doctype")
                || prefix.startsWith("<html")
                || prefix.startsWith("{\"timestamp\"")
                || prefix.startsWith("{\"status\"")
                || prefix.contains("whitelabel error page")
                || prefix.contains("\"error\"")
                || prefix.contains("\"path\"");
    }
}
