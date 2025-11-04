package com.dogukanpolat.telemedicine.service;

import com.dogukanpolat.telemedicine.dto.appointment.AppointmentResponseDto;
import com.dogukanpolat.telemedicine.dto.search.*;
import com.dogukanpolat.telemedicine.mappers.AppointmentMapper;
import com.dogukanpolat.telemedicine.mappers.SearchMapper;
import com.dogukanpolat.telemedicine.model.*;
import com.dogukanpolat.telemedicine.model.enums.AppointmentStatus;
import com.dogukanpolat.telemedicine.model.enums.DayOfWeek;
import com.dogukanpolat.telemedicine.repository.AppointmentRepository;
import com.dogukanpolat.telemedicine.repository.DoctorAvailabilityRepository;
import com.dogukanpolat.telemedicine.repository.DoctorRepository;
import com.dogukanpolat.telemedicine.repository.PatientRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorAvailabilityRepository availabilityRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private SearchMapper searchMapper;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private CriteriaQuery<Doctor> doctorQuery;

    @Mock
    private CriteriaQuery<Patient> patientQuery;

    @Mock
    private CriteriaQuery<Appointment> appointmentQuery;

    @Mock
    private Root<Doctor> doctorRoot;

    @Mock
    private Root<Patient> patientRoot;

    @Mock
    private Root<Appointment> appointmentRoot;

    @Mock
    private TypedQuery<Doctor> doctorTypedQuery;

    @Mock
    private TypedQuery<Patient> patientTypedQuery;

    @Mock
    private TypedQuery<Appointment> appointmentTypedQuery;

    @Mock
    private Path<Object> mockPath;

    @Mock
    private Fetch<Object, Object> mockFetchParent;

    @Mock
    private Predicate mockPredicate;

    @Mock
    private Expression<String> mockExpression;

    @Mock
    private Order mockOrder;

    @InjectMocks
    private SearchService searchService;

    private Doctor testDoctor;
    private Patient testPatient;
    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        UserModel doctorUser = new UserModel();
        doctorUser.setId(UUID.randomUUID());
        doctorUser.setFirstName("John");
        doctorUser.setLastName("Smith");
        doctorUser.setEmail("doctor@test.com");
        doctorUser.setPhoneNumber("+1234567890");

        testDoctor = new Doctor();
        testDoctor.setId(UUID.randomUUID());
        testDoctor.setUser(doctorUser);
        testDoctor.setSpecialization("Cardiology");
        testDoctor.setYearsOfExperience(10);
        testDoctor.setConsultationFee(new BigDecimal("150.00"));
        testDoctor.setIsVerified(true);

        UserModel patientUser = new UserModel();
        patientUser.setId(UUID.randomUUID());
        patientUser.setFirstName("Jane");
        patientUser.setLastName("Doe");
        patientUser.setEmail("patient@test.com");
        patientUser.setPhoneNumber("+9876543210");
        patientUser.setIsActive(true);

        testPatient = new Patient();
        testPatient.setId(UUID.randomUUID());
        testPatient.setUser(patientUser);
        testPatient.setBloodType("A+");
        testPatient.setEmergencyContactName("Emergency Contact");
        testPatient.setEmergencyContactPhone("+1111111111");
        testPatient.setCreatedAt(LocalDate.now());

        testAppointment = new Appointment();
        testAppointment.setId(UUID.randomUUID());
        testAppointment.setPatient(testPatient);
        testAppointment.setDoctor(testDoctor);
        testAppointment.setScheduledDate(LocalDate.now().plusDays(1));
        testAppointment.setScheduledTime(LocalTime.of(10, 0));
        testAppointment.setDurationMinutes(30);
        testAppointment.setStatus(AppointmentStatus.SCHEDULED);

        DoctorSearchResponseDto doctorDto = new DoctorSearchResponseDto(
                testDoctor.getId(),
                "John", "Smith", "doctor@test.com", "+1234567890",
                "Cardiology", 10, new BigDecimal("150.00"), true
        );

        PatientSearchResponseDto patientDto = new PatientSearchResponseDto(
                testPatient.getId(),
                "Jane", "Doe", "patient@test.com", "+9876543210",
                "A+", List.of(), "Emergency Contact", "+1111111111", true, LocalDate.now()
        );

        lenient().when(searchMapper.toDoctorSearchResponseDto(any(Doctor.class)))
                .thenReturn(doctorDto);
        lenient().when(searchMapper.toPatientSearchResponseDto(any(Patient.class)))
                .thenReturn(patientDto);
    }

    @Test
    void searchDoctors_WithSpecialization_ShouldReturnMatchingDoctors() {
        // Given
        DoctorSearchCriteria criteria = new DoctorSearchCriteria(
                "Cardiology", null, null, null, null, null, null, null, null
        );

        setupBasicDoctorQueryMocks();
        when(doctorRoot.get(anyString())).thenReturn(mockPath);
        when(criteriaBuilder.like(any(), eq("%Cardiology%"))).thenReturn(mockPredicate);
        when(doctorTypedQuery.getResultList()).thenReturn(List.of(testDoctor));

        // When
        List<DoctorSearchResponseDto> result = searchService.searchDoctors(criteria);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().specialization()).isEqualTo("Cardiology");
        verify(entityManager).createQuery(doctorQuery);
        verify(searchMapper).toDoctorSearchResponseDto(testDoctor);
    }

    @Test
    void searchDoctors_WithFeeRange_ShouldReturnMatchingDoctors() {
        // Given
        DoctorSearchCriteria criteria = new DoctorSearchCriteria(
                null,
                new BigDecimal("100.00"),
                new BigDecimal("200.00"),
                null, null, null, null, null, null
        );

        setupBasicDoctorQueryMocks();
        when(doctorRoot.get(anyString())).thenReturn(mockPath);
        when(criteriaBuilder.greaterThanOrEqualTo(any(), eq(new BigDecimal("100.00")))).thenReturn(mockPredicate);
        when(criteriaBuilder.lessThanOrEqualTo(any(), eq(new BigDecimal("200.00")))).thenReturn(mockPredicate);
        when(doctorTypedQuery.getResultList()).thenReturn(List.of(testDoctor));

        // When
        List<DoctorSearchResponseDto> result = searchService.searchDoctors(criteria);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().consultationFee()).isEqualTo(new BigDecimal("150.00"));
        verify(searchMapper).toDoctorSearchResponseDto(testDoctor);
    }

    @Test
    void searchDoctors_WithVerificationFilter_ShouldReturnOnlyVerified() {
        // Given
        DoctorSearchCriteria criteria = new DoctorSearchCriteria(
                null, null, null, null, true, null, null, null, null
        );

        setupBasicDoctorQueryMocks();
        when(doctorRoot.get(anyString())).thenReturn(mockPath);
        when(criteriaBuilder.equal(any(), eq(true))).thenReturn(mockPredicate);
        when(doctorTypedQuery.getResultList()).thenReturn(List.of(testDoctor));

        // When
        List<DoctorSearchResponseDto> result = searchService.searchDoctors(criteria);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().isVerified()).isTrue();
        verify(searchMapper).toDoctorSearchResponseDto(testDoctor);
    }

    @Test
    void searchDoctors_WithAvailability_ShouldFilterByAvailability() {
        // Given
        DoctorSearchCriteria criteria = new DoctorSearchCriteria(
                null, null, null, null, null,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                null
        );

        setupBasicDoctorQueryMocks();
        when(doctorTypedQuery.getResultList()).thenReturn(List.of(testDoctor));
        when(availabilityRepository.isDoctorAvailableForSlot(
                eq(testDoctor.getId()),
                eq(DayOfWeek.MONDAY),
                eq(LocalTime.of(9, 0)),
                eq(LocalTime.of(17, 0))
        )).thenReturn(true);

        // When
        List<DoctorSearchResponseDto> result = searchService.searchDoctors(criteria);

        // Then
        assertThat(result).hasSize(1);
        verify(availabilityRepository).isDoctorAvailableForSlot(
                eq(testDoctor.getId()),
                eq(DayOfWeek.MONDAY),
                eq(LocalTime.of(9, 0)),
                eq(LocalTime.of(17, 0))
        );
        verify(searchMapper).toDoctorSearchResponseDto(testDoctor);
    }

    @Test
    void searchDoctors_WithNoCriteria_ShouldReturnAllDoctors() {
        // Given
        DoctorSearchCriteria criteria = new DoctorSearchCriteria(
                null, null, null, null, null, null, null, null, null
        );

        setupBasicDoctorQueryMocks();
        when(doctorTypedQuery.getResultList()).thenReturn(List.of(testDoctor));

        // When
        List<DoctorSearchResponseDto> result = searchService.searchDoctors(criteria);

        // Then
        assertThat(result).hasSize(1);
        verify(searchMapper).toDoctorSearchResponseDto(testDoctor);
    }

    @Test
    void searchPatients_WithName_ShouldReturnMatchingPatients() {
        // Given
        PatientSearchCriteria criteria = new PatientSearchCriteria(
                "Jane", null, null, null, null
        );

        setupBasicPatientQueryMocks();
        when(criteriaBuilder.like(any(), eq("%jane%"))).thenReturn(mockPredicate);
        when(criteriaBuilder.lower(any())).thenReturn(mockExpression);
        when(criteriaBuilder.or(any(Predicate.class), any(Predicate.class))).thenReturn(mockPredicate);
        when(patientTypedQuery.getResultList()).thenReturn(List.of(testPatient));

        // When
        List<PatientSearchResponseDto> result = searchService.searchPatients(criteria);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().firstName()).isEqualTo("Jane");
        verify(entityManager).createQuery(patientQuery);
        verify(searchMapper).toPatientSearchResponseDto(testPatient);
    }

    @Test
    void searchPatients_WithBloodType_ShouldReturnMatchingPatients() {
        // Given
        PatientSearchCriteria criteria = new PatientSearchCriteria(
                null, null, "A+", null, null
        );

        setupBasicPatientQueryMocks();
        when(criteriaBuilder.equal(any(), eq("A+"))).thenReturn(mockPredicate);
        when(patientTypedQuery.getResultList()).thenReturn(List.of(testPatient));

        // When
        List<PatientSearchResponseDto> result = searchService.searchPatients(criteria);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().bloodType()).isEqualTo("A+");
        verify(searchMapper).toPatientSearchResponseDto(testPatient);
    }

    @Test
    void searchPatients_WithEmail_ShouldReturnMatchingPatients() {
        // Given
        PatientSearchCriteria criteria = new PatientSearchCriteria(
                null, "patient@test.com", null, null, null
        );

        setupBasicPatientQueryMocks();
        when(criteriaBuilder.like(any(), eq("%patient@test.com%"))).thenReturn(mockPredicate);
        when(criteriaBuilder.lower(any())).thenReturn(mockExpression);
        when(patientTypedQuery.getResultList()).thenReturn(List.of(testPatient));

        // When
        List<PatientSearchResponseDto> result = searchService.searchPatients(criteria);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().email()).isEqualTo("patient@test.com");
        verify(searchMapper).toPatientSearchResponseDto(testPatient);
    }

    @Test
    void filterAppointments_WithStatus_ShouldReturnMatchingAppointments() {
        // Given
        AppointmentFilterCriteria criteria = new AppointmentFilterCriteria(
                null, null, AppointmentStatus.SCHEDULED, null, null
        );

        AppointmentResponseDto responseDto = new AppointmentResponseDto(
                "Jane", "Doe", "John", "Smith",
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                30,
                AppointmentStatus.SCHEDULED
        );

        setupBasicAppointmentQueryMocks();
        when(criteriaBuilder.equal(any(), eq(AppointmentStatus.SCHEDULED))).thenReturn(mockPredicate);
        when(appointmentTypedQuery.getResultList()).thenReturn(List.of(testAppointment));
        when(appointmentMapper.toAppointmentResponseDto(testAppointment)).thenReturn(responseDto);

        // When
        List<AppointmentResponseDto> result = searchService.filterAppointments(criteria);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().status()).isEqualTo(AppointmentStatus.SCHEDULED);
        verify(appointmentMapper).toAppointmentResponseDto(testAppointment);
    }

    @Test
    void filterAppointments_WithDateRange_ShouldReturnAppointmentsInRange() {
        // Given
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);

        AppointmentFilterCriteria criteria = new AppointmentFilterCriteria(
                null, null, null, startDate, endDate
        );

        AppointmentResponseDto responseDto = new AppointmentResponseDto(
                "Jane", "Doe", "John", "Smith",
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                30,
                AppointmentStatus.SCHEDULED
        );

        setupBasicAppointmentQueryMocks();
        when(criteriaBuilder.greaterThanOrEqualTo(any(), eq(startDate))).thenReturn(mockPredicate);
        when(criteriaBuilder.lessThanOrEqualTo(any(), eq(endDate))).thenReturn(mockPredicate);
        when(appointmentTypedQuery.getResultList()).thenReturn(List.of(testAppointment));
        when(appointmentMapper.toAppointmentResponseDto(testAppointment)).thenReturn(responseDto);

        // When
        List<AppointmentResponseDto> result = searchService.filterAppointments(criteria);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().scheduledDate()).isAfterOrEqualTo(startDate);
        assertThat(result.getFirst().scheduledDate()).isBeforeOrEqualTo(endDate);
        verify(appointmentMapper).toAppointmentResponseDto(testAppointment);
    }

    // Minimal setup methods - only include what's absolutely necessary
    private void setupBasicDoctorQueryMocks() {
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Doctor.class)).thenReturn(doctorQuery);
        when(doctorQuery.from(Doctor.class)).thenReturn(doctorRoot);
        when(doctorRoot.fetch(anyString(), any(JoinType.class))).thenReturn(mockFetchParent);


        when(doctorQuery.where(any(Predicate[].class))).thenReturn(doctorQuery);
        when(entityManager.createQuery(doctorQuery)).thenReturn(doctorTypedQuery);
    }

    private void setupBasicPatientQueryMocks() {
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Patient.class)).thenReturn(patientQuery);
        when(patientQuery.from(Patient.class)).thenReturn(patientRoot);
        when(patientRoot.fetch(anyString(), any(JoinType.class))).thenReturn(mockFetchParent);
        when(patientRoot.get(anyString())).thenReturn(mockPath);

        when(patientQuery.where(any(Predicate[].class))).thenReturn(patientQuery);
        when(entityManager.createQuery(patientQuery)).thenReturn(patientTypedQuery);
    }

    private void setupBasicAppointmentQueryMocks() {
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Appointment.class)).thenReturn(appointmentQuery);
        when(appointmentQuery.from(Appointment.class)).thenReturn(appointmentRoot);
        when(appointmentRoot.fetch(anyString(), any(JoinType.class))).thenReturn(mockFetchParent);
        when(appointmentRoot.get(anyString())).thenReturn(mockPath);

        when(appointmentQuery.where(any(Predicate[].class))).thenReturn(appointmentQuery);
        when(entityManager.createQuery(appointmentQuery)).thenReturn(appointmentTypedQuery);
    }
}