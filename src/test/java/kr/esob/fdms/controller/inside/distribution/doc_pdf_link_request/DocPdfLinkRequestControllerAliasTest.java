package kr.esob.fdms.controller.inside.distribution.doc_pdf_link_request;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DocPdfLinkRequestControllerAliasTest {
    private final DocPdfLinkRequestController controller = new DocPdfLinkRequestController();

    @Test
    void keepsKoreanDocumentAlias() {
        assertTrue(controller.isDocumentType("문서"));
    }

    @Test
    void keepsKoreanDrawingAliases() {
        assertTrue(controller.isDrawingType("도면"));
        assertTrue(controller.isDrawingType("도면·공정서"));
    }

    @Test
    void keepsKoreanProductionAlias() {
        assertTrue(controller.isProductionType("생산기술자료"));
    }
}
