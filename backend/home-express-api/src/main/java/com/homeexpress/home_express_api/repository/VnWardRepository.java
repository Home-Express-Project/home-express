package com.homeexpress.home_express_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Repository
public interface VnWardRepository extends JpaRepository<VnWardRepository.VnWard, String> {

    List<VnWard> findByDistrictCodeOrderByWardNameAsc(String districtCode);

    java.util.Optional<VnWard> findFirstByWardNameContainingIgnoreCaseAndDistrictCode(String wardName, String districtCode);

    @Entity
    @Table(name = "vn_wards")
    class VnWard {
        @Id
        private String wardCode;
        private String wardName;
        private String districtCode;

        public String getWardCode() {
            return wardCode;
        }

        public void setWardCode(String wardCode) {
            this.wardCode = wardCode;
        }

        public String getWardName() {
            return wardName;
        }

        public void setWardName(String wardName) {
            this.wardName = wardName;
        }

        public String getDistrictCode() {
            return districtCode;
        }

        public void setDistrictCode(String districtCode) {
            this.districtCode = districtCode;
        }
    }
}
