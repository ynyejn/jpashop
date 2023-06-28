package jpabook.jpashop.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ResultResDataDto {

    private Boolean result;
    private String message;
    private Map<String, Object> data;

    private List<Map<String,Object>> listData;
    private Long idx;
    private String stringData;
    public static ResultResDataDto fromBoolean(Boolean result, String message, Map<String, Object> data) {
        return ResultResDataDto.builder()
                .result(result)
                .message(message)
                .data(data)
                .build();
    }

    public static ResultResDataDto fromResData(Boolean result, String message, List<Map<String,Object>> data) {
        return ResultResDataDto.builder()
                .result(result)
                .message(message)
                .listData(data)
                .build();
    }

    public static ResultResDataDto fromResMsg(Boolean result, String message) {
        return ResultResDataDto.builder()
                .result(result)
                .message(message)
                .build();
    }
    public static ResultResDataDto fromIdxData(Boolean result,String message, Long idx) {
        return ResultResDataDto.builder()
                .result(result)
                .message(message)
                .idx(idx)
                .build();
    }

    public static ResultResDataDto fromStringData(Boolean result,String message, String stringData) {
        return ResultResDataDto.builder()
                .result(result)
                .message(message)
                .stringData(stringData)
                .build();
    }


}
