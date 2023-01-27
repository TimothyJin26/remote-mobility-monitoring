package com.cpen491.remote_mobility_monitoring.datastore.dao;

import com.cpen491.remote_mobility_monitoring.datastore.exception.DuplicateRecordException;
import com.cpen491.remote_mobility_monitoring.datastore.exception.RecordDoesNotExistException;
import com.cpen491.remote_mobility_monitoring.datastore.model.Admin;
import com.cpen491.remote_mobility_monitoring.datastore.model.Organization;
import com.cpen491.remote_mobility_monitoring.dependency.utility.Validator;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.Map;

import static com.cpen491.remote_mobility_monitoring.datastore.model.Const.AdminTable;

@Slf4j
@AllArgsConstructor
public class AdminDao {
    @NonNull
    GenericDao genericDao;
    @NonNull
    OrganizationDao organizationDao;

    /**
     * Creates a new Admin record and adds it to an organization. Record with the given email must not already exist.
     *
     * @param newRecord The Admin record to create
     * @param organizationId The id of the Organization record
     * @throws RecordDoesNotExistException If Organization record with given organizationId does not exist
     * @throws DuplicateRecordException If record with the given email already exists
     * @throws IllegalArgumentException
     * @throws NullPointerException Above 2 exceptions are thrown if any of email, firstName, or lastName are empty
     */
    public void create(Admin newRecord, String organizationId) {
        log.info("Creating new Admin record {}", newRecord);
        Validator.validateAdmin(newRecord);

        Organization organization = organizationDao.findById(organizationId);

        if (findByEmail(newRecord.getEmail()) != null) {
            throw new DuplicateRecordException(Admin.class.getSimpleName(), newRecord.getEmail());
        }

        // TODO: test whether actually added association
        GenericDao.setId(newRecord, AdminTable.ID_PREFIX);
        Map<String, AttributeValue> adminMap = Admin.convertToMap(newRecord);
        genericDao.create(adminMap);
        genericDao.addAssociation(Organization.convertToMap(organization), adminMap);
    }

    /**
     * Finds an Admin record by id.
     *
     * @param id The id of the record to find
     * @return {@link Admin}
     * @throws RecordDoesNotExistException If record with the given id does not exist
     * @throws IllegalArgumentException
     * @throws NullPointerException Above 2 exceptions are thrown if id is empty
     */
    public Admin findById(String id) {
        log.info("Finding Admin record with id [{}]", id);
        Validator.validateId(id);

        GetItemResponse response = genericDao.findByPartitionKey(id);
        if (!response.hasItem()) {
            log.error("Cannot find Admin record with id [{}]", id);
            throw new RecordDoesNotExistException(Admin.class.getSimpleName(), id);
        }
        return Admin.convertFromMap(response.item());
    }

    /**
     * Finds an Admin record by email. Returns null if record does not exist.
     *
     * @param email The email of the record to find
     * @return {@link Admin}
     * @throws IllegalArgumentException
     * @throws NullPointerException Above 2 exceptions are thrown if email is empty
     */
    public Admin findByEmail(String email) {
        log.info("Finding Admin record with email [{}]", email);
        Validator.validateEmail(email);

        QueryResponse response = genericDao
                .findAllByIndexPartitionKey(AdminTable.EMAIL_INDEX_NAME, AdminTable.EMAIL_NAME, email);
        if (!response.hasItems() || response.items().size() == 0) {
            log.info("Cannot find Admin record with email [{}]", email);
            return null;
        }
        return Admin.convertFromMap(response.items().get(0));
    }

    /**
     * Updates an Admin record. Record with given id must already exist.
     * Record with given email should not already exist unless it is the same record being updated.
     *
     * @param updatedRecord The Admin record to update
     * @throws DuplicateRecordException If record with the given email already exists
     * @throws RecordDoesNotExistException If record with the given id does not exist
     * @throws IllegalArgumentException
     * @throws NullPointerException Above 2 exceptions are thrown if any of pid, sid, email,
     *                              firstName, or lastName are empty
     */
    public void update(Admin updatedRecord) {
        log.info("Updating Admin record {}", updatedRecord);
        Validator.validateAdmin(updatedRecord);
        Validator.validatePid(updatedRecord.getPid());
        Validator.validateSid(updatedRecord.getSid());
        Validator.validatePidEqualsSid(updatedRecord.getPid(), updatedRecord.getSid());

        Admin found = findByEmail(updatedRecord.getEmail());
        if (found != null && !found.getPid().equals(updatedRecord.getPid())) {
            throw new DuplicateRecordException(Admin.class.getSimpleName(), updatedRecord.getEmail());
        }

        try {
            genericDao.update(Admin.convertToMap(updatedRecord));
        } catch (ConditionalCheckFailedException e) {
            throw new RecordDoesNotExistException(Admin.class.getSimpleName(), updatedRecord.getPid());
        }
    }

    /**
     * Deletes an Admin record by id. Does nothing if record does not exist.
     *
     * @param id The id of the record to delete
     * @throws IllegalArgumentException
     * @throws NullPointerException Above 2 exceptions are thrown if id is empty
     */
    public void delete(String id) {
        log.info("Deleting Admin record with id [{}]", id);
        Validator.validateId(id);

        genericDao.delete(id);
    }
}
