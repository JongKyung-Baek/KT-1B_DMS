package kr.esob.tdms.commonlogic.audit;

/**
 * Canonical menu metadata attached to a request while it is being audited.
 */
public final class AuditMenuContext {
    public static final String REQUEST_ATTRIBUTE = AuditMenuContext.class.getName() + ".context";

    private final String menuCd;
    private final String menuNm;
    private final String menuUrl;
    private final int menuLevel;

    public AuditMenuContext(String menuCd, String menuNm, String menuUrl, int menuLevel) {
        this.menuCd = menuCd;
        this.menuNm = menuNm;
        this.menuUrl = menuUrl;
        this.menuLevel = menuLevel;
    }

    public String getMenuCd() {
        return menuCd;
    }

    public String getMenuNm() {
        return menuNm;
    }

    public String getMenuUrl() {
        return menuUrl;
    }

    public int getMenuLevel() {
        return menuLevel;
    }
}
