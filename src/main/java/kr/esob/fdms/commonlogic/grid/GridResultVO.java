package kr.esob.fdms.commonlogic.grid;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GridResultVO {
	private int page;				// 현재 페이지
	private int size;				// 한 페이지당 보여줄 row 수
	private int totalPage;  		// 전체 페이지
	private int records;    		// 전체 row
	private Object contents;		// 그리드에 출력될 데이터
	private List<GridInfoVO> gridInfo;
	public int getTotalPage() {
		return (int) Math.ceil((double)records / size);
	}




}
