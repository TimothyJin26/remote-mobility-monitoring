package com.cpen491.remote_mobility_monitoring.function.service;

import com.cpen491.remote_mobility_monitoring.datastore.dao.CaregiverDao;
import com.cpen491.remote_mobility_monitoring.datastore.dao.MetricsDao;
import com.cpen491.remote_mobility_monitoring.datastore.dao.PatientDao;
import com.cpen491.remote_mobility_monitoring.datastore.exception.RecordDoesNotExistException;
import com.cpen491.remote_mobility_monitoring.datastore.model.Caregiver;
import com.cpen491.remote_mobility_monitoring.datastore.model.Metrics;
import com.cpen491.remote_mobility_monitoring.datastore.model.Metrics.MeasureName;
import com.cpen491.remote_mobility_monitoring.datastore.model.Patient;
import com.cpen491.remote_mobility_monitoring.dependency.exception.InvalidAuthCodeException;
import com.cpen491.remote_mobility_monitoring.function.schema.patient.AddMetricsRequestBody;
import com.cpen491.remote_mobility_monitoring.function.schema.patient.AddMetricsRequestBody.AddMetricsSerialization;
import com.cpen491.remote_mobility_monitoring.function.schema.patient.AddMetricsResponseBody;
import com.cpen491.remote_mobility_monitoring.function.schema.patient.CreatePatientRequestBody;
import com.cpen491.remote_mobility_monitoring.function.schema.patient.CreatePatientResponseBody;
import com.cpen491.remote_mobility_monitoring.function.schema.patient.DeletePatientRequestBody;
import com.cpen491.remote_mobility_monitoring.function.schema.patient.DeletePatientResponseBody;
import com.cpen491.remote_mobility_monitoring.function.schema.patient.GetAllCaregiversRequestBody;
import com.cpen491.remote_mobility_monitoring.function.schema.patient.GetAllCaregiversResponseBody;
import com.cpen491.remote_mobility_monitoring.function.schema.patient.GetAllCaregiversResponseBody.CaregiverSerialization;
import com.cpen491.remote_mobility_monitoring.function.schema.patient.GetPatientRequestBody;
import com.cpen491.remote_mobility_monitoring.function.schema.patient.GetPatientResponseBody;
import com.cpen491.remote_mobility_monitoring.function.schema.patient.QueryMetricsRequestBody;
import com.cpen491.remote_mobility_monitoring.function.schema.patient.QueryMetricsResponseBody;
import com.cpen491.remote_mobility_monitoring.function.schema.patient.QueryMetricsResponseBody.QueryMetricsSerialization;
import com.cpen491.remote_mobility_monitoring.function.schema.patient.UpdatePatientDeviceRequestBody;
import com.cpen491.remote_mobility_monitoring.function.schema.patient.UpdatePatientDeviceResponseBody;
import com.cpen491.remote_mobility_monitoring.function.schema.patient.UpdatePatientRequestBody;
import com.cpen491.remote_mobility_monitoring.function.schema.patient.UpdatePatientResponseBody;
import com.cpen491.remote_mobility_monitoring.function.schema.patient.VerifyPatientRequestBody;
import com.cpen491.remote_mobility_monitoring.function.schema.patient.VerifyPatientResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.cpen491.remote_mobility_monitoring.TestUtils.assertInvalidInputExceptionThrown;
import static com.cpen491.remote_mobility_monitoring.TestUtils.buildCaregiver;
import static com.cpen491.remote_mobility_monitoring.TestUtils.buildMetrics;
import static com.cpen491.remote_mobility_monitoring.TestUtils.buildPatient;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.TimeUtils.getCurrentUtcTime;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.TimeUtils.getCurrentUtcTimeString;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.ADD_METRICS_NULL_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.AUTH_CODE_BLANK_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.CAREGIVER_ID_BLANK_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.CAREGIVER_ID_INVALID_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.CREATE_PATIENT_NULL_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.DELETE_PATIENT_NULL_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.DEVICE_ID_BLANK_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.DISTANCE_WALKED_BLANK_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.DISTANCE_WALKED_INVALID_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.DOUBLE_SUPPORT_TIME_BLANK_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.DOUBLE_SUPPORT_TIME_INVALID_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.FIRST_NAME_BLANK_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.GET_ALL_CAREGIVERS_NULL_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.GET_PATIENT_NULL_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.IDS_NULL_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.LAST_NAME_BLANK_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.METRICS_NULL_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.PATIENT_ID_BLANK_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.PATIENT_ID_INVALID_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.PHONE_NUMBER_BLANK_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.QUERY_METRICS_NULL_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.STEP_LENGTH_BLANK_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.STEP_LENGTH_INVALID_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.TIMESTAMP_BLANK_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.TIMESTAMP_INVALID_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.UPDATE_PATIENT_DEVICE_NULL_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.UPDATE_PATIENT_NULL_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.VERIFY_PATIENT_NULL_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.WALKING_ASYMMETRY_BLANK_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.WALKING_ASYMMETRY_INVALID_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.WALKING_SPEED_BLANK_ERROR_MESSAGE;
import static com.cpen491.remote_mobility_monitoring.dependency.utility.Validator.WALKING_SPEED_INVALID_ERROR_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {
    private static final String EMAIL = "jackjackson@email.com";
    private static final String TITLE = "caregiver";
    private static final String FIRST_NAME = "Jack";
    private static final String LAST_NAME = "Jackson";
    private static final String PHONE_NUMBER1 = "1234567890";
    private static final String PHONE_NUMBER2 = "1313131313";
    private static final String PATIENT_ID = "pat-1";
    private static final String PATIENT_ID2 = "pat-2";
    private static final List<String> PATIENT_IDS = Arrays.asList(PATIENT_ID, PATIENT_ID2);
    private static final String CAREGIVER_ID1 = "car-1";
    private static final String CAREGIVER_ID2 = "car-2";
    private static final String AUTH_CODE = "auth_code-123";
    private static final String AUTH_CODE_TIMESTAMP = getCurrentUtcTimeString();
    private static final String DEVICE_ID = "device-id-1";
    private static final String METRIC_VALUE = "0.0";
    private static final String INVALID_METRIC_VALUE = "0.0%";
    private static final String[] METRIC_VALUES1 = new String[]{"1.0", "2.0", "3.0", "4.0", "5.0"};
    private static final String[] METRIC_VALUES2 = new String[]{"6.0", "7.0", "8.0", "9.0", "10.0"};
    private static final String TIMESTAMP = getCurrentUtcTimeString();
    private static final String INVALID_TIMESTAMP = "2023-02-01 12:00:00";
    private static final String CREATED_AT = "2023-01-01";

    PatientService cut;
    @Mock
    PatientDao patientDao;
    @Mock
    CaregiverDao caregiverDao;
    @Mock
    MetricsDao metricsDao;
    ArgumentCaptor<Patient> patientCaptor;
    ArgumentCaptor<Caregiver> caregiverCaptor;
    @Captor
    ArgumentCaptor<List<Metrics>> metricsListCaptor;

    @BeforeEach
    public void setup() {
        patientCaptor = ArgumentCaptor.forClass(Patient.class);
        caregiverCaptor = ArgumentCaptor.forClass(Caregiver.class);
        cut = new PatientService(patientDao, caregiverDao, metricsDao);
    }

    @Test
    public void testCreatePatient_HappyCase() {
        CreatePatientRequestBody requestBody = buildCreatePatientRequestBody();
        CreatePatientResponseBody responseBody = cut.createPatient(requestBody);

        verify(patientDao, times(1)).create(patientCaptor.capture());
        assertEquals(FIRST_NAME, patientCaptor.getValue().getFirstName());
        assertEquals(LAST_NAME, patientCaptor.getValue().getLastName());
        assertEquals(PHONE_NUMBER1, patientCaptor.getValue().getPhoneNumber());
        assertNotEquals(AUTH_CODE, patientCaptor.getValue().getAuthCode());
        assertNotEquals(AUTH_CODE_TIMESTAMP, patientCaptor.getValue().getAuthCodeTimestamp());
        assertNotNull(responseBody);
        assertEquals(responseBody.getAuthCode(), patientCaptor.getValue().getAuthCode());
    }

    @Test
    public void testCreatePatient_WHEN_PatientDaoCreateThrows_THEN_ThrowSameException() {
        NullPointerException toThrow = new NullPointerException();
        Mockito.doThrow(toThrow).when(patientDao).create(any(Patient.class));

        CreatePatientRequestBody requestBody = buildCreatePatientRequestBody();
        assertThatThrownBy(() -> cut.createPatient(requestBody)).isSameAs(toThrow);
    }

    @ParameterizedTest
    @MethodSource("invalidInputsForCreatePatient")
    public void testCreatePatient_WHEN_InvalidInput_THEN_ThrowInvalidInputException(CreatePatientRequestBody body, String errorMessage) {
        assertInvalidInputExceptionThrown(() -> cut.createPatient(body), errorMessage);
    }

    private static Stream<Arguments> invalidInputsForCreatePatient() {
        return Stream.of(
                Arguments.of(null, CREATE_PATIENT_NULL_ERROR_MESSAGE),
                Arguments.of(buildCreatePatientRequestBody(null, LAST_NAME, PHONE_NUMBER1), FIRST_NAME_BLANK_ERROR_MESSAGE),
                Arguments.of(buildCreatePatientRequestBody("", LAST_NAME, PHONE_NUMBER1), FIRST_NAME_BLANK_ERROR_MESSAGE),
                Arguments.of(buildCreatePatientRequestBody(FIRST_NAME, null, PHONE_NUMBER1), LAST_NAME_BLANK_ERROR_MESSAGE),
                Arguments.of(buildCreatePatientRequestBody(FIRST_NAME, "", PHONE_NUMBER1), LAST_NAME_BLANK_ERROR_MESSAGE),
                Arguments.of(buildCreatePatientRequestBody(FIRST_NAME, LAST_NAME, null), PHONE_NUMBER_BLANK_ERROR_MESSAGE),
                Arguments.of(buildCreatePatientRequestBody(FIRST_NAME, LAST_NAME, ""), PHONE_NUMBER_BLANK_ERROR_MESSAGE)
        );
    }

    @Test
    public void testUpdatePatientDevice_HappyCase() {
        when(patientDao.findById(anyString())).thenReturn(buildPatientDefault());

        UpdatePatientDeviceRequestBody requestBody = buildUpdatePatientDeviceRequestBody();
        UpdatePatientDeviceResponseBody responseBody = cut.updatePatientDevice(requestBody);

        verify(patientDao, times(1)).update(patientCaptor.capture());
        assertNotEquals(AUTH_CODE, patientCaptor.getValue().getAuthCode());
        assertNotEquals(AUTH_CODE_TIMESTAMP, patientCaptor.getValue().getAuthCodeTimestamp());
        assertNotNull(responseBody);
        assertEquals(responseBody.getAuthCode(), patientCaptor.getValue().getAuthCode());
    }

    @Test
    public void testUpdatePatientDevice_WHEN_PatientDaoFindByIdThrows_THEN_ThrowSameException() {
        NullPointerException toThrow = new NullPointerException();
        Mockito.doThrow(toThrow).when(patientDao).findById(anyString());

        UpdatePatientDeviceRequestBody requestBody = buildUpdatePatientDeviceRequestBody();
        assertThatThrownBy(() -> cut.updatePatientDevice(requestBody)).isSameAs(toThrow);
    }

    @Test
    public void testUpdatePatientDevice_WHEN_PatientDaoUpdateThrows_THEN_ThrowSameException() {
        when(patientDao.findById(anyString())).thenReturn(buildPatientDefault());

        NullPointerException toThrow = new NullPointerException();
        Mockito.doThrow(toThrow).when(patientDao).update(any(Patient.class));

        UpdatePatientDeviceRequestBody requestBody = buildUpdatePatientDeviceRequestBody();
        assertThatThrownBy(() -> cut.updatePatientDevice(requestBody)).isSameAs(toThrow);
    }

    @ParameterizedTest
    @MethodSource("invalidInputsForUpdatePatientDevice")
    public void testUpdatePatientDevice_WHEN_InvalidInput_THEN_ThrowInvalidInputException(UpdatePatientDeviceRequestBody body, String errorMessage) {
        assertInvalidInputExceptionThrown(() -> cut.updatePatientDevice(body), errorMessage);
    }

    private static Stream<Arguments> invalidInputsForUpdatePatientDevice() {
        return Stream.of(
                Arguments.of(null, UPDATE_PATIENT_DEVICE_NULL_ERROR_MESSAGE),
                Arguments.of(buildUpdatePatientDeviceRequestBody(null), PATIENT_ID_BLANK_ERROR_MESSAGE),
                Arguments.of(buildUpdatePatientDeviceRequestBody(""), PATIENT_ID_BLANK_ERROR_MESSAGE),
                Arguments.of(buildUpdatePatientDeviceRequestBody(CAREGIVER_ID1), PATIENT_ID_INVALID_ERROR_MESSAGE)
        );
    }

    @Test
    public void testVerifyPatient_HappyCase() {
        when(patientDao.findById(anyString())).thenReturn(buildPatientDefault());

        VerifyPatientRequestBody requestBody = buildVerifyPatientRequestBody();
        VerifyPatientResponseBody responseBody = cut.verifyPatient(requestBody);

        verify(patientDao, times(1)).update(patientCaptor.capture());
        assertTrue(patientCaptor.getValue().getVerified());
        assertEquals(DEVICE_ID, patientCaptor.getValue().getDeviceId());

        verify(caregiverDao, times(1)).addPatient(eq(PATIENT_ID), eq(CAREGIVER_ID1));

        assertNotNull(responseBody);
        assertEquals("OK", responseBody.getMessage());
    }

    @Test
    public void testVerifyPatient_WHEN_PatientDaoFindByIdThrows_THEN_ThrowSameException() {
        NullPointerException toThrow = new NullPointerException();
        Mockito.doThrow(toThrow).when(patientDao).findById(anyString());

        VerifyPatientRequestBody requestBody = buildVerifyPatientRequestBody();
        assertThatThrownBy(() -> cut.verifyPatient(requestBody)).isSameAs(toThrow);
    }

    @Test
    public void testVerifyPatient_WHEN_CaregiverDaoFindByIdThrows_THEN_ThrowSameException() {
        when(patientDao.findById(anyString())).thenReturn(buildPatientDefault());

        NullPointerException toThrow = new NullPointerException();
        Mockito.doThrow(toThrow).when(caregiverDao).findById(anyString());

        VerifyPatientRequestBody requestBody = buildVerifyPatientRequestBody();
        assertThatThrownBy(() -> cut.verifyPatient(requestBody)).isSameAs(toThrow);
    }

    @Test
    public void testVerifyPatient_WHEN_PatientDaoUpdateThrows_THEN_ThrowSameException() {
        when(patientDao.findById(anyString())).thenReturn(buildPatientDefault());

        NullPointerException toThrow = new NullPointerException();
        Mockito.doThrow(toThrow).when(patientDao).update(any(Patient.class));

        VerifyPatientRequestBody requestBody = buildVerifyPatientRequestBody();
        assertThatThrownBy(() -> cut.verifyPatient(requestBody)).isSameAs(toThrow);
    }

    @Test
    public void testVerifyPatient_WHEN_AuthCodesDoNotMatch_THEN_ThrowInvalidAuthCodeException() {
        when(patientDao.findById(anyString())).thenReturn(buildPatientDefault());

        VerifyPatientRequestBody requestBody = buildVerifyPatientRequestBody();
        requestBody.setAuthCode(AUTH_CODE + "1");
        assertThatThrownBy(() -> cut.verifyPatient(requestBody)).isInstanceOf(InvalidAuthCodeException.class);
    }

    @Test
    public void testVerifyPatient_WHEN_AuthCodeExpired_THEN_ThrowInvalidAuthCodeException() {
        Patient patient = buildPatientDefault();
        String tenMinutesAgo = getCurrentUtcTime().minusMinutes(10).toString();
        patient.setAuthCodeTimestamp(tenMinutesAgo);
        when(patientDao.findById(anyString())).thenReturn(patient);

        VerifyPatientRequestBody requestBody = buildVerifyPatientRequestBody();
        assertThatThrownBy(() -> cut.verifyPatient(requestBody)).isInstanceOf(InvalidAuthCodeException.class);
    }

    @ParameterizedTest
    @MethodSource("invalidInputsForVerifyPatient")
    public void testVerifyPatient_WHEN_InvalidInput_THEN_ThrowInvalidInputException(VerifyPatientRequestBody body, String errorMessage) {
        assertInvalidInputExceptionThrown(() -> cut.verifyPatient(body), errorMessage);
    }

    private static Stream<Arguments> invalidInputsForVerifyPatient() {
        return Stream.of(
                Arguments.of(null, VERIFY_PATIENT_NULL_ERROR_MESSAGE),
                Arguments.of(buildVerifyPatientRequestBody(null, PATIENT_ID, AUTH_CODE, DEVICE_ID), CAREGIVER_ID_BLANK_ERROR_MESSAGE),
                Arguments.of(buildVerifyPatientRequestBody("", PATIENT_ID, AUTH_CODE, DEVICE_ID), CAREGIVER_ID_BLANK_ERROR_MESSAGE),
                Arguments.of(buildVerifyPatientRequestBody(PATIENT_ID, PATIENT_ID, AUTH_CODE, DEVICE_ID), CAREGIVER_ID_INVALID_ERROR_MESSAGE),
                Arguments.of(buildVerifyPatientRequestBody(CAREGIVER_ID1, null, AUTH_CODE, DEVICE_ID), PATIENT_ID_BLANK_ERROR_MESSAGE),
                Arguments.of(buildVerifyPatientRequestBody(CAREGIVER_ID1, "", AUTH_CODE, DEVICE_ID), PATIENT_ID_BLANK_ERROR_MESSAGE),
                Arguments.of(buildVerifyPatientRequestBody(CAREGIVER_ID1, CAREGIVER_ID1, AUTH_CODE, DEVICE_ID), PATIENT_ID_INVALID_ERROR_MESSAGE),
                Arguments.of(buildVerifyPatientRequestBody(CAREGIVER_ID1, PATIENT_ID, null, DEVICE_ID), AUTH_CODE_BLANK_ERROR_MESSAGE),
                Arguments.of(buildVerifyPatientRequestBody(CAREGIVER_ID1, PATIENT_ID, "", DEVICE_ID), AUTH_CODE_BLANK_ERROR_MESSAGE),
                Arguments.of(buildVerifyPatientRequestBody(CAREGIVER_ID1, PATIENT_ID, AUTH_CODE, null), DEVICE_ID_BLANK_ERROR_MESSAGE),
                Arguments.of(buildVerifyPatientRequestBody(CAREGIVER_ID1, PATIENT_ID, AUTH_CODE, ""), DEVICE_ID_BLANK_ERROR_MESSAGE)
        );
    }

    @Test
    public void testGetPatient_HappyCase() {
        when(patientDao.findById(anyString())).thenReturn(buildPatientDefault());

        GetPatientRequestBody requestBody = buildGetPatientRequestBody();
        GetPatientResponseBody responseBody = cut.getPatient(requestBody);

        assertNull(responseBody.getDeviceId());
        assertEquals(FIRST_NAME, responseBody.getFirstName());
        assertEquals(LAST_NAME, responseBody.getLastName());
        assertEquals(PHONE_NUMBER1, responseBody.getPhoneNumber());
        assertEquals(CREATED_AT, responseBody.getCreatedAt());
    }

    @Test
    public void testGetPatient_WHEN_PatientDaoFindByIdThrows_THEN_ThrowSameException() {
        NullPointerException toThrow = new NullPointerException();
        Mockito.doThrow(toThrow).when(patientDao).findById(anyString());

        GetPatientRequestBody requestBody = buildGetPatientRequestBody();
        assertThatThrownBy(() -> cut.getPatient(requestBody)).isSameAs(toThrow);
    }

    @ParameterizedTest
    @MethodSource("invalidInputsForGetPatient")
    public void testGetPatient_WHEN_InvalidInput_THEN_ThrowInvalidInputException(GetPatientRequestBody body, String errorMessage) {
        assertInvalidInputExceptionThrown(() -> cut.getPatient(body), errorMessage);
    }

    private static Stream<Arguments> invalidInputsForGetPatient() {
        return Stream.of(
                Arguments.of(null, GET_PATIENT_NULL_ERROR_MESSAGE),
                Arguments.of(buildGetPatientRequestBody(null), PATIENT_ID_BLANK_ERROR_MESSAGE),
                Arguments.of(buildGetPatientRequestBody(""), PATIENT_ID_BLANK_ERROR_MESSAGE),
                Arguments.of(buildGetPatientRequestBody(CAREGIVER_ID1), PATIENT_ID_INVALID_ERROR_MESSAGE)
        );
    }

    @Test
    public void testGetAllCaregivers_HappyCase() {
        Caregiver caregiver1 = buildCaregiverDefault();
        Caregiver caregiver2 = buildCaregiverDefault();
        caregiver2.setPid(CAREGIVER_ID2);
        caregiver2.setSid(CAREGIVER_ID2);
        List<Caregiver> caregivers = Arrays.asList(caregiver1, caregiver2);
        when(patientDao.findAllCaregivers(anyString())).thenReturn(caregivers);

        GetAllCaregiversRequestBody requestBody = buildGetAllCaregiversRequestBody();
        GetAllCaregiversResponseBody responseBody = cut.getAllCaregivers(requestBody);

        List<CaregiverSerialization> expected = caregivers.stream().map(CaregiverSerialization::fromCaregiver).collect(Collectors.toList());
        assertThat(responseBody.getCaregivers()).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testGetAllCaregivers_WHEN_PatientDaoFindByIdThrows_THEN_ThrowSameException() {
        NullPointerException toThrow = new NullPointerException();
        Mockito.doThrow(toThrow).when(patientDao).findById(anyString());

        GetAllCaregiversRequestBody requestBody = buildGetAllCaregiversRequestBody();
        assertThatThrownBy(() -> cut.getAllCaregivers(requestBody)).isSameAs(toThrow);
    }

    @Test
    public void testGetAllCaregivers_WHEN_PatientDaoFindAllCaregiversThrows_THEN_ThrowSameException() {
        NullPointerException toThrow = new NullPointerException();
        Mockito.doThrow(toThrow).when(patientDao).findAllCaregivers(anyString());

        GetAllCaregiversRequestBody requestBody = buildGetAllCaregiversRequestBody();
        assertThatThrownBy(() -> cut.getAllCaregivers(requestBody)).isSameAs(toThrow);
    }

    @ParameterizedTest
    @MethodSource("invalidInputsForGetAllCaregivers")
    public void testGetAllCaregivers_WHEN_InvalidInput_THEN_ThrowInvalidInputException(GetAllCaregiversRequestBody body, String errorMessage) {
        assertInvalidInputExceptionThrown(() -> cut.getAllCaregivers(body), errorMessage);
    }

    private static Stream<Arguments> invalidInputsForGetAllCaregivers() {
        return Stream.of(
                Arguments.of(null, GET_ALL_CAREGIVERS_NULL_ERROR_MESSAGE),
                Arguments.of(buildGetAllCaregiversRequestBody(null), PATIENT_ID_BLANK_ERROR_MESSAGE),
                Arguments.of(buildGetAllCaregiversRequestBody(""), PATIENT_ID_BLANK_ERROR_MESSAGE),
                Arguments.of(buildGetAllCaregiversRequestBody(CAREGIVER_ID1), PATIENT_ID_INVALID_ERROR_MESSAGE)
        );
    }

    @Test
    public void testAddMetrics_HappyCase() {
        when(patientDao.findByDeviceId(anyString())).thenReturn(buildPatientDefault());

        AddMetricsSerialization serialization1 = buildAddMetricsSerialization(METRIC_VALUES1);
        AddMetricsSerialization serialization2 = buildAddMetricsSerialization(METRIC_VALUES2);
        List<AddMetricsSerialization> serializations = Arrays.asList(serialization1, serialization2);
        AddMetricsRequestBody requestBody = buildAddMetricsRequestBody(serializations);
        AddMetricsResponseBody responseBody = cut.addMetrics(requestBody);

        List<Metrics> expected = new ArrayList<>();
        expected.add(buildMetricsDefault(MeasureName.STEP_LENGTH, METRIC_VALUES1[0]));
        expected.add(buildMetricsDefault(MeasureName.DOUBLE_SUPPORT_TIME, METRIC_VALUES1[1]));
        expected.add(buildMetricsDefault(MeasureName.WALKING_SPEED, METRIC_VALUES1[2]));
        expected.add(buildMetricsDefault(MeasureName.WALKING_ASYMMETRY, METRIC_VALUES1[3]));
        expected.add(buildMetricsDefault(MeasureName.DISTANCE_WALKED, METRIC_VALUES1[4]));
        expected.add(buildMetricsDefault(MeasureName.STEP_LENGTH, METRIC_VALUES2[0]));
        expected.add(buildMetricsDefault(MeasureName.DOUBLE_SUPPORT_TIME, METRIC_VALUES2[1]));
        expected.add(buildMetricsDefault(MeasureName.WALKING_SPEED, METRIC_VALUES2[2]));
        expected.add(buildMetricsDefault(MeasureName.WALKING_ASYMMETRY, METRIC_VALUES2[3]));
        expected.add(buildMetricsDefault(MeasureName.DISTANCE_WALKED, METRIC_VALUES2[4]));

        verify(metricsDao, times(1)).add(metricsListCaptor.capture());
        List<Metrics> metricsList = metricsListCaptor.getValue();
        assertThat(metricsList).containsExactlyInAnyOrderElementsOf(expected);
        assertEquals("OK", responseBody.getMessage());
    }

    @Test
    public void testAddMetrics_WHEN_PatientDaoFindByDeviceIdReturnsNull_THEN_ThrowRecordDoesNotExistException() {
        List<AddMetricsSerialization> serializations = Arrays.asList(buildAddMetricsSerialization(METRIC_VALUES1));
        AddMetricsRequestBody requestBody = buildAddMetricsRequestBody(serializations);
        assertThatThrownBy(() -> cut.addMetrics(requestBody)).isInstanceOf(RecordDoesNotExistException.class);
    }

    @Test
    public void testAddMetrics_WHEN_MetricsDaoAddThrows_THEN_ThrowSameException() {
        when(patientDao.findByDeviceId(anyString())).thenReturn(buildPatientDefault());

        NullPointerException toThrow = new NullPointerException();
        Mockito.doThrow(toThrow).when(metricsDao).add(anyList());

        List<AddMetricsSerialization> serializations = Arrays.asList(buildAddMetricsSerialization(METRIC_VALUES1));
        AddMetricsRequestBody requestBody = buildAddMetricsRequestBody(serializations);
        assertThatThrownBy(() -> cut.addMetrics(requestBody)).isSameAs(toThrow);
    }

    @ParameterizedTest
    @MethodSource("invalidInputsForAddMetrics1")
    public void testAddMetrics_WHEN_InvalidInput1_THEN_ThrowInvalidInputException(AddMetricsRequestBody body, String errorMessage) {
        assertInvalidInputExceptionThrown(() -> cut.addMetrics(body), errorMessage);
    }

    private static Stream<Arguments> invalidInputsForAddMetrics1() {
        AddMetricsSerialization validSerialization = buildAddMetricsSerialization(METRIC_VALUES1);
        List<AddMetricsSerialization> serializations = Collections.singletonList(validSerialization);
        return Stream.of(
                Arguments.of(null, ADD_METRICS_NULL_ERROR_MESSAGE),
                Arguments.of(buildAddMetricsRequestBody(null), METRICS_NULL_ERROR_MESSAGE),
                Arguments.of(buildAddMetricsRequestBody(null, serializations), DEVICE_ID_BLANK_ERROR_MESSAGE),
                Arguments.of(buildAddMetricsRequestBody("", serializations), DEVICE_ID_BLANK_ERROR_MESSAGE)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidInputsForAddMetrics2")
    public void testAddMetrics_WHEN_InvalidInput2_THEN_ThrowInvalidInputException(AddMetricsRequestBody body, String errorMessage) {
        when(patientDao.findByDeviceId(anyString())).thenReturn(buildPatientDefault());

        assertInvalidInputExceptionThrown(() -> cut.addMetrics(body), errorMessage);
    }

    private static Stream<Arguments> invalidInputsForAddMetrics2() {
        AddMetricsSerialization invalidSerialization1 = buildAddMetricsSerialization(null, METRIC_VALUE, METRIC_VALUE, METRIC_VALUE, METRIC_VALUE);
        List<AddMetricsSerialization> serializations1 = Collections.singletonList(invalidSerialization1);
        AddMetricsSerialization invalidSerialization2 = buildAddMetricsSerialization("", METRIC_VALUE, METRIC_VALUE, METRIC_VALUE, METRIC_VALUE);
        List<AddMetricsSerialization> serializations2 = Collections.singletonList(invalidSerialization2);
        AddMetricsSerialization invalidSerialization3 = buildAddMetricsSerialization(INVALID_METRIC_VALUE, METRIC_VALUE, METRIC_VALUE, METRIC_VALUE, METRIC_VALUE);
        List<AddMetricsSerialization> serializations3 = Collections.singletonList(invalidSerialization3);
        AddMetricsSerialization invalidSerialization4 = buildAddMetricsSerialization(METRIC_VALUE, null, METRIC_VALUE, METRIC_VALUE, METRIC_VALUE);
        List<AddMetricsSerialization> serializations4 = Collections.singletonList(invalidSerialization4);
        AddMetricsSerialization invalidSerialization5 = buildAddMetricsSerialization(METRIC_VALUE, "", METRIC_VALUE, METRIC_VALUE, METRIC_VALUE);
        List<AddMetricsSerialization> serializations5 = Collections.singletonList(invalidSerialization5);
        AddMetricsSerialization invalidSerialization6 = buildAddMetricsSerialization(METRIC_VALUE, INVALID_METRIC_VALUE, METRIC_VALUE, METRIC_VALUE, METRIC_VALUE);
        List<AddMetricsSerialization> serializations6 = Collections.singletonList(invalidSerialization6);
        AddMetricsSerialization invalidSerialization7 = buildAddMetricsSerialization(METRIC_VALUE, METRIC_VALUE, null, METRIC_VALUE, METRIC_VALUE);
        List<AddMetricsSerialization> serializations7 = Collections.singletonList(invalidSerialization7);
        AddMetricsSerialization invalidSerialization8 = buildAddMetricsSerialization(METRIC_VALUE, METRIC_VALUE, "", METRIC_VALUE, METRIC_VALUE);
        List<AddMetricsSerialization> serializations8 = Collections.singletonList(invalidSerialization8);
        AddMetricsSerialization invalidSerialization9 = buildAddMetricsSerialization(METRIC_VALUE, METRIC_VALUE, INVALID_METRIC_VALUE, METRIC_VALUE, METRIC_VALUE);
        List<AddMetricsSerialization> serializations9 = Collections.singletonList(invalidSerialization9);
        AddMetricsSerialization invalidSerialization10 = buildAddMetricsSerialization(METRIC_VALUE, METRIC_VALUE, METRIC_VALUE, null, METRIC_VALUE);
        List<AddMetricsSerialization> serializations10 = Collections.singletonList(invalidSerialization10);
        AddMetricsSerialization invalidSerialization11 = buildAddMetricsSerialization(METRIC_VALUE, METRIC_VALUE, METRIC_VALUE, "", METRIC_VALUE);
        List<AddMetricsSerialization> serializations11 = Collections.singletonList(invalidSerialization11);
        AddMetricsSerialization invalidSerialization12 = buildAddMetricsSerialization(METRIC_VALUE, METRIC_VALUE, METRIC_VALUE, INVALID_METRIC_VALUE, METRIC_VALUE);
        List<AddMetricsSerialization> serializations12 = Collections.singletonList(invalidSerialization12);
        AddMetricsSerialization invalidSerialization13 = buildAddMetricsSerialization(METRIC_VALUE, METRIC_VALUE, METRIC_VALUE, METRIC_VALUE, null);
        List<AddMetricsSerialization> serializations13 = Collections.singletonList(invalidSerialization13);
        AddMetricsSerialization invalidSerialization14 = buildAddMetricsSerialization(METRIC_VALUE, METRIC_VALUE, METRIC_VALUE, METRIC_VALUE, "");
        List<AddMetricsSerialization> serializations14 = Collections.singletonList(invalidSerialization14);
        AddMetricsSerialization invalidSerialization15 = buildAddMetricsSerialization(METRIC_VALUE, METRIC_VALUE, METRIC_VALUE, METRIC_VALUE, INVALID_METRIC_VALUE);
        List<AddMetricsSerialization> serializations15 = Collections.singletonList(invalidSerialization15);
        List<AddMetricsSerialization> serializations16 = Collections.singletonList(null);
        return Stream.of(
                Arguments.of(buildAddMetricsRequestBody(serializations1), STEP_LENGTH_BLANK_ERROR_MESSAGE),
                Arguments.of(buildAddMetricsRequestBody(serializations2), STEP_LENGTH_BLANK_ERROR_MESSAGE),
                Arguments.of(buildAddMetricsRequestBody(serializations3), STEP_LENGTH_INVALID_ERROR_MESSAGE),
                Arguments.of(buildAddMetricsRequestBody(serializations4), DOUBLE_SUPPORT_TIME_BLANK_ERROR_MESSAGE),
                Arguments.of(buildAddMetricsRequestBody(serializations5), DOUBLE_SUPPORT_TIME_BLANK_ERROR_MESSAGE),
                Arguments.of(buildAddMetricsRequestBody(serializations6), DOUBLE_SUPPORT_TIME_INVALID_ERROR_MESSAGE),
                Arguments.of(buildAddMetricsRequestBody(serializations7), WALKING_SPEED_BLANK_ERROR_MESSAGE),
                Arguments.of(buildAddMetricsRequestBody(serializations8), WALKING_SPEED_BLANK_ERROR_MESSAGE),
                Arguments.of(buildAddMetricsRequestBody(serializations9), WALKING_SPEED_INVALID_ERROR_MESSAGE),
                Arguments.of(buildAddMetricsRequestBody(serializations10), WALKING_ASYMMETRY_BLANK_ERROR_MESSAGE),
                Arguments.of(buildAddMetricsRequestBody(serializations11), WALKING_ASYMMETRY_BLANK_ERROR_MESSAGE),
                Arguments.of(buildAddMetricsRequestBody(serializations12), WALKING_ASYMMETRY_INVALID_ERROR_MESSAGE),
                Arguments.of(buildAddMetricsRequestBody(serializations13), DISTANCE_WALKED_BLANK_ERROR_MESSAGE),
                Arguments.of(buildAddMetricsRequestBody(serializations14), DISTANCE_WALKED_BLANK_ERROR_MESSAGE),
                Arguments.of(buildAddMetricsRequestBody(serializations15), DISTANCE_WALKED_INVALID_ERROR_MESSAGE),
                Arguments.of(buildAddMetricsRequestBody(serializations16), METRICS_NULL_ERROR_MESSAGE)
        );
    }

    @Test
    public void testQueryMetrics_HappyCase() {
        Metrics metrics1 = buildMetricsDefault(MeasureName.STEP_LENGTH, METRIC_VALUES1[0]);
        Metrics metrics2 = buildMetricsDefault(MeasureName.DOUBLE_SUPPORT_TIME, METRIC_VALUES1[1]);
        Metrics metrics3 = buildMetricsDefault(MeasureName.WALKING_SPEED, METRIC_VALUES1[2]);
        metrics3.setPatientId(PATIENT_ID2);
        List<Metrics> metricsList = Arrays.asList(metrics1, metrics2, metrics3);
        when(metricsDao.query(anyList(), anyString(), anyString())).thenReturn(metricsList);

        QueryMetricsRequestBody requestBody = buildQueryMetricsRequestBody();
        QueryMetricsResponseBody responseBody = cut.queryMetrics(requestBody);

        List<QueryMetricsSerialization> expected = new ArrayList<>();
        expected.add(buildQueryMetricsSerialization(PATIENT_ID, MeasureName.STEP_LENGTH.type, METRIC_VALUES1[0]));
        expected.add(buildQueryMetricsSerialization(PATIENT_ID, MeasureName.DOUBLE_SUPPORT_TIME.type, METRIC_VALUES1[1]));
        expected.add(buildQueryMetricsSerialization(PATIENT_ID2, MeasureName.WALKING_SPEED.type, METRIC_VALUES1[2]));
        assertThat(responseBody.getMetrics()).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testQueryMetrics_WHEN_MetricsDaoQueryThrows_THEN_ThrowSameException() {
        NullPointerException toThrow = new NullPointerException();
        Mockito.doThrow(toThrow).when(metricsDao).query(anyList(), anyString(), anyString());

        QueryMetricsRequestBody requestBody = buildQueryMetricsRequestBody();
        assertThatThrownBy(() -> cut.queryMetrics(requestBody)).isSameAs(toThrow);
    }

    @ParameterizedTest
    @MethodSource("invalidInputsForQueryMetrics")
    public void testQueryMetrics_WHEN_InvalidInput_THEN_ThrowInvalidInputException(QueryMetricsRequestBody body, String errorMessage) {
        assertInvalidInputExceptionThrown(() -> cut.queryMetrics(body), errorMessage);
    }

    private static Stream<Arguments> invalidInputsForQueryMetrics() {
        List<String> ids2 = new ArrayList<>();
        ids2.add(null);
        List<String> ids3 = new ArrayList<>();
        ids3.add("");
        List<String> ids4 = new ArrayList<>();
        ids4.add(DEVICE_ID);
        List<String> ids5 = new ArrayList<>();
        ids5.add(PATIENT_ID);
        return Stream.of(
                Arguments.of(null, QUERY_METRICS_NULL_ERROR_MESSAGE),
                Arguments.of(buildQueryMetricsRequestBody(null, TIMESTAMP, TIMESTAMP), IDS_NULL_ERROR_MESSAGE),
                Arguments.of(buildQueryMetricsRequestBody(ids2, TIMESTAMP, TIMESTAMP), PATIENT_ID_BLANK_ERROR_MESSAGE),
                Arguments.of(buildQueryMetricsRequestBody(ids3, TIMESTAMP, TIMESTAMP), PATIENT_ID_BLANK_ERROR_MESSAGE),
                Arguments.of(buildQueryMetricsRequestBody(ids4, TIMESTAMP, TIMESTAMP), PATIENT_ID_INVALID_ERROR_MESSAGE),
                Arguments.of(buildQueryMetricsRequestBody(ids5, null, TIMESTAMP), TIMESTAMP_BLANK_ERROR_MESSAGE),
                Arguments.of(buildQueryMetricsRequestBody(ids5, "", TIMESTAMP), TIMESTAMP_BLANK_ERROR_MESSAGE),
                Arguments.of(buildQueryMetricsRequestBody(ids5, INVALID_TIMESTAMP, TIMESTAMP), TIMESTAMP_INVALID_ERROR_MESSAGE),
                Arguments.of(buildQueryMetricsRequestBody(ids5, TIMESTAMP, null), TIMESTAMP_BLANK_ERROR_MESSAGE),
                Arguments.of(buildQueryMetricsRequestBody(ids5, TIMESTAMP, ""), TIMESTAMP_BLANK_ERROR_MESSAGE),
                Arguments.of(buildQueryMetricsRequestBody(ids5, TIMESTAMP, INVALID_TIMESTAMP), TIMESTAMP_INVALID_ERROR_MESSAGE)
        );
    }

    @Test
    public void testUpdatePatient_HappyCase() {
        when(patientDao.findById(anyString())).thenReturn(buildPatientDefault());

        UpdatePatientRequestBody requestBody = buildUpdatePatientRequestBody();
        requestBody.setPhoneNumber(PHONE_NUMBER2);
        UpdatePatientResponseBody responseBody = cut.updatePatient(requestBody);

        verify(patientDao, times(1)).update(patientCaptor.capture());
        assertEquals(FIRST_NAME, patientCaptor.getValue().getFirstName());
        assertEquals(LAST_NAME, patientCaptor.getValue().getLastName());
        assertEquals(PHONE_NUMBER2, patientCaptor.getValue().getPhoneNumber());
        assertEquals("OK", responseBody.getMessage());
    }

    @Test
    public void testUpdatePatient_WHEN_PatientDaoFindByIdThrows_THEN_ThrowSameException() {
        NullPointerException toThrow = new NullPointerException();
        Mockito.doThrow(toThrow).when(patientDao).findById(anyString());

        UpdatePatientRequestBody requestBody = buildUpdatePatientRequestBody();
        assertThatThrownBy(() -> cut.updatePatient(requestBody)).isSameAs(toThrow);
    }

    @Test
    public void testUpdatePatient_WHEN_PatientDaoUpdateThrows_THEN_ThrowSameException() {
        when(patientDao.findById(anyString())).thenReturn(buildPatientDefault());

        NullPointerException toThrow = new NullPointerException();
        Mockito.doThrow(toThrow).when(patientDao).update(any(Patient.class));

        UpdatePatientRequestBody requestBody = buildUpdatePatientRequestBody();
        assertThatThrownBy(() -> cut.updatePatient(requestBody)).isSameAs(toThrow);
    }

    @ParameterizedTest
    @MethodSource("invalidInputsForUpdatePatient")
    public void testUpdatePatient_WHEN_InvalidInput_THEN_ThrowInvalidInputException(UpdatePatientRequestBody body, String errorMessage) {
        assertInvalidInputExceptionThrown(() -> cut.updatePatient(body), errorMessage);
    }

    private static Stream<Arguments> invalidInputsForUpdatePatient() {
        return Stream.of(
                Arguments.of(null, UPDATE_PATIENT_NULL_ERROR_MESSAGE),
                Arguments.of(buildUpdatePatientRequestBody(null, FIRST_NAME, LAST_NAME, PHONE_NUMBER1),
                        PATIENT_ID_BLANK_ERROR_MESSAGE),
                Arguments.of(buildUpdatePatientRequestBody("", FIRST_NAME, LAST_NAME, PHONE_NUMBER1),
                        PATIENT_ID_BLANK_ERROR_MESSAGE),
                Arguments.of(buildUpdatePatientRequestBody(CAREGIVER_ID1, FIRST_NAME, LAST_NAME, PHONE_NUMBER1),
                        PATIENT_ID_INVALID_ERROR_MESSAGE),
                Arguments.of(buildUpdatePatientRequestBody(PATIENT_ID, null, LAST_NAME, PHONE_NUMBER1),
                        FIRST_NAME_BLANK_ERROR_MESSAGE),
                Arguments.of(buildUpdatePatientRequestBody(PATIENT_ID, "", LAST_NAME, PHONE_NUMBER1),
                        FIRST_NAME_BLANK_ERROR_MESSAGE),
                Arguments.of(buildUpdatePatientRequestBody(PATIENT_ID, FIRST_NAME, null, PHONE_NUMBER1),
                        LAST_NAME_BLANK_ERROR_MESSAGE),
                Arguments.of(buildUpdatePatientRequestBody(PATIENT_ID, FIRST_NAME, "", PHONE_NUMBER1),
                        LAST_NAME_BLANK_ERROR_MESSAGE),
                Arguments.of(buildUpdatePatientRequestBody(PATIENT_ID, FIRST_NAME, LAST_NAME, null),
                        PHONE_NUMBER_BLANK_ERROR_MESSAGE),
                Arguments.of(buildUpdatePatientRequestBody(PATIENT_ID, FIRST_NAME, LAST_NAME, ""),
                        PHONE_NUMBER_BLANK_ERROR_MESSAGE)
        );
    }

    @Test
    public void testDeletePatient_HappyCase() {
        DeletePatientRequestBody requestBody = buildDeletePatientRequestBody();
        DeletePatientResponseBody responseBody = cut.deletePatient(requestBody);

        verify(patientDao, times(1)).delete(eq(PATIENT_ID));
        assertEquals("OK", responseBody.getMessage());
    }

    @Test
    public void testDeletePatient_WHEN_PatientDaoDeleteThrows_THEN_ThrowSameException() {
        NullPointerException toThrow = new NullPointerException();
        Mockito.doThrow(toThrow).when(patientDao).delete(anyString());

        DeletePatientRequestBody requestBody = buildDeletePatientRequestBody();
        assertThatThrownBy(() -> cut.deletePatient(requestBody)).isSameAs(toThrow);
    }

    @ParameterizedTest
    @MethodSource("invalidInputsForDeletePatient")
    public void testDeletePatient_WHEN_InvalidInput_THEN_ThrowInvalidInputException(DeletePatientRequestBody body, String errorMessage) {
        assertInvalidInputExceptionThrown(() -> cut.deletePatient(body), errorMessage);
    }

    private static Stream<Arguments> invalidInputsForDeletePatient() {
        return Stream.of(
                Arguments.of(null, DELETE_PATIENT_NULL_ERROR_MESSAGE),
                Arguments.of(buildDeletePatientRequestBody(null), PATIENT_ID_BLANK_ERROR_MESSAGE),
                Arguments.of(buildDeletePatientRequestBody(""), PATIENT_ID_BLANK_ERROR_MESSAGE),
                Arguments.of(buildDeletePatientRequestBody(CAREGIVER_ID1), PATIENT_ID_INVALID_ERROR_MESSAGE)
        );
    }

    private static CreatePatientRequestBody buildCreatePatientRequestBody() {
        return buildCreatePatientRequestBody(FIRST_NAME, LAST_NAME, PHONE_NUMBER1);
    }

    private static CreatePatientRequestBody buildCreatePatientRequestBody(String firstName, String lastName, String phoneNumber) {
        return CreatePatientRequestBody.builder()
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .build();
    }

    private static UpdatePatientDeviceRequestBody buildUpdatePatientDeviceRequestBody() {
        return buildUpdatePatientDeviceRequestBody(PATIENT_ID);
    }

    private static UpdatePatientDeviceRequestBody buildUpdatePatientDeviceRequestBody(String patientId) {
        return UpdatePatientDeviceRequestBody.builder()
                .patientId(patientId)
                .build();
    }

    private static VerifyPatientRequestBody buildVerifyPatientRequestBody() {
        return buildVerifyPatientRequestBody(CAREGIVER_ID1, PATIENT_ID, AUTH_CODE, DEVICE_ID);
    }

    private static VerifyPatientRequestBody buildVerifyPatientRequestBody(String caregiverId, String patientId,
                                                                          String authCode, String deviceId) {
        return VerifyPatientRequestBody.builder()
                .caregiverId(caregiverId)
                .patientId(patientId)
                .authCode(authCode)
                .deviceId(deviceId)
                .build();
    }

    private static GetPatientRequestBody buildGetPatientRequestBody() {
        return buildGetPatientRequestBody(PATIENT_ID);
    }

    private static GetPatientRequestBody buildGetPatientRequestBody(String patientId) {
        return GetPatientRequestBody.builder()
                .patientId(patientId)
                .build();
    }

    private static GetAllCaregiversRequestBody buildGetAllCaregiversRequestBody() {
        return buildGetAllCaregiversRequestBody(PATIENT_ID);
    }

    private static GetAllCaregiversRequestBody buildGetAllCaregiversRequestBody(String patientId) {
        return GetAllCaregiversRequestBody.builder()
                .patientId(patientId)
                .build();
    }

    private static AddMetricsRequestBody buildAddMetricsRequestBody(List<AddMetricsSerialization> metrics) {
        return buildAddMetricsRequestBody(DEVICE_ID, metrics);
    }

    private static AddMetricsRequestBody buildAddMetricsRequestBody(String deviceId, List<AddMetricsSerialization> metrics) {
        return AddMetricsRequestBody.builder()
                .deviceId(deviceId)
                .metrics(metrics)
                .build();
    }

    private static QueryMetricsRequestBody buildQueryMetricsRequestBody() {
        return buildQueryMetricsRequestBody(PATIENT_IDS, TIMESTAMP, TIMESTAMP);
    }

    private static QueryMetricsRequestBody buildQueryMetricsRequestBody(List<String> patientIds, String start, String end) {
        return QueryMetricsRequestBody.builder()
                .patientIds(patientIds)
                .start(start)
                .end(end)
                .build();
    }

    private static UpdatePatientRequestBody buildUpdatePatientRequestBody() {
        return buildUpdatePatientRequestBody(PATIENT_ID, FIRST_NAME, LAST_NAME, PHONE_NUMBER1);
    }

    private static UpdatePatientRequestBody buildUpdatePatientRequestBody(String patientId, String firstName,
                                                                          String lastName, String phoneNumber) {
        return UpdatePatientRequestBody.builder()
                .patientId(patientId)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .build();
    }

    private static DeletePatientRequestBody buildDeletePatientRequestBody() {
        return buildDeletePatientRequestBody(PATIENT_ID);
    }

    private static DeletePatientRequestBody buildDeletePatientRequestBody(String patientId) {
        return DeletePatientRequestBody.builder()
                .patientId(patientId)
                .build();
    }

    private static Patient buildPatientDefault() {
        Patient patient = buildPatient(PATIENT_ID, PATIENT_ID, null, FIRST_NAME, LAST_NAME, PHONE_NUMBER1,
                AUTH_CODE, AUTH_CODE_TIMESTAMP, false);
        patient.setCreatedAt(CREATED_AT);
        return patient;
    }

    private static Caregiver buildCaregiverDefault() {
        return buildCaregiver(CAREGIVER_ID1, CAREGIVER_ID1, EMAIL, FIRST_NAME, LAST_NAME, TITLE, PHONE_NUMBER1);
    }

    private static Metrics buildMetricsDefault(MeasureName measureName, String measureValue) {
        return buildMetrics(PATIENT_ID, DEVICE_ID, measureName, measureValue, TIMESTAMP);
    }

    private static AddMetricsSerialization buildAddMetricsSerialization(String[] metricsValues) {
        return buildAddMetricsSerialization(metricsValues[0], metricsValues[1], metricsValues[2], metricsValues[3], metricsValues[4]);
    }

    private static AddMetricsSerialization buildAddMetricsSerialization(String stepLength, String doubleSupportTime, String walkingSpeed,
                                                                        String walkingAsymmetry, String distanceWalked) {
        return AddMetricsSerialization.builder()
                .stepLength(stepLength)
                .doubleSupportTime(doubleSupportTime)
                .walkingSpeed(walkingSpeed)
                .walkingAsymmetry(walkingAsymmetry)
                .distanceWalked(distanceWalked)
                .timestamp(TIMESTAMP)
                .build();
    }

    private static QueryMetricsSerialization buildQueryMetricsSerialization(String patientId, String metricName, String metricValue) {
        return QueryMetricsSerialization.builder()
                .patientId(patientId)
                .metricName(metricName)
                .metricValue(metricValue)
                .timestamp(TIMESTAMP)
                .build();
    }
}
