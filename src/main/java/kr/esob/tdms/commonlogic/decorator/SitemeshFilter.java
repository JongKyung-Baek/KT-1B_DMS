package kr.esob.tdms.commonlogic.decorator;

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
		builder.addDecoratorPath("/general*", "/WEB-INF/decorator/decoratorList.jsp");
		builder.addDecoratorPath("/bbs*", "/WEB-INF/decorator/decoratorList.jsp");
		builder.addDecoratorPath("/configuration*", "/WEB-INF/decorator/decoratorList.jsp");
		builder.addDecoratorPath("/configuration*/*/*Popup", "/WEB-INF/decorator/decoratorEmpty.jsp");
		builder.addDecoratorPath("/*/*/*Popup", "/WEB-INF/decorator/decoratorEmpty.jsp");
		builder.addDecoratorPath("/*/*/*/*Popup", "/WEB-INF/decorator/decoratorEmpty.jsp");
		builder.addDecoratorPath("/*/*/*/*/*Popup", "/WEB-INF/decorator/decoratorEmpty.jsp");
		builder.addDecoratorPath("/general/system/role/", "/WEB-INF/decorator/decoratorSide.jsp"); // role
		builder.addDecoratorPath("/general/system/roleassign/", "/WEB-INF/decorator/decoratorSide.jsp"); // roleassign
		builder.addDecoratorPath("/general/system/menu/", "/WEB-INF/decorator/decoratorTree.jsp"); // menu
		builder.addDecoratorPath("/general/system/treemanage/", "/WEB-INF/decorator/decoratorTree.jsp"); // treemanage
		builder.addDecoratorPath("/general/distribution/approval/", "/WEB-INF/decorator/decoratorTab.jsp");
		builder.addDecoratorPath("/general/distribution/printApproval/", "/WEB-INF/decorator/decoratorTab.jsp");
		builder.addDecoratorPath("/general/distribution/printDestroyApproval/", "/WEB-INF/decorator/decoratorTab.jsp");
		builder.addDecoratorPath("/general/production/approval/", "/WEB-INF/decorator/decoratorProductTab.jsp");
		builder.addDecoratorPath("/general/production/disposalApproval/", "/WEB-INF/decorator/decoratorProductTab.jsp");
		builder.addDecoratorPath("/general/production/disposal/", "/WEB-INF/decorator/decoratorDisposalTab.jsp");
		builder.addDecoratorPath("/general/production/disposalStatus/", "/WEB-INF/decorator/decoratorDisposalTab.jsp");
		builder.addExcludedPath("/menu");
		builder.addExcludedPath("/login*");
		builder.addExcludedPath("/general/distribution/docPdfLinkRequest/selectItem2");
	}
}
