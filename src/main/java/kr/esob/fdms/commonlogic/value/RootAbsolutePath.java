package kr.esob.fdms.commonlogic.value;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class RootAbsolutePath {

	private String rootAbsolutePath;

	@Override
	public String toString() {
		return rootAbsolutePath;
	}
}
