package vn.fsaproject.carental.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataPaginationResponse {
    private Meta meta;
    private Object result;
}
