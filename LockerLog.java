package ee.coop.delivery.domain;


import ee.coop.core.domain.SoftDeletableEntity;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(schema = "delivery", name = "locker_log")
public class LockerLog extends SoftDeletableEntity {

    @OneToOne
    private Cabinet cabinet;
    @OneToOne
    private Locker locker;
    private String status;
    private String comment;
    private Long extCreatedAt;
    private String statusMaintenance;
    private String statusTempMode;

    public String getStatusMaintenance() {
        return statusMaintenance;
    }

    public void setStatusMaintenance(String statusMaintenance) {
        this.statusMaintenance = statusMaintenance;
    }

    public String getStatusTempMode() {
        return statusTempMode;
    }

    public void setStatusTempMode(String statusTempMode) {
        this.statusTempMode = statusTempMode;
    }

    public Long getExtCreatedAt() {
        return extCreatedAt;
    }

    public void setExtCreatedAt(Long extCreatedAt) {
        this.extCreatedAt = extCreatedAt;
    }

    public Cabinet getCabinet() {
        return cabinet;
    }

    public void setCabinet(Cabinet cabinet) {
        this.cabinet = cabinet;
    }

    public Locker getLocker() {
        return locker;
    }

    public void setLocker(Locker locker) {
        this.locker = locker;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
