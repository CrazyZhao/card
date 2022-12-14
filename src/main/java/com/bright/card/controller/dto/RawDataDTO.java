package com.bright.card.controller.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * <b> 非敏感信息 </b>
 */
@Getter
@Setter
public class RawDataDTO {
    private String nickName;
    private String avatarUrl;
    private Integer gender;
    private String city;
    private String country;
    private String province;
}
