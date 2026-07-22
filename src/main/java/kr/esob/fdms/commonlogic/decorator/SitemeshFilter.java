package kr.esob.fdms.commonlogic.decorator;

import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.sitemesh.config.ConfigurableSiteMeshFilter;

/**
 * Sitemesh의 filter 설정
 * URL 패턴으로 decorator.jsp와 mapping
 * @author younjh
 *
 */
/**
 * decoratorMain		: full layout - for One Main
 * decoratorList		: full layout - for One Grid
 * decoratorEmpty		: empty layout - for Dialog Popup
 * decoratorHalf		: two-part layout - for half Grid / Grid
 * decoratorSide		: two-part layout - for left List / right Another one
 * decoratorTree		: two-part layout - for left Tree / right Another one
 * decoratorTab			: add menuTab(3depth) - for distribution(approval, printApproval, printDestroyApproval)
 * decoratorProductTab	: add menuTab(3depth) - for production(approval, disposalApproval)
 */
public class SitemeshFilter extends ConfigurableSiteMeshFilter{
	@Override
	protected void applyCustomConfiguration(SiteMeshFilterBuilder builder) {
		builder.addDecoratorPath("/main*", "/WEB-INF/decorator/decoratorMain.jsp");
		builder.addDecoratorPath("/inside*", "/WEB-INF/decorator/decoratorList.jsp");
		builder.addDecoratorPath("/outside*", "/WEB-INF/decorator/decoratorList.jsp");
		builder.addDecoratorPath("/bbs*", "/WEB-INF/decorator/decoratorList.jsp");
		builder.addDecoratorPath("/configuration*", "/WEB-INF/decorator/decoratorList.jsp");
		builder.addDecoratorPath("/configuration*/*/*Popup", "/WEB-INF/decorator/decoratorEmpty.jsp");
		builder.addDecoratorPath("/*/*/*Popup", "/WEB-INF/decorator/decoratorEmpty.jsp");
		builder.addDecoratorPath("/*/*/*/*Popup", "/WEB-INF/decorator/decoratorEmpty.jsp");
		builder.addDecoratorPath("/*/*/*/*/*Popup", "/WEB-INF/decorator/decoratorEmpty.jsp");
		builder.addDecoratorPath("/inside/organizationmanage/outsideuser/", "/WEB-INF/decorator/decoratorHalf.jsp"); // outsideuser
		builder.addDecoratorPath("/inside/system/role/", "/WEB-INF/decorator/decoratorSide.jsp"); // role
		builder.addDecoratorPath("/inside/system/roleassign/", "/WEB-INF/decorator/decoratorSide.jsp"); // roleassign
		builder.addDecoratorPath("/inside/system/menu/", "/WEB-INF/decorator/decoratorTree.jsp"); // menu
		builder.addDecoratorPath("/inside/system/treemanage/", "/WEB-INF/decorator/decoratorTree.jsp"); // treemanage
		builder.addDecoratorPath("/inside/distribution/approval/", "/WEB-INF/decorator/decoratorTab.jsp");
		builder.addDecoratorPath("/inside/distribution/printApproval/", "/WEB-INF/decorator/decoratorTab.jsp");
		builder.addDecoratorPath("/inside/distribution/printDestroyApproval/", "/WEB-INF/decorator/decoratorTab.jsp");
		builder.addDecoratorPath("/inside/production/approval/", "/WEB-INF/decorator/decoratorProductTab.jsp");
		builder.addDecoratorPath("/inside/production/disposalApproval/", "/WEB-INF/decorator/decoratorProductTab.jsp");
		builder.addDecoratorPath("/inside/production/disposal/", "/WEB-INF/decorator/decoratorDisposalTab.jsp");
		builder.addDecoratorPath("/inside/production/disposalStatus/", "/WEB-INF/decorator/decoratorDisposalTab.jsp");
		builder.addDecoratorPath("/inside/distribution/drawingRequest/view3DFile", "/WEB-INF/decorator/decoratorEmpty.jsp");
		// 23.06.12 (yskim) install page
		builder.addDecoratorPath("/downloadpage", "/WEB-INF/decorator/CollabviewInstallPage.jsp");
		builder.addExcludedPath("/menu");
		builder.addExcludedPath("/login*");
		builder.addExcludedPath("/inside/distribution/docPdfLinkRequest/selectItem2");
	}
}
