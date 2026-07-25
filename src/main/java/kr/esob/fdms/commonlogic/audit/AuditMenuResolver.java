package kr.esob.fdms.commonlogic.audit;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.LongSupplier;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import kr.esob.fdms.commonlogic.menu.MenuDao;
import kr.esob.fdms.commonlogic.menu.MenuVO;
import lombok.extern.slf4j.Slf4j;

/**
 * Resolves a request URI to one stable menu identity.
 *
 * <p>The raw menu table contains overlapping patterns. The most specific
 * static URL wins and menu depth breaks ties, so a child menu is selected
 * when a root and child intentionally share a URL.</p>
 */
@Slf4j
@Component
public class AuditMenuResolver {
    static final Duration DEFAULT_CACHE_TTL = Duration.ofSeconds(60);

    private final MenuDao menuDao;
    private final long cacheTtlNanos;
    private final LongSupplier ticker;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private volatile MenuSnapshot snapshot = MenuSnapshot.uninitialized();

    @Autowired
    public AuditMenuResolver(MenuDao menuDao) {
        this(menuDao, DEFAULT_CACHE_TTL, System::nanoTime);
    }

    AuditMenuResolver(MenuDao menuDao, Duration cacheTtl, LongSupplier ticker) {
        this.menuDao = menuDao;
        this.cacheTtlNanos = cacheTtl.toNanos();
        this.ticker = ticker;
    }

    public AuditMenuContext resolve(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return resolve(toApplicationPath(request));
    }

    public AuditMenuContext resolve(String requestPath) {
        String path = normalizePath(requestPath);
        if (path == null) {
            return null;
        }

        MenuDefinition best = null;
        for (MenuDefinition candidate : currentSnapshot().menus) {
            if (!matches(candidate.menuUrl, path)) {
                continue;
            }
            if (isMoreSpecific(candidate, best)) {
                best = candidate;
            }
        }
        return best == null ? null : best.toContext();
    }

    private MenuSnapshot currentSnapshot() {
        long now = ticker.getAsLong();
        MenuSnapshot current = snapshot;
        if (current.initialized && now < current.expiresAtNanos) {
            return current;
        }

        synchronized (this) {
            current = snapshot;
            now = ticker.getAsLong();
            if (current.initialized && now < current.expiresAtNanos) {
                return current;
            }

            try {
                List<MenuVO> source = menuDao.getMenuList();
                snapshot = buildSnapshot(source, expiresAt(now));
            } catch (RuntimeException e) {
                log.warn("Audit menu cache refresh failed; using the last safe snapshot. cause={}",
                        e.getClass().getSimpleName());
                if (current.initialized) {
                    snapshot = current.withExpiry(expiresAt(now));
                } else {
                    snapshot = new MenuSnapshot(Collections.emptyList(), expiresAt(now), true);
                }
            }
            return snapshot;
        }
    }

    private MenuSnapshot buildSnapshot(List<MenuVO> source, long expiresAtNanos) {
        if (source == null || source.isEmpty()) {
            return new MenuSnapshot(Collections.emptyList(), expiresAtNanos, true);
        }

        Map<String, MenuVO> byCode = new HashMap<>();
        for (MenuVO menu : source) {
            if (menu != null && !isBlank(menu.getMenuCd())) {
                byCode.put(menu.getMenuCd(), menu);
            }
        }

        List<MenuDefinition> activeMenus = new ArrayList<>();
        for (MenuVO menu : source) {
            if (!isActive(menu) || normalizePattern(menu.getMenuUrl()) == null) {
                continue;
            }
            String menuUrl = normalizePattern(menu.getMenuUrl());
            activeMenus.add(new MenuDefinition(
                    trimToNull(menu.getMenuCd()),
                    buildMenuPath(menu, byCode),
                    menuUrl,
                    menu.getMenuLevel(),
                    staticLength(menuUrl)));
        }
        return new MenuSnapshot(Collections.unmodifiableList(activeMenus), expiresAtNanos, true);
    }

    private String buildMenuPath(MenuVO menu, Map<String, MenuVO> byCode) {
        Deque<String> names = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        MenuVO current = menu;
        while (current != null) {
            String currentCd = trimToNull(current.getMenuCd());
            if (currentCd != null && !visited.add(currentCd)) {
                break;
            }
            String name = trimToNull(current.getMenuNm());
            if (name != null) {
                names.addFirst(name);
            }
            String parentCd = trimToNull(current.getParentMenuCd());
            current = parentCd == null ? null : byCode.get(parentCd);
        }
        return names.isEmpty() ? trimToNull(menu.getMenuNm()) : String.join(" > ", names);
    }

    private boolean matches(String pattern, String path) {
        if (pathMatcher.match(pattern, path)) {
            return true;
        }

        // A menu URL ending in "/" represents its page plus page-owned APIs.
        // This is needed for exact menu rows such as securityaccess/.
        return !containsWildcard(pattern)
                && pattern.endsWith("/")
                && (path.equals(pattern.substring(0, pattern.length() - 1))
                    || path.startsWith(pattern));
    }

    private boolean isMoreSpecific(MenuDefinition candidate, MenuDefinition current) {
        if (current == null) {
            return true;
        }
        if (candidate.staticLength != current.staticLength) {
            return candidate.staticLength > current.staticLength;
        }
        if (candidate.menuLevel != current.menuLevel) {
            return candidate.menuLevel > current.menuLevel;
        }
        String candidateCd = candidate.menuCd == null ? "" : candidate.menuCd;
        String currentCd = current.menuCd == null ? "" : current.menuCd;
        return candidateCd.compareTo(currentCd) < 0;
    }

    private String toApplicationPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (!isBlank(contextPath) && path != null && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        return path;
    }

    private String normalizePath(String value) {
        String path = trimToNull(value);
        if (path == null) {
            return null;
        }
        int queryStart = path.indexOf('?');
        if (queryStart >= 0) {
            path = path.substring(0, queryStart);
        }
        if (path.isEmpty()) {
            return "/";
        }
        return path.charAt(0) == '/' ? path : "/" + path;
    }

    private String normalizePattern(String value) {
        String pattern = normalizePath(value);
        if (pattern == null) {
            return null;
        }
        return pattern;
    }

    private int staticLength(String pattern) {
        int length = 0;
        for (int index = 0; index < pattern.length(); index++) {
            char value = pattern.charAt(index);
            if (value != '*' && value != '?') {
                length++;
            }
        }
        return length;
    }

    private boolean containsWildcard(String pattern) {
        return pattern.indexOf('*') >= 0 || pattern.indexOf('?') >= 0;
    }

    private boolean isActive(MenuVO menu) {
        return menu != null
                && "Y".equalsIgnoreCase(trimToNull(menu.getUseYn()))
                && "N".equalsIgnoreCase(trimToNull(menu.getDelYn()));
    }

    private long expiresAt(long now) {
        return now + cacheTtlNanos;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return trimToNull(value) == null;
    }

    private static final class MenuDefinition {
        private final String menuCd;
        private final String menuNm;
        private final String menuUrl;
        private final int menuLevel;
        private final int staticLength;

        private MenuDefinition(String menuCd, String menuNm, String menuUrl,
                               int menuLevel, int staticLength) {
            this.menuCd = menuCd;
            this.menuNm = menuNm;
            this.menuUrl = menuUrl;
            this.menuLevel = menuLevel;
            this.staticLength = staticLength;
        }

        private AuditMenuContext toContext() {
            return new AuditMenuContext(menuCd, menuNm, menuUrl, menuLevel);
        }
    }

    private static final class MenuSnapshot {
        private final List<MenuDefinition> menus;
        private final long expiresAtNanos;
        private final boolean initialized;

        private MenuSnapshot(List<MenuDefinition> menus, long expiresAtNanos, boolean initialized) {
            this.menus = menus;
            this.expiresAtNanos = expiresAtNanos;
            this.initialized = initialized;
        }

        private static MenuSnapshot uninitialized() {
            return new MenuSnapshot(Collections.emptyList(), Long.MIN_VALUE, false);
        }

        private MenuSnapshot withExpiry(long newExpiresAtNanos) {
            return new MenuSnapshot(menus, newExpiresAtNanos, true);
        }
    }
}
