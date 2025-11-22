package com.homeexpress.home_express_api.dto.location;

public class WardDto {

    private String code;
    private String name;
    private String districtCode;

    public WardDto() {
    }

    public WardDto(String code, String name, String districtCode) {
        this.code = code;
        this.name = name;
        this.districtCode = districtCode;
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

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode;
    }
}
