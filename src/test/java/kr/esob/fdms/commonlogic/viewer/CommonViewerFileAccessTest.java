package kr.esob.fdms.commonlogic.viewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import kr.esob.fdms.commonlogic.securityacl.FileAccessRequest;
import kr.esob.fdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.fdms.controller.inside.distribution.doc_pdf_link_request.DocPdfLinkRequestDao;
import kr.esob.fdms.controller.login.UserVO;

class CommonViewerFileAccessTest {

	@Test
	void swSubFileAccessUsesParentSubTypeAndExactFileNumber() throws Exception {
		CommonViewerService service = serviceWithAuthenticatedActor();
		when(service.pdao.selectSubFileParent("SW", "SW-CHILD-1", "101"))
				.thenReturn(" SW-PARENT-1 ");

		CommonViewerParam param = viewerParam("SW-CHILD-1", "101");

		assertFalse(service.getDestroyStatus(param));

		verify(service.pdao).selectSubFileParent("SW", "SW-CHILD-1", "101");
		FileAccessRequest access = captureAccess(service.securityAclService);
		assertEquals(SecurityAclService.DETAIL, access.getActionCd());
		assertEquals("SW_SUB", access.getObjectType());
		assertEquals("SW-PARENT-1", access.getObjectId());
		assertEquals("101", access.getFileNo());
		assertEquals("REQ-1", access.getRequestNo());
	}

	@Test
	void swMainFileAccessKeepsOriginalTypeAndObjectId() throws Exception {
		CommonViewerService service = serviceWithAuthenticatedActor();
		when(service.pdao.selectSubFileParent("SW", "SW-MAIN-1", "1"))
				.thenReturn(null);

		CommonViewerParam param = viewerParam("SW-MAIN-1", "1");

		assertFalse(service.getDestroyStatus(param));

		verify(service.pdao).selectSubFileParent("SW", "SW-MAIN-1", "1");
		FileAccessRequest access = captureAccess(service.securityAclService);
		assertEquals("SW", access.getObjectType());
		assertEquals("SW-MAIN-1", access.getObjectId());
		assertEquals("1", access.getFileNo());
	}

	@Test
	void documentAliasIsCanonicalizedBeforeSubFileParentLookup() throws Exception {
		CommonViewerService service = serviceWithAuthenticatedActor();
		when(service.pdao.selectSubFileParent("DOCUMENT", "DOC-CHILD-1", "2"))
				.thenReturn("DOC-PARENT-1");

		CommonViewerParam param = viewerParam("DOC-CHILD-1", "2");
		param.setObjectType("DOC");

		assertFalse(service.getDestroyStatus(param));

		verify(service.pdao).selectSubFileParent("DOCUMENT", "DOC-CHILD-1", "2");
		FileAccessRequest access = captureAccess(service.securityAclService);
		assertEquals("DOCUMENT_SUB", access.getObjectType());
		assertEquals("DOC-PARENT-1", access.getObjectId());
		assertEquals("2", access.getFileNo());
	}

	private CommonViewerService serviceWithAuthenticatedActor() {
		CommonViewerService service = new CommonViewerService();
		service.dao = mock(CommonViewerDao.class);
		service.pdao = mock(DocPdfLinkRequestDao.class);
		service.securityAclService = mock(SecurityAclService.class);
		UserVO actor = new UserVO();
		actor.setUserCd("USER-1");
		when(service.securityAclService.requireCurrentUser()).thenReturn(actor);
		when(service.securityAclService.normalizeObjectType(anyString())).thenAnswer(invocation -> {
			String objectType = invocation.getArgument(0);
			return "DOC".equals(objectType) ? "DOCUMENT" : objectType;
		});
		return service;
	}

	private CommonViewerParam viewerParam(String objectId, String fileNo) {
		CommonViewerParam param = new CommonViewerParam();
		param.setObjectType("SW");
		param.setObjectId(objectId);
		param.setFileNo(fileNo);
		param.setRequestType("DISTRIBUTION");
		param.setRequestNo("REQ-1");
		return param;
	}

	private FileAccessRequest captureAccess(SecurityAclService aclService) {
		ArgumentCaptor<FileAccessRequest> captor =
				ArgumentCaptor.forClass(FileAccessRequest.class);
		verify(aclService).requireAccess(captor.capture());
		return captor.getValue();
	}
}
