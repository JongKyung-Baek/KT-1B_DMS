package kr.esob.tdms.commonlogic.branding;

/** Immutable, JSP-friendly brand model selected for one request. */
public final class TdmsBrandView {
    private final boolean alternate;
    private final String systemName;
    private final String companyName;
    private final String systemDescription;
    private final String logoLightPath;
    private final String logoDarkPath;
    private final String logoAlt;

    TdmsBrandView(boolean alternate,
                  String systemName,
                  String companyName,
                  String systemDescription,
                  String logoLightPath,
                  String logoDarkPath,
                  String logoAlt) {
        this.alternate = alternate;
        this.systemName = systemName;
        this.companyName = companyName;
        this.systemDescription = systemDescription;
        this.logoLightPath = logoLightPath;
        this.logoDarkPath = logoDarkPath;
        this.logoAlt = logoAlt;
    }

    public boolean isAlternate() {
        return alternate;
    }

    public boolean isWideLogo() {
        return alternate;
    }

    public String getSystemName() {
        return systemName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getSystemDescription() {
        return systemDescription;
    }

    public String getLogoLightPath() {
        return logoLightPath;
    }

    public String getLogoDarkPath() {
        return logoDarkPath;
    }

    public String getLogoAlt() {
        return logoAlt;
    }
}
