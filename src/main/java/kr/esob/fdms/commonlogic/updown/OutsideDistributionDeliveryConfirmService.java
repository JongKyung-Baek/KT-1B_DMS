package kr.esob.fdms.commonlogic.updown;

import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OutsideDistributionDeliveryConfirmService {

    @Inject
    private CommonUpdownDao commonUpdownDao;

    @Transactional
    public void markConfirmed(String requestNo, String objectId, String objectType, String fileSeqOrNo) {
        if (isBlank(requestNo) || isBlank(objectId) || isBlank(objectType)) {
            return;
        }

        String normalizedType = objectType.trim().toUpperCase();
        if (!"DOC".equals(normalizedType) && !"DRAWING".equals(normalizedType)) {
            return;
        }

        Map<String, Object> base = commonUpdownDao.selectOutsideDistributionActLogBase(
            requestNo,
            objectId,
            normalizedType,
            fileSeqOrNo
        );
        if (base == null) {
            return;
        }

        String resolvedObjectId = readMapString(base, "objectId");
        if (isBlank(resolvedObjectId)) {
            return;
        }

        commonUpdownDao.updateOutsideDistributionDeliveryConfirmDoc(resolvedObjectId, normalizedType);
        Integer remainingCount = commonUpdownDao.countOutsideDistributionPendingDeliveryConfirmDocs(resolvedObjectId, normalizedType);
        if (remainingCount != null && remainingCount.intValue() == 0) {
            commonUpdownDao.updateOutsideDistributionDeliveryConfirmHeader(resolvedObjectId, normalizedType);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String readMapString(Map<String, Object> source, String key) {
        if (source == null || key == null) {
            return null;
        }
        Object value = source.get(key);
        if (value == null) {
            value = source.get(key.toLowerCase());
        }
        if (value == null) {
            value = source.get(key.toUpperCase());
        }
        return value == null ? null : String.valueOf(value);
    }
}
