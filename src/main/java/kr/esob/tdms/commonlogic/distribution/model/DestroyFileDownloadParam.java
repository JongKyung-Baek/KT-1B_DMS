package kr.esob.tdms.commonlogic.distribution.model;

import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

/**
 * Public download contract for destruction evidence.
 *
 * The physical path, object type and ACL object identifiers are deliberately
 * absent. They are resolved from the destruction request on the server.
 */
@Getter
@Setter
public class DestroyFileDownloadParam extends CommonParam {
    private String destroyRequestNo;
    private int destroyFileSeq;
}
