package com.homeexpress.home_express_api.dto.location;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapPlaceDTO {
    private String placeId;
    private String description; // Địa chỉ đầy đủ hiển thị cho user
    private String mainText;    // Tên địa điểm (VD: Keangnam Landmark 72)
    private String secondaryText; // Phần còn lại (VD: Phạm Hùng, Hà Nội)
    private Double latitude;
    private Double longitude;
    private String provinceCode;
    private String districtCode;
    private String wardCode;
}
