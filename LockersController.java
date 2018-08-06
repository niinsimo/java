package ee.coop.delivery.controller;

import ee.coop.core.domain.Classifier;
import ee.coop.delivery.domain.Locker;
import ee.coop.delivery.domain.LockerLog;
import ee.coop.delivery.dto.LockerLogUpdate;
import ee.coop.delivery.service.CabinetService;
import ee.coop.delivery.dto.LockerDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/delivery/lockers/")
public class LockersController {
    private final CabinetService cabinetService;

    public LockersController(CabinetService cabinetService) {
        this.cabinetService = cabinetService;
    }

    @GetMapping("{id}")
    public List<Locker> getAllLockersById(@PathVariable("id") Long id) {
        return cabinetService.getLockers(id);

    }

    @GetMapping("inactive")
    public List<LockerDetails> getInactive() {
        return cabinetService.getInactiveLockers();
    }

    @RequestMapping(value = "{lockerId}/lockerLog", method = RequestMethod.GET)
    public List<LockerLog> getLogs(@PathVariable("lockerId") Long lockerId) {
        return cabinetService.getLockerLogById(lockerId);
    }

    @RequestMapping(value = "statuses", method = RequestMethod.GET)
    public List<Classifier> getStatuses() {
        return cabinetService.getLockerStatuses();
    }

    @PutMapping("{lockerId}")
    public LockerLog updateStatus(@PathVariable Long lockerId, @RequestBody LockerLogUpdate lockerLog) {
        return cabinetService.updateLockerStatus(lockerId, lockerLog);
    }
}
