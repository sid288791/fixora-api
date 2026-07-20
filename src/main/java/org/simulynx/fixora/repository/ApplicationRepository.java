package org.simulynx.fixora.repository;

import org.simulynx.fixora.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByAlias(String alias);
    List<Application> findByStatus(String status);

    @Query(value = "SELECT * FROM applications a " +
            "WHERE :adGroup = ANY (string_to_array(replace(a.ad_group_mapping, ' ', ''), ','))",
            nativeQuery = true)
    List<Application> findByAdGroup(@Param("adGroup") String adGroup);

    @Query("SELECT COUNT(a) FROM Application a")
    Long countAllApplications();

    @Query("SELECT COUNT(a) FROM Application a WHERE a.status = 'ACTIVE'")
    Long countActiveApplications();
}
