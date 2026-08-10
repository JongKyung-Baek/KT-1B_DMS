package kr.esob.tdms.commonlogic.branding;

import java.util.regex.Pattern;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Runtime-only branding for the alternate public TLS port.
 *
 * <p>The primary brand deliberately remains the packaged KT-1B default. The
 * alternate names and assets are supplied by the REL environment so a shared
 * database never becomes a branding switch.</p>
 */
@Component
@ConfigurationProperties(prefix = "tdms.brand.alternate")
public class TdmsBrandProperties
        implements InitializingBean, ResourceLoaderAware {
    private static final Pattern SAFE_TEXT =
            Pattern.compile("^[^<>\\\"'&\\p{Cntrl}]{1,80}$");
    private static final Pattern SAFE_LOGO_PATH = Pattern.compile(
            "^/resources/images/brand/[A-Za-z0-9._-]+\\.png$");

    private boolean enabled;
    private int port = 443;
    private String systemName;
    private String companyNameKo;
    private String companyNameEn;
    private String logoLightPath;
    private String logoDarkPath;
    private String logoAlt;
    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void afterPropertiesSet() {
        if (!enabled) {
            return;
        }
        if (port < 1 || port > 65535) {
            throw new IllegalStateException(
                    "tdms.brand.alternate.port must be between 1 and 65535");
        }
        requireSafeText("system-name", systemName);
        requireSafeText("company-name-ko", companyNameKo);
        requireSafeText("company-name-en", companyNameEn);
        requireSafeText("logo-alt", logoAlt);
        requireSafeLogoPath("logo-light-path", logoLightPath);
        requireSafeLogoPath("logo-dark-path", logoDarkPath);
        requirePackagedLogo("logo-light-path", logoLightPath);
        requirePackagedLogo("logo-dark-path", logoDarkPath);
    }

    private void requireSafeText(String name, String value) {
        if (!StringUtils.hasText(value)
                || !value.equals(value.trim())
                || !SAFE_TEXT.matcher(value).matches()) {
            throw new IllegalStateException(
                    "tdms.brand.alternate." + name + " is invalid");
        }
    }

    private void requireSafeLogoPath(String name, String value) {
        if (!StringUtils.hasText(value)
                || !SAFE_LOGO_PATH.matcher(value).matches()) {
            throw new IllegalStateException(
                    "tdms.brand.alternate." + name + " is invalid");
        }
    }

    private void requirePackagedLogo(String name, String value) {
        String classpathLocation = "classpath:/static"
                + value.substring("/resources".length());
        if (resourceLoader == null
                || !resourceLoader.getResource(classpathLocation).exists()) {
            throw new IllegalStateException(
                    "tdms.brand.alternate." + name
                            + " does not reference a packaged logo");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getCompanyNameKo() {
        return companyNameKo;
    }

    public void setCompanyNameKo(String companyNameKo) {
        this.companyNameKo = companyNameKo;
    }

    public String getCompanyNameEn() {
        return companyNameEn;
    }

    public void setCompanyNameEn(String companyNameEn) {
        this.companyNameEn = companyNameEn;
    }

    public String getLogoLightPath() {
        return logoLightPath;
    }

    public void setLogoLightPath(String logoLightPath) {
        this.logoLightPath = logoLightPath;
    }

    public String getLogoDarkPath() {
        return logoDarkPath;
    }

    public void setLogoDarkPath(String logoDarkPath) {
        this.logoDarkPath = logoDarkPath;
    }

    public String getLogoAlt() {
        return logoAlt;
    }

    public void setLogoAlt(String logoAlt) {
        this.logoAlt = logoAlt;
    }
}
