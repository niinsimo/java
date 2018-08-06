package ee.coop.delivery.service;

import ee.coop.adapter.strongpoint.SpBox;
import ee.coop.adapter.strongpoint.SpTerminalEvent;
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
import ee.coop.erp.domain.Store;
import ee.coop.erp.repository.OrderRepository;
import ee.coop.erp.repository.StoreRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@RunWith(MockitoJUnitRunner.Silent.class)
public class CabinetServiceTest {

    @Mock
    private StrongPointService strongPointService;
    @Mock
    private CabinetRepository cabinetRepository;
    @Mock
    private LockerRepository lockerRepository;
    @Mock
    private LockerLogRepository lockerLogRepository;
    @Mock
    private TimeSlotConfigRepository timeSlotConfigRepository;
    @Mock
    private UserCabinetRepository userCabinetRepository;
    @Mock
    private DeliveryRepository deliveryRepository;
    @Mock
    private RouteRepository routeRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private ClassifierRepository classifierRepository;
    @Mock
    private RouteVersionRepository routeVersionRepository;
    @Mock
    private RouteVersionCabinetsRepository routeVersionCabinetsRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CabinetLogRepository cabinetLogRepository;

    private CabinetService cabinetService;

    @Before
    public void setUp() throws Exception {
        cabinetService = new CabinetService(orderRepository, strongPointService, cabinetRepository, lockerRepository,
                lockerLogRepository, timeSlotConfigRepository, userCabinetRepository, deliveryRepository,
                routeRepository, storeRepository, classifierRepository, routeVersionCabinetsRepository,
                routeVersionRepository, cabinetLogRepository);
    }

    private Cabinet createCabinet(Long id, String name, String status, Long deletedAt) {
        Cabinet cabinet = new Cabinet();
        cabinet.setId(id);
        cabinet.setName(name);
        cabinet.setStatus(status);
        cabinet.setDeletedAt(deletedAt);

        return cabinet;
    }

    private Locker createLocker(Long id, Long cabinetId, Long index, String status, String comment, Long deletedAt) {
        Locker locker = new Locker();
        locker.setId(id);
        locker.setCabinetId(cabinetId);
        locker.setIndex(index);
        locker.setStatus(status);
        locker.setComment(comment);
        locker.setDeletedAt(deletedAt);

        return locker;
    }

    private LockerLog createLockerLog(Long id, Cabinet cabinet, Locker locker, String comment, String status, Long deletedAt) {
        LockerLog lockerLog = new LockerLog();
        lockerLog.setId(id);
        lockerLog.setCabinet(cabinet);
        lockerLog.setLocker(locker);
        lockerLog.setComment(comment);
        lockerLog.setStatus(status);
        lockerLog.setDeletedAt(deletedAt);

        return lockerLog;
    }

    private CabinetListRow createCabinetListRow(Long id, String name, String lockerStatus, Long lockerErrorCount, String storeName, String routeName) {
        CabinetListRow cabinetListRow = new CabinetListRow();
        cabinetListRow.setId(id);
        cabinetListRow.setName(name);
        cabinetListRow.setLockerStatus(lockerStatus);
        cabinetListRow.setLockerErrorCount(lockerErrorCount);
        cabinetListRow.setStoreName(storeName);
        cabinetListRow.setRouteName(routeName);

        return cabinetListRow;
    }

    private LockerDetails createLockerDetails(String cabinetName, Long lockerId, Long lockerIndex, String routeName,
                                              String storeName, String comment, String status, Long logCount) {
        LockerDetails lockerDetails = new LockerDetails();
        lockerDetails.setCabinetName(cabinetName);
        lockerDetails.setId(lockerId);
        lockerDetails.setIndex(lockerIndex);
        lockerDetails.setRouteName(routeName);
        lockerDetails.setStoreName(storeName);
        lockerDetails.setComment(comment);
        lockerDetails.setStatus(status);
        lockerDetails.setLogCount(logCount);

        return lockerDetails;
    }

    private RouteVersion createRouteVersion(Long id, Long routeId, String status, Long validFrom, Long validUntil, String name) {
        RouteVersion routeVersion = new RouteVersion();
        routeVersion.setId(id);
        routeVersion.setRouteId(routeId);
        routeVersion.setStatus(status);
        routeVersion.setValidFrom(validFrom);
        routeVersion.setValidUntil(validUntil);
        routeVersion.setName(name);

        return routeVersion;
    }

    private TimeSlotConfig createTimeSlot(Long id, Long storeId, Cabinet cabinet, Long startTime, Long endTime, BigDecimal deliveryFee, Long fixedDate, Long pickingStartsAt, Long pickingEndsAt, Long routeVersionId) {
        TimeSlotConfig timeSlotConfig = new TimeSlotConfig();
        timeSlotConfig.setId(id);
        timeSlotConfig.setStoreId(storeId);
        timeSlotConfig.setCabinet(cabinet);
        timeSlotConfig.setStartTime(startTime);
        timeSlotConfig.setEndTime(endTime);
        timeSlotConfig.setDeliveryFee(deliveryFee);
        timeSlotConfig.setFixedDate(fixedDate);
        timeSlotConfig.setPickingStartsAt(pickingStartsAt);
        timeSlotConfig.setPickingEndsAt(pickingEndsAt);
        timeSlotConfig.setRouteVersionId(routeVersionId);

        return timeSlotConfig;
    }

    private TimeSlotsPerDay createTimeSlotPerDay(Long date, List<TimeSlotInstance> timeslots) {
        TimeSlotsPerDay timeSlotsPerDay = new TimeSlotsPerDay();
        timeSlotsPerDay.setDate(date);
        timeSlotsPerDay.setTimeSlots(timeslots);

        return timeSlotsPerDay;
    }

    private TimeSlotInstance createTimeSlotInstance(Long timeSlotConfigId, BigDecimal deliveryFee, Long startTime, Long endTime, String status) {
        TimeSlotInstance timeSlotInstance = new TimeSlotInstance();
        timeSlotInstance.setTimeSlotConfigId(timeSlotConfigId);
        timeSlotInstance.setDeliveryFee(deliveryFee);
        timeSlotInstance.setStartTime(startTime);
        timeSlotInstance.setEndTime(endTime);
        timeSlotInstance.setStatus(status);

        return timeSlotInstance;
    }

    private Classifier createClassifier(Long id, Long parentId, String key) {
        Classifier classifier = new Classifier();
        classifier.setId(id);
        classifier.setParentId(parentId);
        classifier.setKey(key);

        return classifier;
    }

    private UserCabinet createUserCabinet(Long id, Long userId, Long cabinetId, Long deletedAt) {
        UserCabinet userCabinet = new UserCabinet();
        userCabinet.setId(id);
        userCabinet.setUserId(userId);
        userCabinet.setCabinetId(cabinetId);
        userCabinet.setDeletedAt(deletedAt);

        return userCabinet;
    }

    private CabinetLog createCabinetLog(Long id, Long cabinetId, Long userId, String status) {
        CabinetLog cabinetLog = new CabinetLog();
        cabinetLog.setId(id);
        cabinetLog.setCabinetId(cabinetId);
        cabinetLog.setUserId(userId);
        cabinetLog.setStatus(status);

        return cabinetLog;
    }

    private RouteVersionCabinets createRouteVersionCabinet(Long id, Long routeVersionId, Cabinet cabinet) {
        RouteVersionCabinets routeVersionCabinet = new RouteVersionCabinets();
        routeVersionCabinet.setId(id);
        routeVersionCabinet.setRouteVersionId(routeVersionId);
        routeVersionCabinet.setCabinet(cabinet);

        return routeVersionCabinet;
    }

    private SpTerminalEvent createTerminalEvent(String terminalId, String timestamp, List<SpBox> boxes, boolean isDeleted) {
        SpTerminalEvent spTerminalEvent = new SpTerminalEvent();
        spTerminalEvent.setTerminalId(terminalId);
        spTerminalEvent.setTimestamp(timestamp);
        spTerminalEvent.setBoxes(boxes);
        spTerminalEvent.setIsDeleted(isDeleted);

        return spTerminalEvent;
    }

    private CabinetLogRow createCabinetLogRow(Long timestamp, String status, Long index) {
        CabinetLogRow cabinetLogRow = new CabinetLogRow();
        cabinetLogRow.setTimeStamp(timestamp);
        cabinetLogRow.setStatus(status);
        cabinetLogRow.setIndex(index);

        return cabinetLogRow;
    }

    @Test
    public void testGetAvailableCabinetsIterable() {
        Cabinet cabinet1 = createCabinet(1L, "Rimi", "LOCKER_STATE_ACTIVE", null);
        Cabinet cabinet2 = createCabinet(2L, "Rimi", "LOCKER_STATE_ACTIVE", null);

        RouteVersion routeVersion1 = createRouteVersion(1L, 1L, "OK", 1533081600000L, null, "Route Version");
        RouteVersion routeVersion2 = createRouteVersion(2L, 2L, "OK", 1533081600000L, 1596844800000L, "Route Version");

        RouteVersionCabinets routeVersionCabinet1 = createRouteVersionCabinet(1L, 1L, cabinet1);
        RouteVersionCabinets routeVersionCabinet2 = createRouteVersionCabinet(2L, 1L, cabinet2);

        List<RouteVersion> routeVersions = new ArrayList<>();
        routeVersions.add(routeVersion1);
        routeVersions.add(routeVersion2);

        List<Cabinet> availableCabinets = new ArrayList<>();
        availableCabinets.add(cabinet1);
        availableCabinets.add(cabinet2);

        List<RouteVersionCabinets> routeVersionCabinets = new ArrayList<>();
        routeVersionCabinets.add(routeVersionCabinet1);
        routeVersionCabinets.add(routeVersionCabinet2);

        when(routeVersionRepository.findAll())
                .thenReturn(routeVersions);
        when(routeVersionCabinetsRepository.findAllByRouteVersionId(1L))
                .thenReturn(routeVersionCabinets);

        Iterable<Cabinet> result = cabinetService.getAvailableCabinets();
        assertEquals(availableCabinets, result);
        assertEquals(availableCabinets.hashCode(), result.hashCode());
    }

    @Test
    public void testGetAvailableCabinetsIterableValidUntilPassed() {
        Cabinet cabinet1 = createCabinet(1L, "Rimi", "LOCKER_STATE_ACTIVE", null);
        Cabinet cabinet2 = createCabinet(2L, "Rimi", "LOCKER_STATE_ACTIVE", null);

        RouteVersion routeVersion1 = createRouteVersion(1L, 1L, "OK", 1533081600000L, 1533081600001L, "Route Version");
        RouteVersion routeVersion2 = createRouteVersion(2L, 2L, "OK", 1533081600000L, 1533081600001L, "Route Version");

        RouteVersionCabinets routeVersionCabinet1 = createRouteVersionCabinet(1L, 1L, cabinet1);
        RouteVersionCabinets routeVersionCabinet2 = createRouteVersionCabinet(2L, 1L, cabinet2);

        List<RouteVersion> routeVersions = new ArrayList<>();
        routeVersions.add(routeVersion1);
        routeVersions.add(routeVersion2);

        List<Cabinet> availableCabinets = new ArrayList<>();

        List<RouteVersionCabinets> routeVersionCabinets = new ArrayList<>();
        routeVersionCabinets.add(routeVersionCabinet1);
        routeVersionCabinets.add(routeVersionCabinet2);

        when(routeVersionRepository.findAll())
                .thenReturn(routeVersions);
        when(routeVersionCabinetsRepository.findAllByRouteVersionId(1L))
                .thenReturn(routeVersionCabinets);

        Iterable<Cabinet> result = cabinetService.getAvailableCabinets();
        assertEquals(availableCabinets, result);
        assertEquals(availableCabinets.hashCode(), result.hashCode());
    }

    @Test(expected = NullPointerException.class)
    public void testGetAvailableCabinetsIterableValidTimesNull() {
        Cabinet cabinet1 = createCabinet(1L, "Rimi", "LOCKER_STATE_ACTIVE", null);
        Cabinet cabinet2 = createCabinet(2L, "Rimi", "LOCKER_STATE_ACTIVE", null);

        RouteVersion routeVersion1 = createRouteVersion(1L, 1L, "OK", null, null, "Route Version");
        RouteVersion routeVersion2 = createRouteVersion(2L, 2L, "OK", null, null, "Route Version");

        RouteVersionCabinets routeVersionCabinet1 = createRouteVersionCabinet(1L, 1L, cabinet1);
        RouteVersionCabinets routeVersionCabinet2 = createRouteVersionCabinet(2L, 1L, cabinet2);

        List<RouteVersion> routeVersions = new ArrayList<>();
        routeVersions.add(routeVersion1);
        routeVersions.add(routeVersion2);

        List<Cabinet> availableCabinets = new ArrayList<>();

        List<RouteVersionCabinets> routeVersionCabinets = new ArrayList<>();
        routeVersionCabinets.add(routeVersionCabinet1);
        routeVersionCabinets.add(routeVersionCabinet2);

        when(routeVersionRepository.findAll())
                .thenReturn(routeVersions);
        when(routeVersionCabinetsRepository.findAllByRouteVersionId(1L))
                .thenReturn(routeVersionCabinets);
        when(cabinetService.getAvailableCabinets())
                .thenThrow(NullPointerException.class);
        Iterable<Cabinet> result = cabinetService.getAvailableCabinets();
        assertEquals(availableCabinets, result);
    }

    @Test
    public void testGetInactiveLockers() {
        Locker workingLocker1 = createLocker(1L, 1L, 1L, "LOCKER_OK", "ok", null);
        Locker notWorkingLocker1 = createLocker(3L, 3L, 3L, "LOCKER_NEEDS_ATTENTION", "ok", null);
        Locker notWorkingLocker2 = createLocker(4L, 4L, 4L, "LOCKER_NEEDS_REPAIRING", "ok", null);
        Locker notWorkingLocker3 = createLocker(5L, 5L, 5L, "LOCKER_NEEDS_CLEANING", "ok", null);
        Locker notWorkingLocker4 = createLocker(6L, 6L, 6L, "LOCKER_IN_REPAIRING", "ok", null);
        Locker notWorkingLocker5 = createLocker(7L, 7L, 7L, "LOCKER_IN_CLEANING", "ok", null);
        Locker notWorkingLocker6 = createLocker(8L, 8L, 8L, "LOCKER_TURNED_OFF", "ok", null);
        Locker notWorkingLocker7 = createLocker(9L, 9L, 9L, "LOCKER_WRONG_TEMPERATURE", "ok", null);
        Cabinet cabinet = createCabinet(1L, "Rimi", null, null);

        LockerDetails lockerDetails1 = createLockerDetails("Rimi", 3L, 3L, "Viimsi", "Viimsi Rimi", "ok", "value", 7L);
        LockerDetails lockerDetails2 = createLockerDetails("Rimi", 4L, 4L, "Viimsi", "Viimsi Rimi", "ok", "value", 7L);
        LockerDetails lockerDetails3 = createLockerDetails("Rimi", 5L, 5L, "Viimsi", "Viimsi Rimi", "ok", "value", 7L);
        LockerDetails lockerDetails4 = createLockerDetails("Rimi", 6L, 6L, "Viimsi", "Viimsi Rimi", "ok", "value", 7L);
        LockerDetails lockerDetails5 = createLockerDetails("Rimi", 7L, 7L, "Viimsi", "Viimsi Rimi", "ok", "value", 7L);
        LockerDetails lockerDetails6 = createLockerDetails("Rimi", 8L, 8L, "Viimsi", "Viimsi Rimi", "ok", "value", 7L);
        LockerDetails lockerDetails7 = createLockerDetails("Rimi", 9L, 9L, "Viimsi", "Viimsi Rimi", "ok", "value", 7L);

        Store store = new Store();
        store.setName("Viimsi rimi");

        Route route = new Route();
        route.setName("Viimsi");
        route.setStore(store);

        Classifier classifier = new Classifier();
        classifier.setValueEt("value");

        List<Locker> notWorkingLockers = new ArrayList<>();
        notWorkingLockers.add(notWorkingLocker1);
        notWorkingLockers.add(notWorkingLocker2);
        notWorkingLockers.add(notWorkingLocker3);
        notWorkingLockers.add(notWorkingLocker4);
        notWorkingLockers.add(notWorkingLocker5);
        notWorkingLockers.add(notWorkingLocker6);
        notWorkingLockers.add(notWorkingLocker7);

        when(lockerRepository.findAllByStatusNot(anyString()))
                .thenReturn(notWorkingLockers);
        when(cabinetRepository.findOneById(anyLong()))
                .thenReturn(cabinet);
        when(cabinetRepository.findOneById(anyLong()))
                .thenReturn(cabinet);
        when(routeRepository.findByCabinetId(anyLong()))
                .thenReturn(route);
        when(classifierRepository.findFirstByKey(anyString()))
                .thenReturn(classifier);
        when(lockerLogRepository.countAllByLockerId(anyLong()))
                .thenReturn(notWorkingLockers.size());

        List<LockerDetails> result = cabinetService.getInactiveLockers();

        List<LockerDetails> testResult = new ArrayList<>();
        testResult.add(lockerDetails1);
        testResult.add(lockerDetails2);
        testResult.add(lockerDetails3);
        testResult.add(lockerDetails4);
        testResult.add(lockerDetails5);
        testResult.add(lockerDetails6);
        testResult.add(lockerDetails7);

        assertEquals(testResult.size(), result.size());
        assertEquals(testResult.get(0).getCabinetName(), result.get(0).getCabinetName());
        assertEquals(testResult.get(6).getStatus(), result.get(6).getStatus());

        assertEquals(testResult.get(0).getId(), result.get(0).getId());
        assertEquals(testResult.get(6).getId(), result.get(6).getId());

        assertEquals(testResult.get(0).getCabinetName(), result.get(0).getCabinetName());
        assertEquals(testResult.get(6).getCabinetName(), result.get(6).getCabinetName());

        assertEquals(testResult.get(0).getIndex(), result.get(0).getIndex());
        assertEquals(testResult.get(6).getIndex(), result.get(6).getIndex());

        assertEquals(testResult.get(0).getComment(), result.get(0).getComment());
        assertEquals(testResult.get(6).getComment(), result.get(6).getComment());

        assertEquals(testResult.get(0).getStatus(), result.get(0).getStatus());
        assertEquals(testResult.get(6).getStatus(), result.get(6).getStatus());

        assertEquals(testResult.get(0).getLogCount(), result.get(0).getLogCount());
        assertEquals(testResult.get(6).getLogCount(), result.get(6).getLogCount());
    }

    @Test(expected = NullPointerException.class)
    public void testGetInactiveLockersCabinetIdNull() {
        Locker notWorkingLocker1 = createLocker(3L, null, 3L, "LOCKER_NEEDS_ATTENTION", "ok", null);
        Locker notWorkingLocker2 = createLocker(4L, null, 4L, "LOCKER_NEEDS_REPAIRING", "ok", null);

        LockerDetails lockerDetails1 = createLockerDetails(null, 3L, 3L, null,
                null, "ok", "value", 7L);
        LockerDetails lockerDetails2 = createLockerDetails(null, 4L, 4L, null,
                null, "ok", "value", 7L);

        Store store = new Store();
        store.setName("Viimsi rimi");

        Route route = new Route();
        route.setName("Viimsi");
        route.setStore(store);

        Classifier classifier = new Classifier();
        classifier.setValueEt("value");

        List<Locker> notWorkingLockers = new ArrayList<>();
        notWorkingLockers.add(notWorkingLocker1);
        notWorkingLockers.add(notWorkingLocker2);

        when(lockerRepository.findAllByStatusNot(anyString()))
                .thenReturn(notWorkingLockers);
        when(cabinetRepository.findOneById(anyLong()))
                .thenThrow(NullPointerException.class);
        when(cabinetRepository.findOneById(anyLong()))
                .thenThrow(NullPointerException.class);
        when(routeRepository.findByCabinetId(anyLong()))
                .thenReturn(route);
        when(classifierRepository.findFirstByKey(anyString()))
                .thenReturn(classifier);
        when(lockerLogRepository.countAllByLockerId(anyLong()))
                .thenReturn(notWorkingLockers.size());

        List<LockerDetails> result = cabinetService.getInactiveLockers();
        List<LockerDetails> testResult = new ArrayList<>();
        testResult.add(lockerDetails1);
        testResult.add(lockerDetails2);

        assertEquals(testResult.size(), result.size());
    }

    @Test
    public void testGetInactiveLockersStatusNull() {
        Cabinet cabinet = createCabinet(1L, "Rimi", null, null);
        Locker locker1 = createLocker(3L, 1L, 3L, null, "ok", null);
        Locker locker2 = createLocker(4L, 1L, 4L, null, "ok", null);

        LockerDetails lockerDetails1 = createLockerDetails("Rimi", 3L, 3L, "Viimsi",
                "Viimsi Rimi", "ok", "value", 7L);
        LockerDetails lockerDetails2 = createLockerDetails("Rimi", 4L, 4L, "Viimsi",
                "Viimsi Rimi", "ok", "value", 7L);

        Store store = new Store();
        store.setName("Viimsi rimi");

        Route route = new Route();
        route.setName("Viimsi");
        route.setStore(store);

        Classifier classifier = new Classifier();
        classifier.setValueEt("value");

        List<Locker> notWorkingLockers = new ArrayList<>();

        when(lockerRepository.findAllByStatusNot(anyString()))
                .thenReturn(notWorkingLockers);
        when(cabinetRepository.findOneById(anyLong()))
                .thenReturn(cabinet);
        when(routeRepository.findByCabinetId(anyLong()))
                .thenReturn(route);
        when(classifierRepository.findFirstByKey(anyString()))
                .thenReturn(classifier);
        when(lockerLogRepository.countAllByLockerId(anyLong()))
                .thenReturn(notWorkingLockers.size());

        List<LockerDetails> result = cabinetService.getInactiveLockers();
        List<LockerDetails> testResult = new ArrayList<>();

        assertEquals(testResult.size(), result.size());
    }

    @Test
    public void testGetCabinetsForList() {
        CabinetListRow cabinetNormal1 = createCabinetListRow(1L, "Pirita Selver", "LOCKER_STATE_INACTIVE", 1L, "Selver", "Route");
        CabinetListRow cabinetNormal2 = createCabinetListRow(2L, "Lasnamäe Rimi", "LOCKER_STATE_ACTIVE", 0L, "Rimi", "Route");
        cabinetNormal2.setSecondaryId("Secondary id");

        Cabinet cabinet1 = createCabinet(1L, "Pirita Selver", null, null);
        Cabinet cabinet2 = createCabinet(2L, "Lasnamäe Rimi", null, null);
        cabinet2.setSecondaryId("Secondary id");

        Locker locker1 = createLocker(1L, 1L, 1L, "LOCKER_STATE_INACTIVE", null, null);
        Locker locker2 = createLocker(2L, 1L, 2L, "LOCKER_STATE_ACTIVE", null, null);

        Locker locker3 = createLocker(3L, 2L, 1L, "LOCKER_STATE_ACTIVE", null, null);
        Locker locker4 = createLocker(4L, 2L, 2L, "LOCKER_STATE_ACTIVE", null, null);

        RouteVersionCabinets routeVersionCabinets1 = new RouteVersionCabinets();
        routeVersionCabinets1.setId(1L);
        routeVersionCabinets1.setRouteVersionId(1L);
        routeVersionCabinets1.setCabinet(cabinet1);

        RouteVersionCabinets routeVersionCabinets2 = new RouteVersionCabinets();
        routeVersionCabinets2.setId(2L);
        routeVersionCabinets2.setRouteVersionId(2L);
        routeVersionCabinets2.setCabinet(cabinet2);

        RouteVersion routeVersion1 = new RouteVersion();
        routeVersion1.setId(1L);
        routeVersion1.setName("Route1");
        routeVersion1.setRouteId(1L);

        RouteVersion routeVersion2 = new RouteVersion();
        routeVersion2.setId(2L);
        routeVersion2.setName("Route2");
        routeVersion2.setRouteId(2L);

        Store store1 = new Store();
        store1.setId(1L);
        store1.setName("Selver");

        Store store2 = new Store();
        store2.setId(2L);
        store2.setName("Rimi");

        Route route1 = new Route();
        route1.setId(1L);
        route1.setName("Viimsi");
        route1.setStore(store1);

        Route route2 = new Route();
        route2.setId(1L);
        route2.setName("Lasnamägi");
        route2.setStore(store2);

        Classifier classifier = new Classifier();
        classifier.setId(1L);
        classifier.setKey("LOCKER_INACTIVE");
        classifier.setValueEt("Kamber on väljalülitatud");

        List<CabinetListRow> cabinetsListRow = new ArrayList<>();
        cabinetsListRow.add(cabinetNormal1);
        cabinetsListRow.add(cabinetNormal2);

        List<Cabinet> cabinets = new ArrayList<>();
        cabinets.add(cabinet1);
        cabinets.add(cabinet2);

        List<Locker> lockers1 = new ArrayList<>();
        lockers1.add(locker1);
        lockers1.add(locker2);

        List<Locker> lockers2 = new ArrayList<>();
        lockers2.add(locker3);
        lockers2.add(locker4);

        when(cabinetRepository.findAll())
                .thenReturn(cabinets);
        when(lockerRepository.findAllByCabinetIdOrderByIndex(1L))
                .thenReturn(lockers1);
        when(lockerRepository.findAllByCabinetIdOrderByIndex(2L))
                .thenReturn(lockers2);
        when(routeVersionCabinetsRepository.findFirstByCabinetId(1L))
                .thenReturn(routeVersionCabinets1);
        when(routeVersionCabinetsRepository.findFirstByCabinetId(2L))
                .thenReturn(routeVersionCabinets2);
        when(routeVersionRepository.findOneById(1L))
                .thenReturn(routeVersion1);
        when(routeVersionRepository.findOneById(2L))
                .thenReturn(routeVersion2);
        when(routeRepository.findById(1L))
                .thenReturn(Optional.ofNullable(route1));
        when(routeRepository.findById(2L))
                .thenReturn(Optional.ofNullable(route2));
        when(classifierRepository.findFirstByKey(anyString())).thenReturn(classifier);

        List<CabinetListRow> result = cabinetService.getCabinetsForList();


        assertEquals(cabinetsListRow.size(), result.size());
        assertEquals(cabinetsListRow.get(0).getId(), result.get(0).getId());
        assertEquals(cabinetsListRow.get(1).getId(), result.get(1).getId());

        assertEquals(cabinetsListRow.get(0).getName(), result.get(0).getName());
        assertEquals(cabinetsListRow.get(1).getName(), result.get(1).getName());

        assertEquals(cabinetsListRow.get(0).getStoreName(), result.get(0).getStoreName());
        assertEquals(cabinetsListRow.get(1).getStoreName(), result.get(1).getStoreName());

        assertEquals(cabinetsListRow.get(0).getLockerErrorCount(), result.get(0).getLockerErrorCount());
        assertEquals(cabinetsListRow.get(1).getLockerErrorCount(), result.get(1).getLockerErrorCount());

        assertEquals(cabinetsListRow.get(0).getLockerStatus(), result.get(0).getLockerStatus());
        assertEquals(cabinetsListRow.get(1).getLockerStatus(), result.get(1).getLockerStatus());

        assertEquals(cabinetsListRow.get(0).getAddress(), result.get(0).getAddress());
        assertEquals(cabinetsListRow.get(1).getAddress(), result.get(1).getAddress());

        assertEquals(cabinetsListRow.get(0).getSecondaryId(), result.get(0).getSecondaryId());
        assertEquals(cabinetsListRow.get(1).getSecondaryId(), result.get(1).getSecondaryId());

        assertEquals(cabinetsListRow.get(0).getStatus(), result.get(0).getStatus());
        assertEquals(cabinetsListRow.get(1).getStatus(), result.get(1).getStatus());
    }


    @Test
    public void testGetDeletedCabinetsForList() {
        Cabinet cabinet1 = createCabinet(1L, "Pirita Selver", null, 12345L);
        Cabinet cabinet2 = createCabinet(2L, "Lasnamäe Rimi", null, 12345L);
        cabinet2.setSecondaryId("Secondary id");

        Locker locker1 = createLocker(1L, 1L, 1L, "LOCKER_STATE_INACTIVE", null, null);
        Locker locker2 = createLocker(2L, 1L, 2L, "LOCKER_STATE_ACTIVE", null, null);

        Locker locker3 = createLocker(3L, 2L, 1L, "LOCKER_STATE_ACTIVE", null, null);
        Locker locker4 = createLocker(4L, 2L, 2L, "LOCKER_STATE_ACTIVE", null, null);

        RouteVersionCabinets routeVersionCabinets1 = new RouteVersionCabinets();
        routeVersionCabinets1.setId(1L);
        routeVersionCabinets1.setRouteVersionId(1L);
        routeVersionCabinets1.setCabinet(cabinet1);

        RouteVersionCabinets routeVersionCabinets2 = new RouteVersionCabinets();
        routeVersionCabinets2.setId(2L);
        routeVersionCabinets2.setRouteVersionId(2L);
        routeVersionCabinets2.setCabinet(cabinet2);

        RouteVersion routeVersion1 = new RouteVersion();
        routeVersion1.setId(1L);
        routeVersion1.setName("Route1");
        routeVersion1.setRouteId(1L);

        RouteVersion routeVersion2 = new RouteVersion();
        routeVersion2.setId(2L);
        routeVersion2.setName("Route2");
        routeVersion2.setRouteId(2L);

        Store store1 = new Store();
        store1.setId(1L);
        store1.setName("Selver");

        Store store2 = new Store();
        store2.setId(2L);
        store2.setName("Rimi");

        Route route1 = new Route();
        route1.setId(1L);
        route1.setName("Viimsi");
        route1.setStore(store1);

        Route route2 = new Route();
        route2.setId(1L);
        route2.setName("Lasnamägi");
        route2.setStore(store2);

        Classifier classifier = new Classifier();
        classifier.setId(1L);
        classifier.setKey("LOCKER_INACTIVE");
        classifier.setValueEt("Kamber on väljalülitatud");

        List<CabinetListRow> cabinetsListRow = new ArrayList<>();

        List<Cabinet> cabinets = new ArrayList<>();

        List<Locker> lockers1 = new ArrayList<>();
        lockers1.add(locker1);
        lockers1.add(locker2);

        List<Locker> lockers2 = new ArrayList<>();
        lockers2.add(locker3);
        lockers2.add(locker4);

        when(cabinetRepository.findAll())
                .thenReturn(cabinets);
        when(lockerRepository.findAllByCabinetIdOrderByIndex(1L))
                .thenReturn(lockers1);
        when(lockerRepository.findAllByCabinetIdOrderByIndex(2L))
                .thenReturn(lockers2);
        when(routeVersionCabinetsRepository.findFirstByCabinetId(1L))
                .thenReturn(routeVersionCabinets1);
        when(routeVersionCabinetsRepository.findFirstByCabinetId(2L))
                .thenReturn(routeVersionCabinets2);
        when(routeVersionRepository.findOneById(1L))
                .thenReturn(routeVersion1);
        when(routeVersionRepository.findOneById(2L))
                .thenReturn(routeVersion2);
        when(routeRepository.findById(1L))
                .thenReturn(Optional.ofNullable(route1));
        when(routeRepository.findById(2L))
                .thenReturn(Optional.ofNullable(route2));
        when(classifierRepository.findFirstByKey(anyString())).thenReturn(classifier);

        List<CabinetListRow> result = cabinetService.getCabinetsForList();
        assertEquals(cabinetsListRow.size(), result.size());
    }

    @Test
    public void testGetNullCabinetsForList() {
        Cabinet cabinet1 = createCabinet(null, null, null, null);
        Cabinet cabinet2 = createCabinet(null, null, null, null);

        Locker locker1 = createLocker(1L, null, 1L, "LOCKER_STATE_ACTIVE", null, null);
        Locker locker2 = createLocker(2L, null, 2L, "LOCKER_STATE_ACTIVE", null, null);

        List<Cabinet> cabinets = new ArrayList<>();

        List<Locker> lockers = new ArrayList<>();
        lockers.add(locker1);
        lockers.add(locker2);

        List<CabinetListRow> cabinetListRow = new ArrayList<>();

        Classifier classifier = new Classifier();
        classifier.setId(1L);
        classifier.setKey("LOCKER_INACTIVE");
        classifier.setValueEt("Kamber on väljalülitatud");

        when(cabinetRepository.findAll())
                .thenReturn(cabinets);
        when(lockerRepository.findAllByCabinetIdOrderByIndex(anyLong()))
                .thenReturn(lockers);
        when(routeVersionCabinetsRepository.findFirstByCabinetId(anyLong()))
                .thenReturn(null);
        when(classifierRepository.findFirstByKey(anyString()))
                .thenReturn(classifier);

        List<CabinetListRow> result = cabinetService.getCabinetsForList();
        assertEquals(cabinetListRow.size(), result.size());
    }

    @Test
    public void testGetCabinetsForListWithoutRouteVersionCabinets() {
        Cabinet cabinet = createCabinet(1L, "Pirita Selver", null, null);
        cabinet.setSecondaryId("Secondary id");

        Locker locker1 = createLocker(1L, 1L, 1L, "LOCKER_STATE_INACTIVE", null, null);
        Locker locker2 = createLocker(2L, 1L, 2L, "LOCKER_STATE_ACTIVE", null, null);

        CabinetListRow cabinetListRow = createCabinetListRow(1L, "Pirita Selver", "LOCKER_STATE_INACTIVE", 1L, null, null);
        cabinetListRow.setSecondaryId("Secondary id");
        Classifier classifier = new Classifier();
        classifier.setId(1L);
        classifier.setKey("LOCKER_INACTIVE");
        classifier.setValueEt("Kamber on väljalülitatud");

        List<Cabinet> cabinets = new ArrayList<>();
        cabinets.add(cabinet);

        List<Locker> lockers = new ArrayList<>();
        lockers.add(locker1);
        lockers.add(locker2);

        List<CabinetListRow> cabinetListRows = new ArrayList<>();
        cabinetListRows.add(cabinetListRow);

        when(cabinetRepository.findAll())
                .thenReturn(cabinets);
        when(lockerRepository.findAllByCabinetIdOrderByIndex(anyLong()))
                .thenReturn(lockers);
        when(routeVersionCabinetsRepository.findFirstByCabinetId(anyLong()))
                .thenReturn(null);
        when(classifierRepository.findFirstByKey(anyString()))
                .thenReturn(classifier);

        List<CabinetListRow> result = cabinetService.getCabinetsForList();
        assertEquals(cabinetListRows.size(), result.size());
        assertEquals(cabinetListRows.get(0).getId(), result.get(0).getId());
        assertEquals(cabinetListRows.get(0).getName(), result.get(0).getName());
        assertEquals(cabinetListRows.get(0).getStoreName(), result.get(0).getStoreName());
        assertEquals(cabinetListRows.get(0).getLockerErrorCount(), result.get(0).getLockerErrorCount());
        assertEquals(cabinetListRows.get(0).getLockerStatus(), result.get(0).getLockerStatus());
        assertEquals(cabinetListRows.get(0).getAddress(), result.get(0).getAddress());
        assertEquals(cabinetListRows.get(0).getSecondaryId(), result.get(0).getSecondaryId());
        assertEquals(cabinetListRows.get(0).getStatus(), result.get(0).getStatus());
    }

    @Test
    public void testGetLockerLogById() {
        Cabinet cabinetNormal1 = createCabinet(1L, "Pirita Selver", null, null);
        Cabinet cabinetNormal2 = createCabinet(2L, "Lasnamäe Rimi", null, null);

        Locker workingLocker1 = createLocker(1L, 1L, 1L, "LOCKER_OK", "ok", null);
        Locker workingLocker2 = createLocker(2L, 2L, 2L, "LOCKER_OK", "ok", null);

        LockerLog lockerLogNormal1 = createLockerLog(1L, cabinetNormal1, workingLocker1, "korras", "LOCKER_OK", null);
        LockerLog lockerLogNormal2 = createLockerLog(2L, cabinetNormal1, workingLocker1, "katki", "LOCKER_NEEDS_REPAIRING", null);
        LockerLog lockerLogNormal3 = createLockerLog(3L, cabinetNormal2, workingLocker2, "räpane", "LOCKER_IN_REPAIRING", null);

        List<LockerLog> lockerLogs1 = new ArrayList<>();
        lockerLogs1.add(lockerLogNormal1);
        lockerLogs1.add(lockerLogNormal2);

        List<LockerLog> lockerLogs2 = new ArrayList<>();
        lockerLogs2.add(lockerLogNormal3);

        when(cabinetService.getLockerLogById(1L))
                .thenReturn(lockerLogs1);
        when(cabinetService.getLockerLogById(2L))
                .thenReturn(lockerLogs2);

        List<LockerLog> result1 = cabinetService.getLockerLogById(1L);
        List<LockerLog> result2 = cabinetService.getLockerLogById(2L);

        assertEquals(lockerLogs1.size(), result1.size());
        assertEquals(lockerLogs2.size(), result2.size());

        assertEquals(lockerLogs1.get(0).getId(), result1.get(0).getId());
        assertEquals(lockerLogs2.get(0).getId(), result2.get(0).getId());

        assertEquals(lockerLogs1.get(0).getCabinet(), result1.get(0).getCabinet());
        assertEquals(lockerLogs2.get(0).getCabinet(), result2.get(0).getCabinet());

        assertEquals(lockerLogs1.get(0).getLocker(), result1.get(0).getLocker());
        assertEquals(lockerLogs2.get(0).getLocker(), result2.get(0).getLocker());

        assertEquals(lockerLogs1.get(0).getComment(), result1.get(0).getComment());
        assertEquals(lockerLogs2.get(0).getComment(), result2.get(0).getComment());

        assertEquals(lockerLogs1.get(0).getStatus(), result1.get(0).getStatus());
        assertEquals(lockerLogs2.get(0).getStatus(), result2.get(0).getStatus());
    }

    @Test
    public void testGetNullLockerLogById() {
        LockerLog lockerLogNormal1 = createLockerLog(null, null, null, null, null, null);
        LockerLog lockerLogNormal2 = createLockerLog(null, null, null, null, null, null);

        List<LockerLog> lockerLogs = new ArrayList<>();
        lockerLogs.add(lockerLogNormal1);
        lockerLogs.add(lockerLogNormal2);

        when(cabinetService.getLockerLogById(anyLong()))
                .thenReturn(lockerLogs);

        List<LockerLog> result = cabinetService.getLockerLogById(anyLong());

        assertEquals(lockerLogs.size(), result.size());
        assertEquals(lockerLogs.get(0).getId(), result.get(0).getId());
        assertEquals(lockerLogs.get(0).getCabinet(), result.get(0).getCabinet());
        assertEquals(lockerLogs.get(0).getLocker(), result.get(0).getLocker());
        assertEquals(lockerLogs.get(0).getComment(), result.get(0).getComment());
        assertEquals(lockerLogs.get(0).getStatus(), result.get(0).getStatus());
    }

    @Test
    public void testGetDeletedLockersLogById() {
        Cabinet cabinetNormal = createCabinet(2L, "Lasnamäe Rimi", null, null);
        Locker deletedLocker = createLocker(2L, 2L, 2L, "LOCKER_OK", "ok", 12345L);

        LockerLog lockerLog1 = createLockerLog(1L, cabinetNormal, deletedLocker, "korras", "LOCKER_OK", null);
        LockerLog lockerLog2 = createLockerLog(2L, cabinetNormal, deletedLocker, "katki", "LOCKER_NEEDS_REPAIRING", null);

        List<LockerLog> lockerLogs = new ArrayList<>();
        lockerLogs.add(lockerLog1);
        lockerLogs.add(lockerLog2);

        when(cabinetService.getLockerLogById(anyLong()))
                .thenReturn(lockerLogs);

        List<LockerLog> result = cabinetService.getLockerLogById(2L);

        assertEquals(lockerLogs.size(), result.size());
        assertEquals(lockerLogs.get(0).getId(), result.get(0).getId());
        assertEquals(lockerLogs.get(0).getCabinet(), result.get(0).getCabinet());
        assertEquals(lockerLogs.get(0).getLocker(), result.get(0).getLocker());
        assertEquals(lockerLogs.get(0).getComment(), result.get(0).getComment());
        assertEquals(lockerLogs.get(0).getStatus(), result.get(0).getStatus());
    }

    @Test
    public void testGetLockersLogByIdWithNullStatus() {
        Cabinet cabinetNormal = createCabinet(2L, "Lasnamäe Rimi", null, null);
        Locker locker = createLocker(2L, 2L, 2L, "LOCKER_OK", "ok", null);

        LockerLog lockerLog1 = createLockerLog(1L, cabinetNormal, locker, "korras", null, null);
        LockerLog lockerLog2 = createLockerLog(2L, cabinetNormal, locker, "katki", null, null);

        List<LockerLog> lockerLogs = new ArrayList<>();
        lockerLogs.add(lockerLog1);
        lockerLogs.add(lockerLog2);

        when(cabinetService.getLockerLogById(anyLong()))
                .thenReturn(lockerLogs);

        List<LockerLog> result = cabinetService.getLockerLogById(2L);

        assertEquals(lockerLogs.size(), result.size());
        assertEquals(lockerLogs.get(0).getId(), result.get(0).getId());
        assertEquals(lockerLogs.get(0).getCabinet(), result.get(0).getCabinet());
        assertEquals(lockerLogs.get(0).getLocker(), result.get(0).getLocker());
        assertEquals(lockerLogs.get(0).getComment(), result.get(0).getComment());
        assertEquals(lockerLogs.get(0).getStatus(), result.get(0).getStatus());
    }

    @Test
    public void testGetLockerLogsByIdWithNullCabinet() {

        Locker locker = createLocker(2L, 2L, 2L, "LOCKER_OK", "ok", null);
        Cabinet cabinet = createCabinet(null, null, null, null);

        LockerLog lockerLog1 = createLockerLog(1L, null, locker, "korras", "LOCKER_OK", 12345L);
        LockerLog lockerLog2 = createLockerLog(2L, null, locker, "katki", "LOCKER_NEEDS_REPAIRING", 12345L);

        List<LockerLog> lockerLogs = new ArrayList<>();
        lockerLogs.add(lockerLog1);
        lockerLogs.add(lockerLog2);

        when(cabinetService.getLockerLogById(anyLong()))
                .thenReturn(lockerLogs);

        List<LockerLog> result = cabinetService.getLockerLogById(2L);

        assertEquals(lockerLogs.size(), result.size());
    }

    @Test
    public void testGetLockers() {
        Locker workingLocker1 = createLocker(1L, 1L, 1L, "LOCKER_OK", "ok", null);
        Locker workingLocker2 = createLocker(2L, 1L, 2L, "LOCKER_NEEDS_CLEANING", "ok", null);
        Locker workingLocker3 = createLocker(3L, 2L, 3L, "LOCKER_OK", "ok", null);

        List<Locker> lockers = new ArrayList<>();
        lockers.add(workingLocker1);
        lockers.add(workingLocker2);

        when(lockerRepository.findAllByCabinetIdOrderByIndex(anyLong()))
                .thenReturn(lockers);
        List<Locker> result = cabinetService.getLockers(anyLong());

        assertEquals(lockers.size(), result.size());
        assertEquals(lockers.get(0).getCabinetId(), result.get(0).getCabinetId());
        assertEquals(lockers.get(1).getCabinetId(), result.get(1).getCabinetId());

        assertEquals(lockers.get(0).getId(), result.get(0).getId());
        assertEquals(lockers.get(1).getId(), result.get(1).getId());

        assertEquals(lockers.get(0).getStatus(), result.get(0).getStatus());
        assertEquals(lockers.get(1).getStatus(), result.get(1).getStatus());
    }

    @Test
    public void testGetDeletedLockers() {
        Locker workingLocker1 = createLocker(1L, 1L, 1L, "LOCKER_OK", "ok", 12345L);
        Locker workingLocker2 = createLocker(2L, 1L, 2L, "LOCKER_NEEDS_CLEANING", "ok", 12345L);
        List<Locker> lockers = new ArrayList<>();

        when(lockerRepository.findAllByCabinetIdOrderByIndex(anyLong()))
                .thenReturn(lockers);
        List<Locker> result = cabinetService.getLockers(anyLong());
        assertEquals(lockers.size(), result.size());
    }

    @Test
    public void testGetNullLockers() {
        Locker workingLocker1 = createLocker(null, null, null, null, null, null);
        Locker workingLocker2 = createLocker(null, null, null, null, null, null);
        List<Locker> lockers = new ArrayList<>();

        when(lockerRepository.findAllByCabinetIdOrderByIndex(anyLong()))
                .thenReturn(lockers);
        List<Locker> result = cabinetService.getLockers(anyLong());
        assertEquals(lockers.size(), result.size());
    }

    @Test
    public void testGetLockersWithNullIndex() {
        Locker workingLocker1 = createLocker(1L, 1L, null, "LOCKER_OK", "ok", null);
        Locker workingLocker2 = createLocker(2L, 1L, null, "LOCKER_NEEDS_CLEANING", "ok", null);
        Locker workingLocker3 = createLocker(3L, 2L, null, "LOCKER_OK", "ok", null);

        List<Locker> lockers = new ArrayList<>();
        lockers.add(workingLocker1);
        lockers.add(workingLocker2);

        when(lockerRepository.findAllByCabinetIdOrderByIndex(anyLong()))
                .thenReturn(lockers);
        List<Locker> result = cabinetService.getLockers(anyLong());

        assertEquals(lockers.size(), result.size());
        assertEquals(lockers.get(0).getCabinetId(), result.get(0).getCabinetId());
        assertEquals(lockers.get(1).getCabinetId(), result.get(1).getCabinetId());

        assertEquals(lockers.get(0).getId(), result.get(0).getId());
        assertEquals(lockers.get(1).getId(), result.get(1).getId());

        assertEquals(lockers.get(0).getStatus(), result.get(0).getStatus());
        assertEquals(lockers.get(1).getStatus(), result.get(1).getStatus());
    }

    @Test
    public void testGetCabinetById() {
        Cabinet cabinetNormal1 = createCabinet(1L, "Pirita Selver", null, null);
        Cabinet cabinetNormal2 = createCabinet(2L, "Lasnamäe Rimi", null, null);

        when(cabinetRepository.findOneById(1L)).thenReturn(cabinetNormal1);
        when(cabinetRepository.findOneById(2L)).thenReturn(cabinetNormal2);

        Cabinet resultNormalCabinet1 = cabinetService.getCabinetById(1L);
        Cabinet resultNormalCabinet2 = cabinetService.getCabinetById(2L);

        assertEquals(cabinetNormal1, resultNormalCabinet1);
        assertEquals(cabinetNormal2, resultNormalCabinet2);
    }

    @Test
    public void testGetDeletedCabinetsId() {
        Cabinet cabinet = createCabinet(1L, "Pirita Selver", null, 12345L);
        when(cabinetRepository.findOneById(anyLong()))
                .thenReturn(null);

        Cabinet resultCabinet = cabinetService.getCabinetById(null);
        assertEquals(null, resultCabinet);
    }

    @Test
    public void testGetNullCabinetsId() {
        Cabinet cabinetNull = createCabinet(null, null, null, null);
        when(cabinetRepository.findOneById(null))
                .thenReturn(null);

        Cabinet resultNullCabinet = cabinetService.getCabinetById(null);
        assertEquals(null, resultNullCabinet);
    }

    @Test
    public void testGetTimeSlotsForPeriod() {
        Cabinet cabinet = createCabinet(1L, "Solaris", null, null);
        TimeSlotConfig timeSlotConfig1 = createTimeSlot(1L, 1L, cabinet, 1234L, 12345L,
                BigDecimal.valueOf(2), 1234L, 1234L, 12345L, 1L);
        TimeSlotConfig timeSlotConfig2 = createTimeSlot(2L, 1L, cabinet, 1234L, 123456L,
                BigDecimal.valueOf(2), 12345L, 12345L, 123456L, 1L);

        TimeSlotInstance timeSlotInstance1 = createTimeSlotInstance(1L, BigDecimal.valueOf(2),
                1234L, 12345L, "ok");
        TimeSlotInstance timeSlotInstance2 = createTimeSlotInstance(2L, BigDecimal.valueOf(2),
                1234L, 12345L, "ok");

        List<TimeSlotInstance> timeSlotInstances = new ArrayList<>();
        timeSlotInstances.add(timeSlotInstance1);
        timeSlotInstances.add(timeSlotInstance2);

        RouteVersion routeVersion = createRouteVersion(1L, 1L, "OK", 12345L,
                123456L, "Route Version");

        TimeSlotsPerDay timeSlotsPerDay1 = createTimeSlotPerDay(12345L, timeSlotInstances);

        List<TimeSlotConfig> timeSlotList = new ArrayList<>();
        timeSlotList.add(timeSlotConfig1);
        timeSlotList.add(timeSlotConfig2);

        List<TimeSlotsPerDay> timeSlotsPerDays = new ArrayList<>();
        timeSlotsPerDays.add(timeSlotsPerDay1);

        when(timeSlotConfigRepository.findAllByCabinetIdOrderByStartTime(anyLong()))
                .thenReturn(timeSlotList);
        when(routeVersionRepository.findOneById(anyLong()))
                .thenReturn(routeVersion);
        when(deliveryRepository.findDeliveryCountForDateAndTimeSlot(anyLong(), anyLong())).thenReturn(2L);

        List<TimeSlotsPerDay> result = cabinetService.getTimeSlotsForPeriod(cabinet.getId(), 1);

        assertEquals(timeSlotsPerDays.size(), result.size());
    }

    @Test
    public void testGetTimeSlotsForNegativePeriod() {
        Cabinet cabinet = createCabinet(1L, "Solaris", null, null);
        TimeSlotConfig timeSlotConfig1 = createTimeSlot(1L, 1L, cabinet, 1234L, 12345L,
                BigDecimal.valueOf(2), 1234L, 1234L, 12345L, 1L);
        TimeSlotConfig timeSlotConfig2 = createTimeSlot(2L, 1L, cabinet, 1234L, 123456L,
                BigDecimal.valueOf(2), 12345L, 12345L, 123456L, 1L);

        TimeSlotInstance timeSlotInstance1 = createTimeSlotInstance(1L, BigDecimal.valueOf(2),
                1234L, 12345L, "ok");
        TimeSlotInstance timeSlotInstance2 = createTimeSlotInstance(2L, BigDecimal.valueOf(2),
                1234L, 12345L, "ok");

        List<TimeSlotInstance> timeSlotInstances = new ArrayList<>();
        timeSlotInstances.add(timeSlotInstance1);
        timeSlotInstances.add(timeSlotInstance2);

        RouteVersion routeVersion = createRouteVersion(1L, 1L, "OK", 12345L,
                123456L, "Route Version");

        TimeSlotsPerDay timeSlotsPerDay1 = createTimeSlotPerDay(12345L, timeSlotInstances);

        List<TimeSlotConfig> timeSlotList = new ArrayList<>();
        timeSlotList.add(timeSlotConfig1);
        timeSlotList.add(timeSlotConfig2);

        List<TimeSlotsPerDay> timeSlotsPerDays = new ArrayList<>();

        when(timeSlotConfigRepository.findAllByCabinetIdOrderByStartTime(anyLong()))
                .thenReturn(timeSlotList);
        when(routeVersionRepository.findOneById(anyLong()))
                .thenReturn(routeVersion);
        when(deliveryRepository.findDeliveryCountForDateAndTimeSlot(anyLong(), anyLong())).thenReturn(2L);

        List<TimeSlotsPerDay> result = cabinetService.getTimeSlotsForPeriod(cabinet.getId(), -3);

        assertEquals(0, result.size());
        assertEquals(timeSlotsPerDays, result);
    }

    @Test
    public void testGetTimeSlotsForPeriodWhenTimeSlotListNull() {
        Cabinet cabinet = createCabinet(1L, "Solaris", null, null);

        TimeSlotInstance timeSlotInstance1 = createTimeSlotInstance(1L, BigDecimal.valueOf(2),
                1234L, 12345L, "ok");
        TimeSlotInstance timeSlotInstance2 = createTimeSlotInstance(2L, BigDecimal.valueOf(2),
                1234L, 12345L, "ok");

        List<TimeSlotInstance> timeSlotInstances = new ArrayList<>();
        timeSlotInstances.add(timeSlotInstance1);
        timeSlotInstances.add(timeSlotInstance2);

        RouteVersion routeVersion = createRouteVersion(1L, 1L, "OK", 12345L,
                123456L, "Route Version");

        TimeSlotsPerDay timeSlotsPerDay1 = createTimeSlotPerDay(12345L, timeSlotInstances);

        List<TimeSlotConfig> timeSlotList = new ArrayList<>();

        List<TimeSlotsPerDay> timeSlotsPerDays = new ArrayList<>();

        when(timeSlotConfigRepository.findAllByCabinetIdOrderByStartTime(anyLong()))
                .thenReturn(timeSlotList);
        when(routeVersionRepository.findOneById(anyLong()))
                .thenReturn(routeVersion);
        when(deliveryRepository.findDeliveryCountForDateAndTimeSlot(anyLong(), anyLong())).thenReturn(2L);

        List<TimeSlotsPerDay> result = cabinetService.getTimeSlotsForPeriod(cabinet.getId(), -3);

        assertEquals(0, result.size());
        assertEquals(timeSlotsPerDays, result);
    }

    @Test
    public void testIsTimeSlotValidByRoute() {
        Cabinet cabinet = createCabinet(1L, "Pirita Selver", null, 12345L);
        TimeSlotConfig timeSlotConfig = createTimeSlot(1L, 1L, cabinet, 123L, 12345L,
                BigDecimal.valueOf(2), 1234L, 1234L, 12345L, 1L);
        RouteVersion routeVersion = createRouteVersion(1L, 1L, "ok", 1234L,
                12345L, "Route version");

        when(routeVersionRepository.findOneById(1L))
                .thenReturn(routeVersion);
        boolean result = cabinetService.isTimeSlotValidByRoute(timeSlotConfig, 12345L);
        assertEquals(true, result);
    }

    @Test
    public void testIsTimeSlotValidByRouteValidUntilSmallerThanValidFrom() {
        Cabinet cabinet = createCabinet(1L, "Pirita Selver", null, 12345L);
        TimeSlotConfig timeSlotConfig = createTimeSlot(1L, 1L, cabinet, 1234L, 12345L,
                BigDecimal.valueOf(2), 1234L, 1234L, 12345L, 1L);
        RouteVersion routeVersion = createRouteVersion(1L, 1L, "ok", 12345L, 1234L,
                "Route version");

        when(routeVersionRepository.findOneById(1L))
                .thenReturn(routeVersion);
        boolean result = cabinetService.isTimeSlotValidByRoute(timeSlotConfig, 1234L);
        assertEquals(false, result);
    }

    @Test(expected = NullPointerException.class)
    public void testIsTimeSlotValidByNullRouteVersion() {
        Cabinet cabinet = createCabinet(1L, "Pirita Selver", null, null);
        TimeSlotConfig timeSlotConfig = createTimeSlot(1L, 1L, cabinet, 1234L, 12345L, BigDecimal.valueOf(2), 1234L, 1234L, 12345L, 1L);
        RouteVersion routeVersion = createRouteVersion(1L, null, null, null, null, null);

        when(routeVersionRepository.findOneById(anyLong()))
                .thenReturn(routeVersion);
        when(cabinetService.isTimeSlotValidByRoute(timeSlotConfig, 14325L))
                .thenThrow(NullPointerException.class);
        boolean result = cabinetService.isTimeSlotValidByRoute(timeSlotConfig, 12345L);
        assertEquals(false, result);
    }

    @Test
    public void testIsTimeSlotValidByRouteDeliveryDateSmallerThanValidFrom() {
        Cabinet cabinet = createCabinet(1L, "Pirita Selver", null, 12345L);
        TimeSlotConfig timeSlotConfig = createTimeSlot(1L, 1L, cabinet, 1234L, 12345L, BigDecimal.valueOf(2), 1234L, 1234L, 12345L, 1L);
        RouteVersion routeVersion = createRouteVersion(1L, 1L, "ok", 1234L, 12345L, "Route version");

        when(routeVersionRepository.findOneById(1L))
                .thenReturn(routeVersion);
        boolean result = cabinetService.isTimeSlotValidByRoute(timeSlotConfig, 123L);
        assertEquals(false, result);
    }

    @Test
    public void testGetTimeSlotsForDay() {
        Cabinet cabinet = createCabinet(1L, "Pirita Selver", null, null);
        TimeSlotConfig timeSlotConfig1 = createTimeSlot(1L, 1L, cabinet, 1598947200000L, 1598950800000L, BigDecimal.valueOf(3), 1234L, 1598956200000L, 1598977800000L, 1L);
        timeSlotConfig1.setMaxOrders(3);
        TimeSlotConfig timeSlotConfig2 = createTimeSlot(2L, 1L, cabinet, 1598954400000L, 1598956200000L, BigDecimal.valueOf(3), 12345L, 1234L, 12345L, 1L);
        timeSlotConfig2.setMaxOrders(3);

        List<TimeSlotConfig> timeSlotConfigs = new ArrayList<>();
        timeSlotConfigs.add(timeSlotConfig1);
        timeSlotConfigs.add(timeSlotConfig2);

        TimeSlotInstance timeSlotInstance1 = createTimeSlotInstance(1L, BigDecimal.valueOf(3), 1598947200000L, 1598950800000L, "TIME_SLOT_STATUS_AVAILABLE");
        TimeSlotInstance timeSlotInstance2 = createTimeSlotInstance(2L, BigDecimal.valueOf(3), 1598954400000L, 1598956200000l, "TIME_SLOT_STATUS_AVAILABLE");

        List<TimeSlotInstance> timeSlotInstances = new ArrayList<>();
        timeSlotInstances.add(timeSlotInstance1);
        timeSlotInstances.add(timeSlotInstance2);

        when(deliveryRepository.findDeliveryCountForDateAndTimeSlot(anyLong(), anyLong()))
                .thenReturn(2L);
        List<TimeSlotInstance> result = cabinetService.getTimeSlotsForDay(1599001200000L, timeSlotConfigs);

        assertEquals(timeSlotInstances.size(), result.size());
        assertEquals(timeSlotInstances.get(0).getTimeSlotConfigId(), result.get(0).getTimeSlotConfigId());
        assertEquals(timeSlotInstances.get(1).getTimeSlotConfigId(), result.get(1).getTimeSlotConfigId());

        assertEquals(timeSlotInstances.get(0).getStartTime(), result.get(0).getStartTime());
        assertEquals(timeSlotInstances.get(1).getStartTime(), result.get(1).getStartTime());

        assertEquals(timeSlotInstances.get(0).getEndTime(), result.get(0).getEndTime());
        assertEquals(timeSlotInstances.get(1).getEndTime(), result.get(1).getEndTime());

        assertEquals(timeSlotInstances.get(0).getStatus(), result.get(0).getStatus());
        assertEquals(timeSlotInstances.get(1).getStatus(), result.get(1).getStatus());

        assertEquals(timeSlotInstances.get(0).getDeliveryFee(), result.get(0).getDeliveryFee());
        assertEquals(timeSlotInstances.get(1).getDeliveryFee(), result.get(1).getDeliveryFee());
    }

    @Test
    public void testGetTimeSlotsForDayWithUnavailableStatus() {
        Cabinet cabinet = createCabinet(1L, "Pirita Selver", null, null);
        TimeSlotConfig timeSlotConfig1 = createTimeSlot(1L, 1L, cabinet, 1234L, 12345l, BigDecimal.valueOf(9), 1234L, 123456l, 1234567l, 1L);
        timeSlotConfig1.setMaxOrders(20);
        TimeSlotConfig timeSlotConfig2 = createTimeSlot(2L, 1L, cabinet, 1598954400000L, 1598956200000L, BigDecimal.valueOf(3), 12345L, 1234L, 12345L, 1L);
        timeSlotConfig2.setMaxOrders(3);

        List<TimeSlotConfig> timeSlotConfigs = new ArrayList<>();
        timeSlotConfigs.add(timeSlotConfig1);
        timeSlotConfigs.add(timeSlotConfig2);

        TimeSlotInstance timeSlotInstance1 = createTimeSlotInstance(1L, BigDecimal.valueOf(9), 1234L, 12345l, "TIME_SLOT_STATUS_UNAVAILABLE");
        TimeSlotInstance timeSlotInstance2 = createTimeSlotInstance(2L, BigDecimal.valueOf(3), 1598954400000L, 1598956200000l, "TIME_SLOT_STATUS_AVAILABLE");

        List<TimeSlotInstance> timeSlotInstances = new ArrayList<>();
        timeSlotInstances.add(timeSlotInstance1);
        timeSlotInstances.add(timeSlotInstance2);

        when(deliveryRepository.findDeliveryCountForDateAndTimeSlot(anyLong(), anyLong()))
                .thenReturn(2L);
        List<TimeSlotInstance> result = cabinetService.getTimeSlotsForDay(1599001200000L, timeSlotConfigs);

        assertEquals(timeSlotInstances.size(), result.size());
    }

    @Test
    public void testGetTimeSlotsForDayWithTimeSlotIdNull() {
        Cabinet cabinet = createCabinet(1L, "Pirita Selver", null, null);
        TimeSlotConfig timeSlotConfig1 = createTimeSlot(null, 1L, cabinet, 1598947200000L, 1598950800000L, BigDecimal.valueOf(3), 1234L, 1598956200000L, 1598977800000L, 1L);
        timeSlotConfig1.setMaxOrders(3);
        TimeSlotConfig timeSlotConfig2 = createTimeSlot(null, 1L, cabinet, 1598954400000L, 1598956200000L, BigDecimal.valueOf(3), 12345L, 1234L, 12345L, 1L);
        timeSlotConfig2.setMaxOrders(3);

        List<TimeSlotConfig> timeSlotConfigs = new ArrayList<>();
        timeSlotConfigs.add(timeSlotConfig1);
        timeSlotConfigs.add(timeSlotConfig2);

        TimeSlotInstance timeSlotInstance1 = createTimeSlotInstance(null, BigDecimal.valueOf(3), 1598947200000L, 1598950800000L, "TIME_SLOT_STATUS_AVAILABLE");
        TimeSlotInstance timeSlotInstance2 = createTimeSlotInstance(null, BigDecimal.valueOf(3), 1598954400000L, 1598956200000l, "TIME_SLOT_STATUS_AVAILABLE");

        List<TimeSlotInstance> timeSlotInstances = new ArrayList<>();
        timeSlotInstances.add(timeSlotInstance1);
        timeSlotInstances.add(timeSlotInstance2);

        when(deliveryRepository.findDeliveryCountForDateAndTimeSlot(anyLong(), anyLong()))
                .thenReturn(2L);
        List<TimeSlotInstance> result = cabinetService.getTimeSlotsForDay(1599001200000L, timeSlotConfigs);

        assertEquals(timeSlotInstances.size(), result.size());
        assertEquals(timeSlotInstances.get(0).getTimeSlotConfigId(), result.get(0).getTimeSlotConfigId());

        assertEquals(timeSlotInstances.get(0).getStartTime(), result.get(0).getStartTime());
        assertEquals(timeSlotInstances.get(1).getStartTime(), result.get(1).getStartTime());

        assertEquals(timeSlotInstances.get(0).getEndTime(), result.get(0).getEndTime());
        assertEquals(timeSlotInstances.get(1).getEndTime(), result.get(1).getEndTime());

        assertEquals(timeSlotInstances.get(0).getStatus(), result.get(0).getStatus());
        assertEquals(timeSlotInstances.get(1).getStatus(), result.get(1).getStatus());

        assertEquals(timeSlotInstances.get(0).getDeliveryFee(), result.get(0).getDeliveryFee());
        assertEquals(timeSlotInstances.get(1).getDeliveryFee(), result.get(1).getDeliveryFee());
    }

    @Test
    public void testLogAndSetCabinetStatus() {
        SpTerminalEvent spTerminalEvent = createTerminalEvent("id", null, null, false);
        Cabinet cabinet = createCabinet(1L, "Tartu Selver", "CABINET_STATUS_ACTIVE", null);
        cabinet.setExternalId("id");
        CabinetLog cabinetLogToReturn = createCabinetLog(1L, 1L, null, "CABINET_STATUS_ACTIVE");

        when(cabinetRepository.findOneByExternalId("id"))
                .thenReturn(cabinet);
        when(cabinetLogRepository.save(Mockito.any(CabinetLog.class)))
                .thenReturn(cabinetLogToReturn);

        Cabinet result = cabinetService.logAndSetCabinetStatus(spTerminalEvent);

        assertEquals(cabinet, result);
        assertEquals(cabinet.getStatus(), result.getStatus());
        assertEquals(cabinet.getName(), result.getName());
    }

    @Test
    public void testLogAndSetCabinetStatusIsDeleted() {
        SpTerminalEvent spTerminalEvent = createTerminalEvent("id", null, null, true);
        Cabinet cabinet = createCabinet(1L, "Tartu Selver", "CABINET_STATUS_INACTIVE", null);
        cabinet.setExternalId("id");
        CabinetLog cabinetLogToReturn = createCabinetLog(1L, 1L, null, "CABINET_STATUS_INACTIVE");

        when(cabinetRepository.findOneByExternalId("id"))
                .thenReturn(cabinet);
        when(cabinetLogRepository.save(Mockito.any(CabinetLog.class)))
                .thenReturn(cabinetLogToReturn);

        Cabinet result = cabinetService.logAndSetCabinetStatus(spTerminalEvent);

        assertEquals(cabinet, result);
        assertEquals(cabinet.getStatus(), result.getStatus());
        assertEquals(cabinet.getName(), result.getName());
    }

    @Test
    public void testLogAndSetCabinetStatusWhenCabinetNull() {
        SpTerminalEvent spTerminalEvent = createTerminalEvent("id", null, null, false);

        when(cabinetRepository.findOneByExternalId("id"))
                .thenReturn(null);
        Cabinet result = cabinetService.logAndSetCabinetStatus(spTerminalEvent);
        assertEquals(null, result);

    }

    @Test
    public void getLockerClassifiers() {
        Classifier classifier1 = createClassifier(1L, 8L, "LOCKER_OK");
        Classifier classifier2 = createClassifier(2L, 8L, "LOCKER_NEEDS_ATTENTION");
        Classifier classifier3 = createClassifier(3L, 8L, "LOCKER_NEEDS_CLEANING");
        Classifier classifier4 = createClassifier(3L, 10L, "CABINET_LOCKED");

        List<Classifier> classifiers = new ArrayList<>();
        classifiers.add(classifier1);
        classifiers.add(classifier2);
        classifiers.add(classifier3);

        when(classifierRepository.findAllByParentId(8L))
                .thenReturn(classifiers);

        List<Classifier> result = cabinetService.getLockerClassifiers();

        assertEquals(classifiers.size(), result.size());
        assertEquals(classifiers.get(0).getKey(), result.get(0).getKey());
        assertEquals(classifiers.get(1).getKey(), result.get(1).getKey());
        assertEquals(classifiers.get(2).getKey(), result.get(2).getKey());
    }

    @Test
    public void getLockerClassifiersFalseParentId() {
        Classifier classifier1 = createClassifier(1L, 7L, "LOCKER_OK");
        Classifier classifier2 = createClassifier(2L, 7L, "LOCKER_NEEDS_ATTENTION");

        List<Classifier> classifiers = new ArrayList<>();
        classifiers.add(classifier1);
        classifiers.add(classifier2);

        when(classifierRepository.findAllByParentId(7L))
                .thenReturn(classifiers);

        List<Classifier> result = cabinetService.getLockerClassifiers();

        assertEquals(0, result.size());
    }

    @Test
    public void testGetLockerStatuses() {
        Classifier classifier1 = createClassifier(1L, 8L, "LOCKER_OK");
        Classifier classifier2 = createClassifier(2L, 8L, "LOCKER_NEEDS_ATTENTION");
        Classifier classifier3 = createClassifier(3L, 8L, "LOCKER_NEEDS_CLEANING");
        Classifier classifier4 = createClassifier(3L, 8L, "LOCKER_STATE_PACKAGE_LOADED");

        List<Classifier> allClassifiers = new ArrayList<>();
        allClassifiers.add(classifier1);
        allClassifiers.add(classifier2);
        allClassifiers.add(classifier3);
        allClassifiers.add(classifier4);

        List<Classifier> validClassifiers = new ArrayList<>();
        validClassifiers.add(classifier1);
        validClassifiers.add(classifier2);
        validClassifiers.add(classifier3);

        when(classifierRepository.findAllByParentId(anyLong()))
                .thenReturn(allClassifiers);

        List<Classifier> result = cabinetService.getLockerStatuses();

        assertEquals(validClassifiers.size(), result.size());
        assertEquals(validClassifiers.get(0).getKey(), result.get(0).getKey());
        assertEquals(validClassifiers.get(1).getKey(), result.get(1).getKey());
        assertEquals(validClassifiers.get(2).getKey(), result.get(2).getKey());
    }

    @Test
    public void testUpdateLockerStatus() {
        Long lockerId = 1L;

        LockerLogUpdate lockerLogUpdate = new LockerLogUpdate();
        lockerLogUpdate.setStatus("LOCKER_STATE_ACTIVE");
        lockerLogUpdate.setComment("Uks ei lähe lukku");

        Locker locker = createLocker(1L, 1L, 1L, null, null, null);
        Cabinet cabinet = createCabinet(1L, "Selver", null, null);
        LockerLog lockerLog = createLockerLog(1L, cabinet, locker, "Uks ei lähe lukku", "LOCKER_STATE_ACTIVE", null);

        when(lockerRepository.findOneById(anyLong()))
                .thenReturn(locker);
        when(cabinetRepository.findOneById(anyLong()))
                .thenReturn(cabinet);

        LockerLog result = cabinetService.updateLockerStatus(lockerId, lockerLogUpdate);

        assertEquals(result.getStatus(), lockerLog.getStatus());
        assertEquals(result.getComment(), lockerLog.getComment());
        assertEquals(result.getLocker(), lockerLog.getLocker());
        assertEquals(result.getCabinet(), lockerLog.getCabinet());
    }

    @Test
    public void testUpdateLockerStatusWhenStatusEmpty() {
        Long lockerId = 1L;

        LockerLogUpdate lockerLogUpdate = new LockerLogUpdate();
        lockerLogUpdate.setStatus("");
        lockerLogUpdate.setComment("Uks ei lähe lukku");

        Locker locker = createLocker(1L, 1L, 1L, "LOCKER_STATE_ACTIVE", null, null);
        Cabinet cabinet = createCabinet(1L, "Selver", null, null);
        LockerLog lockerLog = createLockerLog(1L, cabinet, locker, "Uks ei lähe lukku", "LOCKER_STATE_ACTIVE", null);

        when(lockerRepository.findOneById(anyLong()))
                .thenReturn(locker);
        when(cabinetRepository.findOneById(anyLong()))
                .thenReturn(cabinet);

        LockerLog result = cabinetService.updateLockerStatus(lockerId, lockerLogUpdate);

        assertEquals(result.getStatus(), lockerLog.getStatus());
        assertEquals(result.getComment(), lockerLog.getComment());
        assertEquals(result.getLocker(), lockerLog.getLocker());
        assertEquals(result.getCabinet(), lockerLog.getCabinet());
    }

    @Test
    public void testGetUserCabinets() {
        Cabinet cabinet1 = createCabinet(1L, "Selver", null, null);
        Cabinet cabinet2 = createCabinet(2L, "Rimi", null, null);

        UserCabinet userCabinet1 = createUserCabinet(1L, 1L, 1L, null);
        UserCabinet userCabinet2 = createUserCabinet(2L, 1L, 2L, null);

        List<Cabinet> cabinets = new ArrayList<>();
        cabinets.add(cabinet1);
        cabinets.add(cabinet2);
        List<UserCabinet> userCabinets = new ArrayList<>();
        userCabinets.add(userCabinet1);
        userCabinets.add(userCabinet2);

        when(userCabinetRepository.findAllByUserId(1L))
                .thenReturn(userCabinets);
        when(cabinetRepository.findOneById(1L))
                .thenReturn(cabinet1);
        when(cabinetRepository.findOneById(2L))
                .thenReturn(cabinet2);
        List<Cabinet> result = cabinetService.getUserCabinets(1L);

        assertEquals(cabinets, result);
        assertEquals(cabinets.size(), result.size());
    }

    @Test
    public void testGetNullUserCabinets() {
        Cabinet cabinet1 = createCabinet(1L, "Selver", null, null);
        Cabinet cabinet2 = createCabinet(2L, "Rimi", null, null);

        UserCabinet userCabinet1 = createUserCabinet(1L, null, 1L, null);
        UserCabinet userCabinet2 = createUserCabinet(2L, null, 2L, null);

        List<Cabinet> cabinets = new ArrayList<>();

        when(userCabinetRepository.findAllByUserId(null))
                .thenReturn(null);
        when(cabinetRepository.findOneById(1L))
                .thenReturn(cabinet1);
        when(cabinetRepository.findOneById(2L))
                .thenReturn(cabinet2);
        List<Cabinet> result = cabinetService.getUserCabinets(1L);

        assertEquals(cabinets, result);
        assertEquals(cabinets.size(), result.size());
    }

    @Test
    public void testGetUserNullCabinets() {
        Cabinet cabinet1 = createCabinet(null, null, null, null);

        UserCabinet userCabinet1 = createUserCabinet(1L, 1L, null, null);
        UserCabinet userCabinet2 = createUserCabinet(2L, 1L, null, null);

        List<UserCabinet> userCabinets = new ArrayList<>();
        userCabinets.add(userCabinet1);
        userCabinets.add(userCabinet2);

        when(userCabinetRepository.findAllByUserId(1L))
                .thenReturn(userCabinets);
        when(cabinetRepository.findOneById(null))
                .thenReturn(null);
        List<Cabinet> result = cabinetService.getUserCabinets(1L);

        assertEquals(null, result.get(0));
        assertEquals(null, result.get(1));
    }

    @Test
    public void testGetCabinetLogs() {
        Long startTime = 1533051100L;
        Long endTime = 1733207472L;

        Locker locker = createLocker(1L, 1L, 1L, "LOCKER_STATE_ACTIVE", null, null);
        locker.setStatusTempMode("LOCKER_STATE_ACTIVE");
        locker.setThermoMode(4L);
        Cabinet cabinet = createCabinet(1L, "Selver", null, null);

        LockerLog lockerLog1 = createLockerLog(1L, cabinet, locker, null, "LOCKER_STATE_ACTIVE", null);
        LockerLog lockerLog2 = createLockerLog(2L, cabinet, locker, null, "LOCKER_STATE_NEEDS_ATTENTION", null);
        lockerLog1.setExtCreatedAt(1533051110L);
        lockerLog2.setExtCreatedAt(1533051110L);

        long millis = System.currentTimeMillis();
        CabinetLogRow cabinetLogRow1 = createCabinetLogRow(millis, "LOCKER_STATE_ACTIVE", 1L);
        CabinetLogRow cabinetLogRow2 = createCabinetLogRow(millis, "LOCKER_STATE_NEEDS_ATTENTION", 1L);


        List<LockerLog> lockerLogs = new ArrayList<>();
        lockerLogs.add(lockerLog1);
        lockerLogs.add(lockerLog2);

        List<CabinetLogRow> cabinetLogRows = new ArrayList<>();
        cabinetLogRows.add(cabinetLogRow1);
        cabinetLogRows.add(cabinetLogRow2);

        when(lockerLogRepository.findAllByCabinetId(anyLong()))
                .thenReturn(lockerLogs);
        List<CabinetLogRow> result = cabinetService.getCabinetLogs(startTime, endTime, 1L, null);

        assertEquals(cabinetLogRows.size(), result.size());
        assertEquals(cabinetLogRows.get(0).getIndex(), result.get(0).getIndex());
        assertEquals(cabinetLogRows.get(1).getIndex(), result.get(1).getIndex());

        assertEquals(cabinetLogRows.get(0).getStatus(), result.get(0).getStatus());
        assertEquals(cabinetLogRows.get(1).getStatus(), result.get(1).getStatus());
    }

    @Test
    public void testGetCabinetLogsWithTypeOne() {
        Long startTime = 1533051100L;
        Long endTime = 1733207472L;

        Locker locker = createLocker(1L, 1L, 1L, "LOCKER_STATE_ACTIVE", null, null);
        locker.setStatusTempMode("LOCKER_STATE_ACTIVE");
        locker.setThermoMode(4L);
        Cabinet cabinet = createCabinet(1L, "Selver", null, null);

        LockerLog lockerLog1 = createLockerLog(1L, cabinet, locker, null, "LOCKER_STATE_ACTIVE", null);
        LockerLog lockerLog2 = createLockerLog(2L, cabinet, locker, null, "LOCKER_STATE_NEEDS_ATTENTION", null);
        lockerLog1.setExtCreatedAt(1533051110L);
        lockerLog2.setExtCreatedAt(1533051110L);

        List<LockerLog> lockerLogs = new ArrayList<>();
        lockerLogs.add(lockerLog1);
        lockerLogs.add(lockerLog2);

        List<CabinetLogRow> cabinetLogRows = new ArrayList<>();

        when(lockerLogRepository.findAllByCabinetId(anyLong()))
                .thenReturn(lockerLogs);
        List<CabinetLogRow> result = cabinetService.getCabinetLogs(startTime, endTime, 1L, 1);

        assertEquals(cabinetLogRows.size(), result.size());
    }

    @Test(expected = NullPointerException.class)
    public void testGetCabinetLogsWhenExtCreatedAtNull() {
        Long startTime = 1533051100L;
        Long endTime = 1733207472L;

        Locker locker = createLocker(1L, 1L, 1L, "LOCKER_STATE_ACTIVE", null, null);
        locker.setStatusTempMode("LOCKER_STATE_ACTIVE");
        locker.setThermoMode(4L);
        Cabinet cabinet = createCabinet(1L, "Selver", null, null);

        LockerLog lockerLog1 = createLockerLog(1L, cabinet, locker, null, "LOCKER_STATE_ACTIVE", null);
        LockerLog lockerLog2 = createLockerLog(2L, cabinet, locker, null, "LOCKER_STATE_NEEDS_ATTENTION", null);
        lockerLog1.setExtCreatedAt(null);
        lockerLog2.setExtCreatedAt(null);

        List<LockerLog> lockerLogs = new ArrayList<>();
        lockerLogs.add(lockerLog1);
        lockerLogs.add(lockerLog2);

        when(lockerLogRepository.findAllByCabinetId(anyLong()))
                .thenReturn(lockerLogs);
        when(cabinetService.getCabinetLogs(startTime, endTime, 1L, null))
                .thenThrow(NullPointerException.class);
    }
}
