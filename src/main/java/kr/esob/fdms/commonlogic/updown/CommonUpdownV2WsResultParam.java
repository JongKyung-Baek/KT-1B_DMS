package kr.esob.fdms.commonlogic.updown;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CommonUpdownV2WsResultParam {
    // SEQ(32) + RESULT_CODE(2) + OPTIONAL_DATA
    private String message;
}
