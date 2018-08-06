package ee.coop.delivery.service;

import ee.coop.ECoop;
import ee.coop.adapter.strongpoint.*;
import ee.coop.core.domain.Classifier;
import ee.coop.core.domain.UserCabinet;
import ee.coop.core.repository.ClassifierRepository;
import ee.coop.core.repository.UserCabinetRepository;
import ee.coop.delivery.domain.*;
import ee.coop.delivery.dto.CabinetListRow;
import ee.coop.delivery.dto.CabinetLogRow;
import ee.coop.delivery.dto.LockerDetails;
import ee.coop.delivery.dto.LockerLogUpdate;
import ee.coop.delivery.repository.*;
import ee.coop.erp.domain.Order;
import ee.coop.erp.domain.Store;
import ee.coop.erp.repository.OrderRepository;
import ee.coop.erp.repository.StoreRepository;
import ee.coop.utilites.DateTimeUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CabinetService {

    private final StrongPointService strongPointService;
    private final CabinetRepository cabinetRepository;
    private final LockerRepository lockerRepository;
    private final LockerLogRepository lockerLogRepository;
    private final TimeSlotConfigRepository timeSlotConfigRepository;
    private final UserCabinetRepository userCabinetRepository;
    private final DeliveryRepository deliveryRepository;
    private final RouteRepository routeRepository;
    private final StoreRepository storeRepository;
    private final ClassifierRepository classifierRepository;
    private final RouteVersionRepository routeVersionRepository;
    private final RouteVersionCabinetsRepository routeVersionCabinetsRepository;
    private final OrderRepository orderRepository;
    private final CabinetLogRepository cabinetLogRepository;

    private static final Logger log = LoggerFactory.getLogger(CabinetService.class);

    @Autowired
    public CabinetService(OrderRepository orderRepository, StrongPointService strongPointService, CabinetRepository cabinetRepository, LockerRepository lockerRepository, LockerLogRepository lockerLogRepository,
                          TimeSlotConfigRepository timeSlotConfigRepository, UserCabinetRepository userCabinetRepository, DeliveryRepository deliveryRepository,
                          RouteRepository routeRepository, StoreRepository storeRepository, ClassifierRepository classifierRepository, RouteVersionCabinetsRepository routeVersionCabinetsRepository,
                          RouteVersionRepository routeVersionRepository, CabinetLogRepository cabinetLogRepository) {

        this.strongPointService = strongPointService;
        this.cabinetRepository = cabinetRepository;
        this.lockerRepository = lockerRepository;
        this.lockerLogRepository = lockerLogRepository;
        this.timeSlotConfigRepository = timeSlotConfigRepository;
        this.userCabinetRepository = userCabinetRepository;
        this.deliveryRepository = deliveryRepository;
        this.routeRepository = routeRepository;
        this.storeRepository = storeRepository;
        this.classifierRepository = classifierRepository;
        this.routeVersionRepository = routeVersionRepository;
        this.routeVersionCabinetsRepository = routeVersionCabinetsRepository;
        this.orderRepository = orderRepository;
        this.cabinetLogRepository = cabinetLogRepository;
    }

    public Iterable<Cabinet> getCabinets() {
        return cabinetRepository.findAll();
    }

    public Iterable<Cabinet> getAvailableCabinets() {
        List<RouteVersion> routeVersions = routeVersionRepository.findAll();
        List<Cabinet> availableCabinets = new ArrayList<>();
        Long currentDate = DateTimeUtil.currentDate();
        for (RouteVersion routeVersion : routeVersions) {
            Long validFrom = DateTimeUtil.resetTime(routeVersion.getValidFrom());
            if ( validFrom <= currentDate && routeVersion.getValidUntil() == null || validFrom <= currentDate && DateTimeUtil.resetTime(routeVersion.getValidUntil()) >= currentDate ) {
                List<RouteVersionCabinets> activeRouteVersionCabinets = routeVersionCabinetsRepository.findAllByRouteVersionId(routeVersion.getId());
                for (RouteVersionCabinets routeVersionCabinets : activeRouteVersionCabinets) {
                    Cabinet cabinet = routeVersionCabinets.getCabinet();
                    availableCabinets.add(cabinet);
                }
            }
        }

        return availableCabinets;
    }

    public List<LockerDetails> getInactiveLockers() {
        List<Locker> lockers = lockerRepository.findAllByStatusNot(ECoop.LOCKER_STATE_ACTIVE);
        List<LockerDetails> lockerDetailsList = new ArrayList<>();
        for (Locker locker : lockers) {
            Cabinet cabinet = cabinetRepository.findOneById(locker.getCabinetId());
            Route route = routeRepository.findByCabinetId(locker.getCabinetId());
            Classifier classifier = classifierRepository.findFirstByKey(locker.getStatus());
            Integer count = lockerLogRepository.countAllByLockerId(locker.getId());
            if (route != null) {
                Store store = route.getStore();
                LockerDetails lockerDetails = new LockerDetails(cabinet.getName(), locker.getIndex(), route.getName(),
                        store.getName(), locker.getComment(), classifier.getValueEt(), locker.getId(), count);
                lockerDetailsList.add(lockerDetails);
            } else {
                LockerDetails lockerDetails = new LockerDetails();
                lockerDetails.setCabinetName(cabinet.getName());
                lockerDetails.setIndex(locker.getIndex());
                lockerDetails.setComment(locker.getComment());
                lockerDetails.setStatus(classifier.getValueEt());
                lockerDetails.setId(locker.getId());
                lockerDetails.setLogCount(count.longValue());
                lockerDetailsList.add(lockerDetails);
            }
        }
        return lockerDetailsList;
    }

    public List<CabinetListRow> getCabinetsForList() {
        List<Cabinet> cabinets = cabinetRepository.findAll();
        List<CabinetListRow> cabinetListRows = new ArrayList<>();
        for (Cabinet cabinet : cabinets) {
            cabinetListRows.add(toCabinetListRow(cabinet));
        }
        return cabinetListRows;
    }

    private CabinetListRow toCabinetListRow(Cabinet cabinet) {
        CabinetListRow newRow = new CabinetListRow();
        String lockerStatus = ECoop.LOCKER_STATE_ACTIVE;
        Long lockersWithErrorCount = 0L;
        List<Locker> lockers = lockerRepository.findAllByCabinetIdOrderByIndex(cabinet.getId());
        if (routeVersionCabinetsRepository.findFirstByCabinetId(cabinet.getId()) != null) {
            RouteVersionCabinets byCabinetId = routeVersionCabinetsRepository.findFirstByCabinetId(cabinet.getId());
            RouteVersion routeVersionByVersionId = routeVersionRepository.findOneById(byCabinetId.getRouteVersionId());
            Optional<Route> routeById = routeRepository.findById(routeVersionByVersionId.getRouteId());
            newRow.setRouteName(routeById.get().getName());
            newRow.setStoreName(routeById.get().getStore().getName());
        }
        for (Locker locker : lockers) {
            if (locker.getStatus().equals(ECoop.LOCKER_STATE_INACTIVE)) {
                lockerStatus = ECoop.LOCKER_STATE_INACTIVE;
                lockersWithErrorCount++;
            }
        }
        newRow.setLockerStatus(lockerStatus);
        newRow.setLockerErrorCount(lockersWithErrorCount);
        newRow.setAddress(cabinet.getAddress());
        newRow.setId(cabinet.getId());
        newRow.setSecondaryId(cabinet.getSecondaryId());
        newRow.setName(cabinet.getName());
        newRow.setStatus(classifierRepository.findFirstByKey(cabinet.getStatus()));
        return newRow;
    }

    public List<LockerLog> getLockerLogById(Long lockerId) {
        return lockerLogRepository.findAllByLockerId(lockerId);
    }

    public List<Locker> getLockers(Long id) {
        return lockerRepository.findAllByCabinetIdOrderByIndex(id);
    }

    public Cabinet getCabinetById(Long id) {
        return cabinetRepository.findOneById(id);
    }

    public void deleteCabinet(Long id) {
        cabinetRepository.deleteById(id);
    }

    public void updateLockers(SpTerminalEvent event) {
        String status;
        Cabinet cabinet = cabinetRepository.findOneByExternalId(event.getTerminalId());
        if (cabinet == null) {
            log.error("No cabinet found for " + event.getTerminalId());
            return;
        }
        logAndSetCabinetStatus(event);
        for (SpBox box : event.getBoxes()) {
            Locker locker = lockerRepository.findOneByCabinetIdAndIndex(cabinet.getId(), box.getBoxIndex());
            if (locker != null) {
                status = box.getIsDisabled() ? ECoop.LOCKER_STATE_INACTIVE : ECoop.LOCKER_STATE_ACTIVE;
                if (!status.equals(locker.getStatus())) {
                    locker.setStatus(status);
                    LockerLogUpdate update = new LockerLogUpdate();
                    update.setStatus(status);
                    LockerLog log = toLockerLog(locker, update);
                    lockerLogRepository.save(log);
                }
                locker.setThermoMode(box.getTempMode());
                lockerRepository.save(locker);
            } else {
                log.error(String.format("No locker for cabinet id: %d, locker index: %d", cabinet.getId(), box.getBoxIndex()));
            }

        }
    }

    public Cabinet logAndSetCabinetStatus(SpTerminalEvent event) {
        Cabinet cabinet = cabinetRepository.findOneByExternalId(event.getTerminalId());
        DateTime dateTime = new DateTime(event.getTimestamp() != null ? event.getTimestamp() : new Date());
        if (cabinet == null) {
            log.error("No cabinet found for " + event.getTerminalId());
            return null;
        }
        String newStatus = event.getIsDeleted() ? "CABINET_STATUS_INACTIVE" : "CABINET_STATUS_ACTIVE";
        if (!newStatus.equals(cabinet.getStatus())) {
            CabinetLog log = new CabinetLog();
            log.setCabinetId(cabinet.getId());
            log.setUserId(null);
            log.setStatus(newStatus);
            log.setExtCreatedAt(dateTime.getMillis());
            cabinetLogRepository.save(log);
            cabinet.setStatus(newStatus);
        }
        cabinetRepository.save(cabinet);
        return cabinet;
    }


    public List<Classifier> getLockerClassifiers() {
        List<Classifier> classifiers = classifierRepository.findAllByParentId(ECoop.LOCKER_CLASSIFIER_PARENT_ID);
        return classifiers;
    }

    public List<Classifier> getLockerStatuses() {
        List<Classifier> classifiers = getLockerClassifiers();
        List<Classifier> statuses = new ArrayList<>();
        for (Classifier classifier : classifiers) {
            if (!classifier.getKey().equals(ECoop.LOCKER_STATE_PACKAGE_NOT_LOADED)
                    && !classifier.getKey().equals(ECoop.LOCKER_STATE_PACKAGE_LOADED)
                    && !classifier.getKey().equals(ECoop.LOCKER_STATE_PACKAGE_COLLECTED)
                    && !classifier.getKey().equals(ECoop.LOCKER_STATE_EMPTY)
                    && !classifier.getKey().equals(ECoop.LOCKER_STATE_NOT_EMPTY)) {
                statuses.add(classifier);
            }
        }
        return statuses;
    }

    public LockerLog updateLockerStatus(Long lockerId, LockerLogUpdate lockerLog) {
        Locker locker = lockerRepository.findOneById(lockerId);
        LockerLog log = toLockerLog(locker, lockerLog);
        lockerLogRepository.save(log);
        locker.setComment(log.getComment());
        if (log.getStatus().equals(ECoop.LOCKER_STATE_ACTIVE)){
            locker.setStatus(ECoop.LOCKER_STATE_ACTIVE);
        }else{
            locker.setStatus(ECoop.LOCKER_STATE_INACTIVE);
        }
        locker.setStatusMaintenance(log.getStatusMaintenance() != null ? log.getStatusMaintenance() : locker.getStatusMaintenance());
        locker.setStatusTempMode(log.getStatusTempMode() != null ? log.getStatusTempMode() : locker.getStatusTempMode());
        lockerRepository.save(locker);
        return log;
    }

    private LockerLog toLockerLog(Locker locker, LockerLogUpdate logUpdate) {
        LockerLog log = new LockerLog();
        log.setComment(logUpdate.getComment() != null ? logUpdate.getComment() : null);
        setLockerLogStatus(log,logUpdate, locker);
        log.setCabinet(cabinetRepository.findOneById(locker.getCabinetId()));
        log.setLocker(locker);
        return log;
    }

    private LockerLog setLockerLogStatus(LockerLog log, LockerLogUpdate logUpdate, Locker locker) {
        if (logUpdate.getStatus().equals(ECoop.LOCKER_STATE_INACTIVE) ||
            logUpdate.getStatus().equals(ECoop.LOCKER_STATE_ACTIVE)) {
            log.setStatus(logUpdate.getStatus());
        }else {
            log.setStatus(locker.getStatus());
        }
        if (logUpdate.getStatus().equals("LOCKER_STATE_NEEDS_ATTENTION") ||
            logUpdate.getStatus().equals("LOCKER_STATE_NEEDS_REPAIRING") ||
            logUpdate.getStatus().equals("LOCKER_STATE_NEEDS_CLEANING") ||
            logUpdate.getStatus().equals("LOCKER_STATE_IN_CLEANING") ||
            logUpdate.getStatus().equals("LOCKER_STATE_IN_REPAIRING")) {
            log.setStatusMaintenance(logUpdate.getStatus());
        } else {
            log.setStatusTempMode(logUpdate.getStatus());
        }
        return log;
    }

    public Cabinet updateCabinet(Cabinet cabinet, Long id) {
        Cabinet cabinetById = getCabinetById(id);
        cabinetById.setName(cabinet.getName());
        cabinetById.setMaxOrders(cabinet.getMaxOrders());
        cabinetById.setFee(cabinet.getFee());
        cabinetById.setDescriptionEt(cabinet.getDescriptionEt());
        // coordinates and address are only set in StrongPoint server for now
//        cabinetById.setWgsLatitude(cabinet.getWgsLatitude());
//        cabinetById.setWgsLongitude(cabinet.getWgsLongitude());
//        cabinetById.setAddress(cabinet.getAddress());

        return cabinetRepository.save(cabinetById);
    }

  
}
