package kr.esob.fdms.commonlogic.abstractclass;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import kr.esob.fdms.controller.login.UserVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CommonParam {

    private Integer page;
    private Integer size;
    private String gridId;
    private String sessionLang;
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
        if (sortColumn != null) {
            try {
                sortColumn = sortColumn.replaceAll(",", "");
                sortColumn = sortColumn.replaceAll("\"", "");
                sortColumn = sortColumn.replaceAll(";", "");
                sortColumn = sortColumn.replaceAll("--", "");
                sortColumn = sortColumn.replaceAll("#", "");
                sortColumn = sortColumn.replaceAll("/*", "");
                sortColumn = sortColumn.replaceAll("=", "");
                sortColumn = sortColumn.replaceAll("\\)", "");
                sortColumn = sortColumn.replaceAll("\\(", "");
                sortColumn = sortColumn.replaceAll("select", "");
                sortColumn = sortColumn.replaceAll("where", "");
                sortColumn = sortColumn.replaceAll("sleep", "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            sortColumn = "";
        }

        if (sortColumn.contains(" ")) {
            try {
                sortColumn = sortColumn.replaceAll(" ", "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.sortColumn = sortColumn;
    }

    public void setOrder(String order) {
        if (order == null) {
            return;
        }
        if (order.equals("")) {
            return;
        }
        if (order.equalsIgnoreCase("asc") || order.equalsIgnoreCase("desc")) {
            this.order = order;
        }
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
