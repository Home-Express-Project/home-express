package com.homeexpress.home_express_api.dto.location;

public class ProvinceDto {

    private String code;
    private String name;

    public ProvinceDto() {
    }

    public ProvinceDto(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
