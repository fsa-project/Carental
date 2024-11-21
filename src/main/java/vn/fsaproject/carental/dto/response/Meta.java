package vn.fsaproject.carental.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Meta {
    private int page;
    private int size;
    private int pages;
    private long total;
}
