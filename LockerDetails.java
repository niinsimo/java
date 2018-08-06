package ee.coop.delivery.dto;

/**
 * For displaying locker data on dashboard
 */
public class LockerDetails {

    private String cabinetName;
    private Long index;
    private String storeName;
    private String routeName;
    private String comment;
    private String status;
    private Long id;
    private int logCount;

    public LockerDetails() {
    }

    public LockerDetails(String cabinetName, Long index, String storeName, String routeName, String comment, String status, Long id, int logCount) {
        this.cabinetName = cabinetName;
        this.index = index;
        this.storeName = storeName;
        this.routeName = routeName;
        this.comment = comment;
        this.status = status;
        this.id = id;
        this.logCount = logCount;
    }

    public String getCabinetName() {
        return cabinetName;
    }

    public void setCabinetName(String cabinetName) {
        this.cabinetName = cabinetName;
    }

    public Long getIndex() {
        return index;
    }

    public void setIndex(Long index) {
        this.index = index;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getLogCount() {
        return logCount;
    }

    public void setLogCount(Long logCount) {
        this.logCount = Math.toIntExact(logCount);
    }
}
