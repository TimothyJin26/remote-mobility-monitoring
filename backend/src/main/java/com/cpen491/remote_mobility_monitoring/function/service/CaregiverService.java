package com.cpen491.remote_mobility_monitoring.function.service;

import com.cpen491.remote_mobility_monitoring.datastore.dao.CaregiverDao;
import com.cpen491.remote_mobility_monitoring.datastore.exception.DuplicateRecordException;
import com.cpen491.remote_mobility_monitoring.datastore.exception.RecordDoesNotExistException;
import com.cpen491.remote_mobility_monitoring.datastore.model.Caregiver;
import com.cpen491.remote_mobility_monitoring.datastore.model.Organization;
import com.cpen491.remote_mobility_monitoring.datastore.model.Patient;
import com.cpen491.remote_mobility_monitoring.dependency.utility.Validator;
import com.cpen491.remote_mobility_monitoring.function.schema.caregiver.AddPatientRequestBody;
import com.cpen491.remote_mobility_monitoring.function.schema.caregiver.AddPatientResponseBody;
import com.cpen491.remote_mobility_monitoring.function.schema.caregiver.CreateCaregiverRequestBody;
import com.cpen491.remote_mobility_monitoring.function.schema.caregiver.CreateCaregiverResponseBody;
import com.cpen491.remote_mobility_monitoring.function.schema.caregiver.DeleteCaregiverRequestBody;
import com.cpen491.remote_mobility_monitoring.function.schema.caregiver.DeleteCaregiverResponseBody;
import com.cpen491.remote_mobility_monitoring.function.schema.caregiver.GetAllPatientsRequestBody;
import com.cpen491.remote_mobility_monitoring.function.schema.caregiver.GetAllPatientsResponseBody;
import com.cpen491.remote_mobility_monitoring.function.schema.caregiver.GetAllPatientsResponseBody.PatientSerialization;
import com.cpen491.remote_mobility_monitoring.function.schema.caregiver.GetCaregiverRequestBody;
import com.cpen491.remote_mobility_monitoring.function.schema.caregiver.GetCaregiverResponseBody;
import com.cpen491.remote_mobility_monitoring.function.schema.caregiver.RemovePatientRequestBody;
import com.cpen491.remote_mobility_monitoring.function.schema.caregiver.RemovePatientResponseBody;
import com.cpen491.remote_mobility_monitoring.function.schema.caregiver.UpdateCaregiverRequestBody;
import com.cpen491.remote_mobility_monitoring.function.schema.caregiver.UpdateCaregiverResponseBody;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CaregiverService {
    @NonNull
    private CaregiverDao caregiverDao;

    /**
     * Creates a Caregiver and adds it to an Organization.
     *
     * @param body The request body
     * @return {@link CreateCaregiverResponseBody}
     * @throws RecordDoesNotExistException If Organization record with given organizationId does not exist
     * @throws DuplicateRecordException If record with the given email already exists
     * @throws IllegalArgumentException
     * @throws NullPointerException Above 2 exceptions are thrown if any of email, firstName, lastName,
     *                              title, phoneNumber, or organizationId are empty
     */
    public CreateCaregiverResponseBody createCaregiver(CreateCaregiverRequestBody body) {
        Validator.validateCreateCaregiverRequestBody(body);

        Caregiver caregiver = Caregiver.builder()
                .email(body.getEmail())
                .firstName(body.getFirstName())
                .lastName(body.getLastName())
                .title(body.getTitle())
                .phoneNumber(body.getPhoneNumber())
                .build();
        caregiverDao.create(caregiver, body.getOrganizationId());

        return CreateCaregiverResponseBody.builder()
                .message("OK")
                .build();
    }

    /**
     * Adds a Patient to a Caregiver. Patient and Caregiver must already exist.
     *
     * @param body The request body
     * @return {@link AddPatientResponseBody}
     * @throws RecordDoesNotExistException If Patient or Caregiver records do not exist
     * @throws DuplicateRecordException If Patient/Caregiver association already exists
     * @throws IllegalArgumentException
     * @throws NullPointerException Above 2 exceptions are thrown if patientId or caregiverId is empty or invalid
     */
    public AddPatientResponseBody addPatient(AddPatientRequestBody body) {
        Validator.validateAddPatientRequestBody(body);

        caregiverDao.addPatient(body.getPatientId(), body.getCaregiverId());

        return AddPatientResponseBody.builder()
                .message("OK")
                .build();
    }

    /**
     * Removes a Patient from a Caregiver. Patient and Caregiver must already exist.
     *
     * @param body The request body
     * @return {@link RemovePatientResponseBody}
     * @throws IllegalArgumentException
     * @throws NullPointerException Above 2 exceptions are thrown if patientId or caregiverId is empty or invalid
     */
    public RemovePatientResponseBody removePatient(RemovePatientRequestBody body) {
        Validator.validateRemovePatientRequestBody(body);

        caregiverDao.removePatient(body.getPatientId(), body.getCaregiverId());

        return RemovePatientResponseBody.builder()
                .message("OK")
                .build();
    }

    /**
     * Gets the Caregiver specified by caregiverId and its Organization.
     *
     * @param body The request body
     * @return {@link GetCaregiverResponseBody}
     * @throws RecordDoesNotExistException If Caregiver record with the given caregiverId does not exist
     * @throws IllegalArgumentException
     * @throws NullPointerException Above 2 exceptions are thrown if caregiverId is empty
     */
    public GetCaregiverResponseBody getCaregiver(GetCaregiverRequestBody body) {
        Validator.validateGetCaregiverRequestBody(body);

        Caregiver caregiver = caregiverDao.findById(body.getCaregiverId());
        Organization organization = caregiverDao.findOrganization(body.getCaregiverId());

        return GetCaregiverResponseBody.builder()
                .email(caregiver.getEmail())
                .firstName(caregiver.getFirstName())
                .lastName(caregiver.getLastName())
                .title(caregiver.getTitle())
                .phoneNumber(caregiver.getPhoneNumber())
                .organizationId(organization.getPid())
                .organizationName(organization.getName())
                .createdAt(caregiver.getCreatedAt())
                .build();
    }

    /**
     * Gets all Patients for a Caregiver.
     *
     * @param body The request body
     * @return {@link GetAllPatientsResponseBody}
     * @throws RecordDoesNotExistException If Caregiver record with the given caregiverId does not exist
     * @throws IllegalArgumentException
     * @throws NullPointerException Above 2 exceptions are thrown if caregiverId is empty
     */
    public GetAllPatientsResponseBody getAllPatients(GetAllPatientsRequestBody body) {
        Validator.validateGetAllPatientsRequestBody(body);

        List<Patient> patients = caregiverDao.findAllPatients(body.getCaregiverId());

        return GetAllPatientsResponseBody.builder()
                .patients(patients.stream().map(PatientSerialization::fromPatient).collect(Collectors.toList()))
                .build();
    }

    /**
     * Updates a Caregiver.
     *
     * @param body The request body
     * @return {@link UpdateCaregiverResponseBody}
     * @throws DuplicateRecordException If record with the given email already exists
     * @throws RecordDoesNotExistException If record with the given id does not exist
     * @throws IllegalArgumentException
     * @throws NullPointerException Above 2 exceptions are thrown if any of caregiverId, email, firstName, lastName,
     *                              title, or phoneNumber are empty
     */
    public UpdateCaregiverResponseBody updateCaregiver(UpdateCaregiverRequestBody body) {
        Validator.validateUpdateCaregiverRequestBody(body);

        Caregiver caregiver = caregiverDao.findById(body.getCaregiverId());
        caregiver.setEmail(body.getEmail());
        caregiver.setFirstName(body.getFirstName());
        caregiver.setLastName(body.getLastName());
        caregiver.setTitle(body.getTitle());
        caregiver.setPhoneNumber(body.getPhoneNumber());
        caregiverDao.update(caregiver);

        return UpdateCaregiverResponseBody.builder()
                .message("OK")
                .build();
    }

    /**
     * Deletes a Caregiver.
     *
     * @param body The request body
     * @return {@link DeleteCaregiverResponseBody}
     * @throws IllegalArgumentException
     * @throws NullPointerException Above 2 exceptions are thrown if caregiverId is empty
     */
    public DeleteCaregiverResponseBody deleteCaregiver(DeleteCaregiverRequestBody body) {
        Validator.validateDeleteCaregiverRequestBody(body);

        caregiverDao.delete(body.getCaregiverId());

        return DeleteCaregiverResponseBody.builder()
                .message("OK")
                .build();
    }
}
