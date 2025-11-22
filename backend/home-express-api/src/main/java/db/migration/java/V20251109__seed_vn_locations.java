package db.migration.java;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Flyway Java migration that synchronises the Vietnamese administrative
 * divisions (provinces, districts, wards) from https://provinces.open-api.vn/.
 */
public class V20251109__seed_vn_locations extends BaseJavaMigration {

    private static final String PROVINCES_API = "https://provinces.open-api.vn/api/?depth=3";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public V20251109__seed_vn_locations() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            this.httpClient = HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create HTTP client", e);
        }

        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Override
    public void migrate(Context context) throws Exception {
        List<Province> provinces = fetchProvinces();
        if (provinces == null || provinces.isEmpty()) {
            throw new IllegalStateException("Vietnam provinces API returned no data");
        }

        Connection connection = context.getConnection();
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            clearExistingData(connection);
            insertAdministrativeDivisions(connection, provinces);
            connection.commit();
        } catch (Exception ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    private List<Province> fetchProvinces() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PROVINCES_API))
                .header("Accept", "application/json")
                .header("User-Agent", "home-express-flyway-migration/1.0")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );

        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Failed to fetch Vietnamese administrative divisions. HTTP status: " + response.statusCode());
        }

        return objectMapper.readValue(response.body(), new TypeReference<List<Province>>() {
        });
    }

    private void clearExistingData(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM vn_wards");
            statement.execute("DELETE FROM vn_districts");
            statement.execute("DELETE FROM vn_provinces");
        }
    }

    private void insertAdministrativeDivisions(Connection connection, List<Province> provinces) throws SQLException {
        String provinceSql = "INSERT INTO vn_provinces (province_code, province_name, codename, division_type, phone_code) "
                + "VALUES (?, ?, ?, ?, ?)";
        String districtSql = "INSERT INTO vn_districts (district_code, district_name, codename, division_type, short_codename, province_code) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        String wardSql = "INSERT INTO vn_wards (ward_code, ward_name, codename, division_type, short_codename, district_code) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement provinceStmt = connection.prepareStatement(provinceSql); PreparedStatement districtStmt = connection.prepareStatement(districtSql); PreparedStatement wardStmt = connection.prepareStatement(wardSql)) {

            for (Province province : provinces) {
                String provinceCode = formatProvinceCode(province.code);
                provinceStmt.setString(1, provinceCode);
                provinceStmt.setString(2, province.name);
                provinceStmt.setString(3, province.codename);
                provinceStmt.setString(4, province.divisionType);
                provinceStmt.setString(5, province.phoneCode == null ? null : province.phoneCode.toString());
                provinceStmt.addBatch();

                if (province.districts == null) {
                    continue;
                }

                for (District district : province.districts) {
                    String districtCode = formatDistrictCode(district.code);
                    districtStmt.setString(1, districtCode);
                    districtStmt.setString(2, district.name);
                    districtStmt.setString(3, district.codename);
                    districtStmt.setString(4, district.divisionType);
                    districtStmt.setString(5, district.shortCodename);
                    districtStmt.setString(6, provinceCode);
                    districtStmt.addBatch();

                    if (district.wards == null) {
                        continue;
                    }

                    for (Ward ward : district.wards) {
                        String wardCode = formatWardCode(ward.code);
                        wardStmt.setString(1, wardCode);
                        wardStmt.setString(2, ward.name);
                        wardStmt.setString(3, ward.codename);
                        wardStmt.setString(4, ward.divisionType);
                        wardStmt.setString(5, ward.shortCodename);
                        wardStmt.setString(6, districtCode);
                        wardStmt.addBatch();
                    }
                }
            }

            provinceStmt.executeBatch();
            districtStmt.executeBatch();
            wardStmt.executeBatch();
        }
    }

    private String formatProvinceCode(int code) {
        return formatCode(code, 2);
    }

    private String formatDistrictCode(int code) {
        return formatCode(code, 3);
    }

    private String formatWardCode(int code) {
        return formatCode(code, 5);
    }

    private String formatCode(int code, int minLength) {
        String raw = Integer.toString(code);
        if (raw.length() >= minLength) {
            return raw;
        }
        return String.format("%0" + minLength + "d", code);
    }

    private static final class Province {

        public int code;
        public String name;
        public String codename;
        public String divisionType;
        public Integer phoneCode;
        public List<District> districts;
    }

    private static final class District {

        public int code;
        public String name;
        public String codename;
        public String divisionType;
        public String shortCodename;
        public List<Ward> wards;
    }

    private static final class Ward {

        public int code;
        public String name;
        public String codename;
        public String divisionType;
        public String shortCodename;
    }
}
