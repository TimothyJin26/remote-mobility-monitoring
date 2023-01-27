package com.cpen491.remote_mobility_monitoring.datastore.dao;

import com.cpen491.remote_mobility_monitoring.datastore.exception.DuplicateRecordException;
import com.cpen491.remote_mobility_monitoring.datastore.exception.RecordDoesNotExistException;
import com.cpen491.remote_mobility_monitoring.datastore.model.Organization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.stream.Stream;

import static com.cpen491.remote_mobility_monitoring.TestUtils.assertInvalidInputExceptionThrown;
import static com.cpen491.remote_mobility_monitoring.TestUtils.buildOrganization;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.ID_BLANK_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.ORGANIZATION_RECORD_NULL_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.NAME_BLANK_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.PID_BLANK_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.PID_NOT_EQUAL_SID_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.SID_BLANK_ERROR_MESSAGE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.junit.jupiter.api.Assertions.assertNull;

public class OrganizationDaoTest extends DaoTestParent {
    private static final String PID = "org-1";
    private static final String SID = PID;
    private static final String NAME1 = "ORG1";
    private static final String NAME2 = "ORG2";
    private static final String CREATED_AT = "2020-01-01T05:00:00.000000";

    OrganizationDao cut;

    @BeforeEach
    public void setup() {
        setupTable();
        cut = new OrganizationDao(new GenericDao(ddbClient));
    }

    @AfterEach
    public void teardown() {
        teardownTable();
    }

    @Test
    public void testCreate_HappyCase() {
        Organization newRecord = buildOrganizationDefault();
        cut.create(newRecord);

        assertNotEquals(PID, newRecord.getPid());
        assertNotEquals(SID, newRecord.getSid());
        assertEquals(NAME1, newRecord.getName());
        assertNotNull(newRecord.getCreatedAt());
        assertNotNull(newRecord.getUpdatedAt());
    }

    @Test
    public void testCreate_WHEN_RecordWithNameAlreadyExists_THEN_ThrowDuplicateRecordException() {
        Organization newRecord = buildOrganizationDefault();
        cut.create(newRecord);
        assertThatThrownBy(() -> cut.create(newRecord)).isInstanceOf(DuplicateRecordException.class);
    }

    @ParameterizedTest
    @MethodSource("invalidInputsForCreate")
    public void testCreate_WHEN_InvalidInput_THEN_ThrowInvalidInputException(Organization record, String errorMessage) {
        assertInvalidInputExceptionThrown(() -> cut.create(record), errorMessage);
    }

    private static Stream<Arguments> invalidInputsForCreate() {
        return Stream.of(
                Arguments.of(null, ORGANIZATION_RECORD_NULL_ERROR_MESSAGE),
                Arguments.of(buildOrganization(PID, SID, null), NAME_BLANK_ERROR_MESSAGE),
                Arguments.of(buildOrganization(PID, SID, ""), NAME_BLANK_ERROR_MESSAGE)
        );
    }

    @Test
    public void testFindById_HappyCase() {
        Organization newRecord = buildOrganizationDefault();
        createOrganization(newRecord);

        Organization found = cut.findById(newRecord.getPid());
        assertEquals(newRecord, found);
    }

    @Test
    public void testFindById_WHEN_RecordDoesNotExist_THEN_ThrowRecordDoesNotExistException() {
        assertThatThrownBy(() -> cut.findById(PID)).isInstanceOf(RecordDoesNotExistException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void testFindById_WHEN_InvalidInput_THEN_ThrowInvalidInputException(String id) {
        assertInvalidInputExceptionThrown(() -> cut.findById(id), ID_BLANK_ERROR_MESSAGE);
    }

    @Test
    public void testFindByName_HappyCase() {
        Organization newRecord = buildOrganizationDefault();
        createOrganization(newRecord);

        Organization found = cut.findByName(NAME1);
        assertEquals(newRecord, found);
    }

    @Test
    public void testFindByName_WHEN_RecordDoesNotExist_THEN_ReturnNull() {
        Organization found = cut.findByName(NAME1);
        assertNull(found);
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void testFindByName_WHEN_InvalidInput_THEN_ThrowInvalidInputException(String name) {
        assertInvalidInputExceptionThrown(() -> cut.findByName(name), NAME_BLANK_ERROR_MESSAGE);
    }

    @Test
    public void testUpdate_HappyCase() {
        Organization newRecord = buildOrganizationDefault();
        cut.create(newRecord);

        Organization updatedRecord = cut.findById(newRecord.getPid());
        assertEquals(newRecord, updatedRecord);
        updatedRecord.setName(NAME2);
        cut.update(updatedRecord);

        Organization found = cut.findById(newRecord.getPid());
        assertEquals(newRecord.getPid(), found.getPid());
        assertNotEquals(newRecord.getName(), found.getName());
        assertNotEquals(newRecord.getUpdatedAt(), found.getUpdatedAt());
        assertEquals(newRecord.getCreatedAt(), found.getCreatedAt());
    }

    @Test
    public void testUpdate_WHEN_RecordWithNameAlreadyExists_THEN_ThrowDuplicateRecordException() {
        Organization newRecord1 = buildOrganizationDefault();
        cut.create(newRecord1);
        Organization newRecord2 = buildOrganizationDefault();
        newRecord2.setName(NAME2);
        cut.create(newRecord2);

        Organization updatedRecord = cut.findById(newRecord2.getPid());
        updatedRecord.setName(NAME1);
        assertThatThrownBy(() -> cut.update(updatedRecord)).isInstanceOf(DuplicateRecordException.class);
    }

    @Test
    public void testUpdate_WHEN_RecordDoesNotExist_THEN_ThrowRecordDoesNotExistException() {
        Organization newRecord = buildOrganizationDefault();
        assertThatThrownBy(() -> cut.update(newRecord)).isInstanceOf(RecordDoesNotExistException.class);
    }

    @ParameterizedTest
    @MethodSource("invalidInputsForUpdate")
    public void testUpdate_WHEN_InvalidInput_THEN_ThrowInvalidInputException(Organization record, String errorMessage) {
        assertInvalidInputExceptionThrown(() -> cut.update(record), errorMessage);
    }

    private static Stream<Arguments> invalidInputsForUpdate() {
        return Stream.of(
                Arguments.of(null, ORGANIZATION_RECORD_NULL_ERROR_MESSAGE),
                Arguments.of(buildOrganization(null, SID, NAME1), PID_BLANK_ERROR_MESSAGE),
                Arguments.of(buildOrganization("", SID, NAME1), PID_BLANK_ERROR_MESSAGE),
                Arguments.of(buildOrganization(PID, null, NAME1), SID_BLANK_ERROR_MESSAGE),
                Arguments.of(buildOrganization(PID, "", NAME1), SID_BLANK_ERROR_MESSAGE),
                Arguments.of(buildOrganization(PID, SID, null), NAME_BLANK_ERROR_MESSAGE),
                Arguments.of(buildOrganization(PID, SID, ""), NAME_BLANK_ERROR_MESSAGE),
                Arguments.of(buildOrganization(PID, SID + "1", NAME1), PID_NOT_EQUAL_SID_ERROR_MESSAGE)
        );
    }

    @Test
    public void testDelete_HappyCase() {
        Organization newRecord = buildOrganizationDefault();
        createOrganization(newRecord);
        Organization found = cut.findById(newRecord.getPid());
        assertNotNull(found);

        cut.delete(newRecord.getPid());
        assertThatThrownBy(() -> cut.findById(newRecord.getPid())).isInstanceOf(RecordDoesNotExistException.class);
    }

    @Test
    public void testDelete_WHEN_RecordDoesNotExist_THEN_DoNothing() {
        assertDoesNotThrow(() -> cut.delete(PID));
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void testDelete_WHEN_InvalidInput_THEN_ThrowInvalidInputException(String id) {
        assertInvalidInputExceptionThrown(() -> cut.delete(id), ID_BLANK_ERROR_MESSAGE);
    }

    private static Organization buildOrganizationDefault() {
        return buildOrganization(PID, SID, NAME1);
    }
}
