package com.rappidrive.infrastructure.config;

import com.rappidrive.application.ports.input.driver.*;
import com.rappidrive.application.ports.input.notification.*;
import com.rappidrive.application.ports.input.passenger.*;
import com.rappidrive.application.ports.input.payment.*;
import com.rappidrive.application.ports.input.rating.*;
import com.rappidrive.application.ports.input.trip.*;
import com.rappidrive.application.ports.input.vehicle.*;
import com.rappidrive.application.ports.input.CompleteTripWithPaymentInputPort;
import com.rappidrive.application.ports.input.GetTripWithPaymentDetailsInputPort;
import com.rappidrive.application.ports.output.*;
import com.rappidrive.application.usecases.driver.*;
import com.rappidrive.application.usecases.notification.*;
import com.rappidrive.application.usecases.passenger.*;
import com.rappidrive.application.usecases.payment.*;
import com.rappidrive.application.usecases.rating.*;
import com.rappidrive.application.usecases.trip.*;
import com.rappidrive.application.usecases.vehicle.*;
import com.rappidrive.domain.services.FareCalculator;
import com.rappidrive.domain.services.StandardFareCalculator;
import com.rappidrive.domain.events.DomainEventPublisher;
import com.rappidrive.domain.services.RatingValidationService;
import com.rappidrive.domain.services.TripCompletionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for wiring use cases (application layer).
 * Use cases are plain Java classes, not Spring components.
 * Dependencies are injected via constructor.
 */
@Configuration
public class UseCaseConfiguration {
    
    // Driver Use Cases
    
    @Bean
    public CreateDriverInputPort createDriverUseCase(DriverRepositoryPort driverRepository) {
        return new CreateDriverUseCase(driverRepository);
    }
    
    @Bean
    public GetDriverInputPort getDriverUseCase(DriverRepositoryPort driverRepository) {
        return new GetDriverUseCase(driverRepository);
    }
    
    @Bean
    public ActivateDriverInputPort activateDriverUseCase(DriverRepositoryPort driverRepository) {
        return new ActivateDriverUseCase(driverRepository);
    }
    
    @Bean
    public UpdateDriverLocationInputPort updateDriverLocationUseCase(DriverRepositoryPort driverRepository) {
        return new UpdateDriverLocationUseCase(driverRepository);
    }
    
    @Bean
    public FindAvailableDriversInputPort findAvailableDriversUseCase(DriverGeoQueryPort driverGeoQueryPort) {
        return new FindAvailableDriversUseCase(driverGeoQueryPort);
    }
    
    // Passenger Use Cases
    
    @Bean
    public CreatePassengerInputPort createPassengerUseCase(PassengerRepositoryPort passengerRepository) {
        return new CreatePassengerUseCase(passengerRepository);
    }
    
    @Bean
    public GetPassengerInputPort getPassengerUseCase(PassengerRepositoryPort passengerRepository) {
        return new GetPassengerUseCase(passengerRepository);
    }
    
    // Trip Use Cases
    
    @Bean
    public CreateTripInputPort createTripUseCase(TripRepositoryPort tripRepository,
                                                  PassengerRepositoryPort passengerRepository) {
        return new CreateTripUseCase(tripRepository, passengerRepository);
    }
    
    @Bean
    public GetTripInputPort getTripUseCase(TripRepositoryPort tripRepository) {
        return new GetTripUseCase(tripRepository);
    }
    
    @Bean
    public AssignDriverToTripInputPort assignDriverToTripUseCase(TripRepositoryPort tripRepository,
                                                                  DriverRepositoryPort driverRepository,
                                                                  DomainEventPublisher eventPublisher) {
        return new AssignDriverToTripUseCase(tripRepository, driverRepository, eventPublisher);
    }
    
    @Bean
    public StartTripInputPort startTripUseCase(TripRepositoryPort tripRepository) {
        return new StartTripUseCase(tripRepository);
    }
    
    @Bean
    public CompleteTripInputPort completeTripUseCase(TripRepositoryPort tripRepository,
                                                      DriverRepositoryPort driverRepository) {
        return new CompleteTripUseCase(tripRepository, driverRepository);
    }
    
    @Bean
    public TripCompletionService tripCompletionService() {
        return new TripCompletionService();
    }
    
    @Bean
    public CompleteTripWithPaymentInputPort completeTripWithPaymentUseCase(
            TripRepositoryPort tripRepository,
            FareRepositoryPort fareRepository,
            DistanceCalculationPort distanceCalculation,
            CalculateFareInputPort calculateFare,
            ProcessPaymentInputPort processPayment,
            TripCompletionService completionService) {
        return new CompleteTripWithPaymentUseCase(
            tripRepository,
            fareRepository,
            distanceCalculation,
            calculateFare,
            processPayment,
            completionService
        );
    }
    
    @Bean
    public GetTripWithPaymentDetailsInputPort getTripWithPaymentDetailsUseCase(
            TripRepositoryPort tripRepository,
            FareRepositoryPort fareRepository,
            PaymentRepositoryPort paymentRepository) {
        return new GetTripWithPaymentDetailsUseCase(
            tripRepository,
            fareRepository,
            paymentRepository
        );
    }
    
    // Vehicle Use Cases
    
    @Bean
    public CreateVehicleInputPort createVehicleUseCase(VehicleRepositoryPort vehicleRepository) {
        return new CreateVehicleUseCase(vehicleRepository);
    }
    
    @Bean
    public GetVehicleInputPort getVehicleUseCase(VehicleRepositoryPort vehicleRepository) {
        return new GetVehicleUseCase(vehicleRepository);
    }
    
    @Bean
    public UpdateVehicleInputPort updateVehicleUseCase(VehicleRepositoryPort vehicleRepository) {
        return new UpdateVehicleUseCase(vehicleRepository);
    }
    
    @Bean
    public AssignVehicleToDriverInputPort assignVehicleToDriverUseCase(VehicleRepositoryPort vehicleRepository) {
        return new AssignVehicleToDriverUseCase(vehicleRepository);
    }
    
    @Bean
    public ActivateVehicleInputPort activateVehicleUseCase(VehicleRepositoryPort vehicleRepository) {
        return new ActivateVehicleUseCase(vehicleRepository);
    }
    
    // Payment Use Cases
    
    @Bean
    public CalculateFareInputPort calculateFareUseCase(FareConfigurationRepositoryPort fareConfigRepository) {
        return new CalculateFareUseCase(fareConfigRepository);
    }
    
    @Bean
    public ProcessPaymentInputPort processPaymentUseCase(PaymentRepositoryPort paymentRepository,
                                                          PaymentGatewayPort paymentGateway,
                                                          FareConfigurationRepositoryPort fareConfigRepository) {
        return new ProcessPaymentUseCase(paymentRepository, paymentGateway, fareConfigRepository);
    }
    
    @Bean
    public GetPaymentInputPort getPaymentUseCase(PaymentRepositoryPort paymentRepository) {
        return new GetPaymentUseCase(paymentRepository);
    }
    
    @Bean
    public RefundPaymentInputPort refundPaymentUseCase(PaymentRepositoryPort paymentRepository,
                                                        PaymentGatewayPort paymentGateway) {
        return new RefundPaymentUseCase(paymentRepository, paymentGateway);
    }
    
    @Bean
    public GetFareConfigurationInputPort getFareConfigurationUseCase(FareConfigurationRepositoryPort fareConfigRepository) {
        return new GetFareConfigurationUseCase(fareConfigRepository);
    }
    
    @Bean
    public UpdateFareConfigurationInputPort updateFareConfigurationUseCase(FareConfigurationRepositoryPort fareConfigRepository) {
        return new UpdateFareConfigurationUseCase(fareConfigRepository);
    }
    
    // Rating Use Cases
    
    @Bean
    public RatingValidationService ratingValidationService() {
        return new RatingValidationService();
    }
    
    @Bean
    public CreateRatingInputPort createRatingUseCase(
            RatingRepositoryPort ratingRepository,
            TripRepositoryPort tripRepository,
            RatingValidationService validationService) {
        return new CreateRatingUseCase(ratingRepository, tripRepository, validationService);
    }
    
    @Bean
    public GetDriverRatingSummaryInputPort getDriverRatingSummaryUseCase(
            RatingRepositoryPort ratingRepository) {
        return new GetDriverRatingSummaryUseCase(ratingRepository);
    }
    
    @Bean
    public GetPassengerRatingInputPort getPassengerRatingUseCase(
            RatingRepositoryPort ratingRepository) {
        return new GetPassengerRatingUseCase(ratingRepository);
    }
    
    @Bean
    public GetTripRatingsInputPort getTripRatingsUseCase(
            RatingRepositoryPort ratingRepository) {
        return new GetTripRatingsUseCase(ratingRepository);
    }
    
    @Bean
    public ReportOffensiveRatingInputPort reportOffensiveRatingUseCase(
            RatingRepositoryPort ratingRepository) {
        return new ReportOffensiveRatingUseCase(ratingRepository);
    }
    
    // Notification Use Cases
    
    @Bean
    public SendNotificationInputPort sendNotificationUseCase(
            NotificationRepositoryPort notificationRepository,
            NotificationServicePort notificationService) {
        return new SendNotificationUseCase(notificationRepository, notificationService);
    }
    
    @Bean
    public GetUserNotificationsInputPort getUserNotificationsUseCase(
            NotificationRepositoryPort notificationRepository) {
        return new GetUserNotificationsUseCase(notificationRepository);
    }
    
    @Bean
    public MarkNotificationAsReadInputPort markNotificationAsReadUseCase(
            NotificationRepositoryPort notificationRepository) {
        return new MarkNotificationAsReadUseCase(notificationRepository);
    }
    
    @Bean
    public GetUnreadCountInputPort getUnreadCountUseCase(
            NotificationRepositoryPort notificationRepository) {
        return new GetUnreadCountUseCase(notificationRepository);
    }
    
    // FareCalculator Bean
    @Bean
    public FareCalculator fareCalculator() {
        return new StandardFareCalculator();
    }

    // DomainEventPublisher Bean
    @Bean
    public DomainEventPublisher domainEventPublisher() {
        return DomainEventPublisher.instance();
    }
}
