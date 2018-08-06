package ee.coop.delivery.repository;

import ee.coop.delivery.domain.LockerLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LockerLogRepository extends JpaRepository<LockerLog, Long> {
    List<LockerLog> findAllByLockerId(Long lockerId);
    List<LockerLog> findAllByCabinetId(Long cabinetId);

    Integer countAllByLockerId(Long lockerId);
}


