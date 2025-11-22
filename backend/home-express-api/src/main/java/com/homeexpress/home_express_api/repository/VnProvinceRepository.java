package com.homeexpress.home_express_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Repository
public interface VnProvinceRepository extends JpaRepository<VnProvinceRepository.VnProvince, String> {

    List<VnProvince> findAllByOrderByProvinceNameAsc();

    @Entity
    @Table(name = "vn_provinces")
    class VnProvince {
        @Id
        private String provinceCode;
        private String provinceName;

        public String getProvinceCode() {
            return provinceCode;
        }

        public void setProvinceCode(String provinceCode) {
            this.provinceCode = provinceCode;
        }

        public String getProvinceName() {
            return provinceName;
        }

        public void setProvinceName(String provinceName) {
            this.provinceName = provinceName;
        }
    }
}
