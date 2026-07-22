package kr.esob.fdms.commonlogic.excel;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author younjh
 *
 */
@Getter
@Setter
public class CreateExcelVO {
	private String result;
	private String url;		// 엑셀이 저장된 경로
}
