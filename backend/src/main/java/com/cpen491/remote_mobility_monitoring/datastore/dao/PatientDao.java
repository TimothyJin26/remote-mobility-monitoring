package com.cpen491.remote_mobility_monitoring.datastore.dao;

import com.cpen491.remote_mobility_monitoring.datastore.exception.DuplicateRecordException;
import com.cpen491.remote_mobility_monitoring.datastore.exception.RecordDoesNotExistException;
import com.cpen491.remote_mobility_monitoring.datastore.model.Caregiver;
import com.cpen491.remote_mobility_monitoring.datastore.model.Const;
import com.cpen491.remote_mobility_monitoring.datastore.model.Patient;
import com.cpen491.remote_mobility_monitoring.dependency.utility.Validator;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.cpen491.remote_mobility_monitoring.datastore.model.Const.BaseTable;
import static com.cpen491.remote_mobility_monitoring.datastore.model.Const.CaregiverTable;
import static com.cpen491.remote_mobility_monitoring.datastore.model.Const.PatientTable;

@Slf4j
@AllArgsConstructor
public class PatientDao {
    @NonNull
    GenericDao genericDao;
    @NonNull
    CaregiverDao caregiverDao;

    /**
     * Creates a new Patient record. If deviceId is provided, record with the deviceId must not already exist.
     *
     * @param newRecord The Patient record to create
     * @throws DuplicateRecordException If record with the given deviceId already exists
     * @throws IllegalArgumentException
     * @throws NullPointerException Above 2 exceptions are thrown if any of firstName, lastName,
     *                              dateOfBirth, phoneNumber, authCode, authCodeTimestamp, or verified are empty
     */
    public void create(Patient newRecord) {
        log.info("Creating new Patient record {}", newRecord);
        Validator.validatePatient(newRecord);

        String deviceId = newRecord.getDeviceId();
        if (deviceId != null && findByDeviceId(deviceId) != null) {
            throw new DuplicateRecordException(Patient.class.getSimpleName(), deviceId);
        }

        GenericDao.setId(newRecord, PatientTable.ID_PREFIX);
        genericDao.create(Patient.convertToMap(newRecord));
    }

    /**
     * Adds a patient to a caregiver. Patient and caregiver must already exist.
     *
     * @param patientId The id of the Patient record
     * @param caregiverId The id of the Caregiver record
     * @throws RecordDoesNotExistException If Patient or Caregiver records do not exist
     * @throws IllegalArgumentException
     * @throws NullPointerException Above 2 exceptions are thrown if patientId or caregiverId is empty
     */
    public void addCaregiver(String patientId, String caregiverId) {
        log.info("Adding Patient [{}] to Caregiver [{}]", patientId, caregiverId);
        Validator.validatePatientId(patientId);
        Validator.validateCaregiverId(caregiverId);

        Patient patient = findById(patientId);
        Caregiver caregiver = caregiverDao.findById(caregiverId);

        genericDao.addAssociation(Caregiver.convertToMap(caregiver), Patient.convertToMap(patient));
    }

    /**
     * Finds a Patient record by id.
     *
     * @param id The id of the record to find
     * @return {@link Patient}
     * @throws RecordDoesNotExistException If record with the given id does not exist
     * @throws IllegalArgumentException
     * @throws NullPointerException Above 2 exceptions are thrown if id is empty
     */
    public Patient findById(String id) {
        log.info("Finding Patient record with id [{}]", id);
        Validator.validateId(id);

        GetItemResponse response = genericDao.findByPartitionKey(id);
        if (!response.hasItem()) {
            log.error("Cannot find Patient record with id [{}]", id);
            throw new RecordDoesNotExistException(Patient.class.getSimpleName(), id);
        }
        return Patient.convertFromMap(response.item());
    }

    /**
     * Finds a Patient record by deviceId. Returns null if record does not exist.
     *
     * @param deviceId The deviceId of the record to find
     * @return {@link Patient}
     * @throws IllegalArgumentException
     * @throws NullPointerException Above 2 exceptions are thrown if deviceId is empty
     */
    public Patient findByDeviceId(String deviceId) {
        log.info("Finding Patient record with deviceId [{}]", deviceId);
        Validator.validateDeviceId(deviceId);

        QueryResponse response = genericDao
                .findAllByIndexPartitionKey(PatientTable.DEVICE_ID_INDEX_NAME, PatientTable.DEVICE_ID_NAME, deviceId);
        if (!response.hasItems() || response.items().size() == 0) {
            log.info("Cannot find Patient record with deviceId [{}]", deviceId);
            return null;
        }
        return Patient.convertFromMap(response.items().get(0));
    }

    /**
     * Batch finds all Patient records by IDs.
     *
     * @param ids Set of IDs of the records to find
     * @return {@link List}
     * @throws NullPointerException If ids is null
     */
    public List<Patient> batchFindById(List<String> ids) {
        log.info("Batch finding Patient records matching IDs {}", ids);
        Validator.validateIds(ids);
        for (String id : ids) {
            Validator.validatePatientId(id);
        }

        List<Map<String, AttributeValue>> result = genericDao.batchFindByPartitionKey(ids);
        return result.stream().map(Patient::convertFromMap).collect(Collectors.toList());
    }

    /**
     * Find all Caregivers caring for this patient.
     *
     * @param patientId The id of the Patient record
     * @return {@link List}
     * @throws IllegalArgumentException
     * @throws NullPointerException Above 2 exceptions are thrown if patientId is empty
     */
    public List<Caregiver> findAllCaregivers(String patientId) {
        log.info("Finding all Caregiver records caring for Patient [{}]", patientId);
        Validator.validatePatientId(patientId);

        List<Map<String, AttributeValue>> result = genericDao
                .findAllAssociationsOnIndex(patientId, CaregiverTable.ID_PREFIX, BaseTable.SID_INDEX_NAME).items();
        return result.stream().map(map -> {
            Caregiver caregiver = Caregiver.convertFromMap(map);
            caregiver.setSid(caregiver.getPid());
            return caregiver;
        }).collect(Collectors.toList());
    }

    /**
     * Updates a Patient record. Record with given id must already exist.
     * Record with given deviceId should not already exist unless it is the same record being updated.
     *
     * @param updatedRecord The Patient record to update
     * @throws DuplicateRecordException If record with the given deviceId already exists
     * @throws RecordDoesNotExistException If record with the given id does not exist
     * @throws IllegalArgumentException
     * @throws NullPointerException Above 2 exceptions are thrown if any of id, deviceId, firstName, lastName,
     *                              dateOfBirth, or phoneNumber are empty
     */
    public void update(Patient updatedRecord) {
        log.info("Updating Patient record {}", updatedRecord);
        Validator.validatePatient(updatedRecord);
        Validator.validatePid(updatedRecord.getPid());
        Validator.validateSid(updatedRecord.getSid());
        Validator.validatePidEqualsSid(updatedRecord.getPid(), updatedRecord.getSid());
        Validator.validateDeviceId(updatedRecord.getDeviceId());

        Patient found = findByDeviceId(updatedRecord.getDeviceId());
        if (found != null && !found.getPid().equals(updatedRecord.getPid())) {
            throw new DuplicateRecordException(Patient.class.getSimpleName(), updatedRecord.getDeviceId());
        }

        try {
            genericDao.update(Patient.convertToMap(updatedRecord));
        } catch (ConditionalCheckFailedException e) {
            throw new RecordDoesNotExistException(Patient.class.getSimpleName(), updatedRecord.getPid());
        }
    }

    /**
     * Deletes a Patient record by id. Does nothing if record does not exist.
     *
     * @param id The id of the record to delete
     * @throws IllegalArgumentException
     * @throws NullPointerException Above 2 exceptions are thrown if id is empty
     */
    public void delete(String id) {
        log.info("Deleting Patient record with id [{}]", id);
        Validator.validateId(id);

        genericDao.delete(id);

        // TODO: delete caregiver associations
    }
}
