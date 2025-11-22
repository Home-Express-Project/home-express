package com.homeexpress.home_express_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Repository
public interface VnDistrictRepository extends JpaRepository<VnDistrictRepository.VnDistrict, String> {

    List<VnDistrict> findByProvinceCodeOrderByDistrictNameAsc(String provinceCode);

    java.util.Optional<VnDistrict> findFirstByDistrictNameContainingIgnoreCaseAndProvinceCode(String districtName, String provinceCode);

    @Entity
    @Table(name = "vn_districts")
    class VnDistrict {
        @Id
        private String districtCode;
        private String districtName;
        private String provinceCode;

        public String getDistrictCode() {
            return districtCode;
        }

        public void setDistrictCode(String districtCode) {
            this.districtCode = districtCode;
        }

        public String getDistrictName() {
            return districtName;
        }

        public void setDistrictName(String districtName) {
            this.districtName = districtName;
        }

        public String getProvinceCode() {
            return provinceCode;
        }

        public void setProvinceCode(String provinceCode) {
            this.provinceCode = provinceCode;
        }
    }
}
