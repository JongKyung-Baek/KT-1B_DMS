package kr.esob.tdms.commonlogic.abstractclass;

import java.util.Locale;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import kr.esob.tdms.controller.login.UserVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CommonParam {

    private static final int MAX_SORT_COLUMN_LENGTH = 128;
    private static final Pattern SAFE_SORT_COLUMN =
        Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z_][A-Za-z0-9_]*)?$");

    private Integer page;
    private Integer size;
    private String gridId;
    private String sessionLang;
    @JsonIgnore
    @ToString.Exclude
    private UserVO sessionUser;
    private String sortColumn;
    private String order;

    public CommonParam() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserVO) {
            sessionUser = (UserVO) authentication.getPrincipal();
        }
    }

    public void setSortColumn(String sortColumn) {
        if (sortColumn == null || sortColumn.trim().isEmpty()) {
            this.sortColumn = null;
            return;
        }

        String candidate = sortColumn.trim();
        if (candidate.length() > MAX_SORT_COLUMN_LENGTH
                || !SAFE_SORT_COLUMN.matcher(candidate).matches()) {
            throw new IllegalArgumentException("Invalid sort column");
        }
        this.sortColumn = candidate;
    }

    public void setOrder(String order) {
        if (order == null || order.trim().isEmpty()) {
            this.order = null;
            return;
        }

        String candidate = order.trim().toUpperCase(Locale.ROOT);
        if (!"ASC".equals(candidate) && !"DESC".equals(candidate)) {
            throw new IllegalArgumentException("Invalid sort order");
        }
        this.order = candidate;
    }

    public int getStart() {
        int p = null == page ? 0 : page;
        int s = null == size ? 0 : size;
        return ((p - 1) * s) + 1;
    }

    public int getEnd() {
        int p = null == page ? 0 : page;
        int s = null == size ? 0 : size;
        return p * s;
    }
}
