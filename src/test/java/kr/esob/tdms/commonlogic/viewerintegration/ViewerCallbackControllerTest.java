package kr.esob.tdms.commonlogic.viewerintegration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;

class ViewerCallbackControllerTest {
    @Test
    void passesExactRawBodyToSignatureVerification() {
        ViewerIntegrationService service = org.mockito.Mockito.mock(ViewerIntegrationService.class);
        ViewerCallbackController controller = new ViewerCallbackController(service);
        byte[] body = "{\"eventType\":\"VIEW_OPENED\"}".getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent(body);

        controller.receive(request, "collabview", "1", "nonce", "hash", "signature");

        ArgumentCaptor<byte[]> captured = ArgumentCaptor.forClass(byte[].class);
        verify(service).acceptCallback(
                captured.capture(), anyString(), anyString(), anyString(), anyString(), anyString());
        assertArrayEquals(body, captured.getValue());
    }

    @Test
    void rejectsChunkedBodyBeyondSixteenKibBeforeService() {
        ViewerIntegrationService service = org.mockito.Mockito.mock(ViewerIntegrationService.class);
        ViewerCallbackController controller = new ViewerCallbackController(service);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override public int getContentLength() { return -1; }
            @Override public long getContentLengthLong() { return -1L; }
        };
        request.setContent(new byte[(16 * 1024) + 1]);

        assertThrows(ViewerCallbackPayloadTooLargeException.class, () -> controller.receive(
                request, "collabview", "1", "nonce", "hash", "signature"));
        verify(service, never()).acceptCallback(
                org.mockito.ArgumentMatchers.any(), anyString(), anyString(),
                anyString(), anyString(), anyString());
    }
}
