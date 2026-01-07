package com.rappidrive.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Architecture tests to enforce hexagonal architecture boundaries.
 * These tests ensure that dependency rules are not violated.
 */
class HexagonalArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.rappidrive");
    }

    @Test
    void domainLayerShouldNotDependOnOuterLayers() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..application..",
                "..infrastructure..",
                "..presentation.."
            )
            .because("Domain layer must not depend on outer layers");

        rule.check(importedClasses);
    }

    @Test
    void domainLayerShouldNotDependOnFrameworks() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "org.springframework..",
                "jakarta.persistence..",
                "com.fasterxml.jackson.."
            )
            .because("Domain layer must be framework-free");

        rule.check(importedClasses);
    }

    @Test
    void applicationLayerShouldNotDependOnInfrastructureOrPresentation() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..infrastructure..",
                "..presentation.."
            )
            .because("Application layer should only depend on domain");

        rule.check(importedClasses);
    }

    @Test
    void layeredArchitectureShouldBeRespected() {
        ArchRule rule = layeredArchitecture()
            .consideringAllDependencies()
            .layer("Domain").definedBy("..domain..")
            .layer("Application").definedBy("..application..")
            .layer("Infrastructure").definedBy("..infrastructure..")
            .layer("Presentation").definedBy("..presentation..")
            
            .whereLayer("Presentation").mayNotBeAccessedByAnyLayer()
            .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure", "Presentation")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Presentation");

        rule.check(importedClasses);
    }

    @Test
    void domainEntitiesShouldNotHaveJpaAnnotations() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..domain.entities..")
            .should().beAnnotatedWith("jakarta.persistence.Entity")
            .orShould().beAnnotatedWith("jakarta.persistence.Table")
            .because("Domain entities must not have JPA annotations")
            .allowEmptyShould(true);

        rule.check(importedClasses);
    }

    @Test
    void useCasesShouldNotBeAnnotatedWithSpringStereotypes() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..application.usecases..")
            .should().beAnnotatedWith("org.springframework.stereotype.Service")
            .orShould().beAnnotatedWith("org.springframework.stereotype.Component")
            .because("Use cases should be plain Java classes, wired in configuration")
            .allowEmptyShould(true);

        rule.check(importedClasses);
    }

    @Test
    void portsShouldBeInterfaces() {
        ArchRule rule = classes()
            .that().resideInAPackage("..application.ports..")
            .and().areNotNestedClasses() // Allow nested records/classes as DTOs
            .and().areNotAssignableTo(Record.class) // Exclude Command records
            .should().beInterfaces()
            .because("Ports must be interfaces (nested classes like Command records are allowed)")
            .allowEmptyShould(true);

        rule.check(importedClasses);
    }

    @Test
    void repositoryAdaptersShouldImplementRepositoryPorts() {
        ArchRule rule = classes()
            .that().resideInAPackage("..infrastructure.persistence.adapters..")
            .and().haveSimpleNameEndingWith("Adapter")
            .should().implement(com.rappidrive.application.ports.output.DriverRepositoryPort.class)
            .orShould().implement(com.rappidrive.application.ports.output.PassengerRepositoryPort.class)
            .orShould().implement(com.rappidrive.application.ports.output.TripRepositoryPort.class)
            .orShould().implement(com.rappidrive.application.ports.output.VehicleRepositoryPort.class)
            .orShould().implement(com.rappidrive.application.ports.output.PaymentRepositoryPort.class)
            .orShould().implement(com.rappidrive.application.ports.output.FareConfigurationRepositoryPort.class)
            .orShould().implement(com.rappidrive.application.ports.output.PaymentGatewayPort.class)
            .orShould().implement(com.rappidrive.application.ports.output.RatingRepositoryPort.class)
            .orShould().implement(com.rappidrive.application.ports.output.NotificationRepositoryPort.class)
            .because("Repository adapters must implement port interfaces")
            .allowEmptyShould(true);

        rule.check(importedClasses);
    }

    @Test
    void useCasesShouldImplementInputPorts() {
        ArchRule rule = classes()
            .that().resideInAPackage("..application.usecases..")
            .and().haveSimpleNameEndingWith("UseCase")
            .should().implement(com.rappidrive.application.ports.input.driver.CreateDriverInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.driver.GetDriverInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.driver.ActivateDriverInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.driver.UpdateDriverLocationInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.driver.FindAvailableDriversInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.passenger.CreatePassengerInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.passenger.GetPassengerInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.trip.CreateTripInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.trip.GetTripInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.trip.AssignDriverToTripInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.trip.StartTripInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.trip.CompleteTripInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.vehicle.CreateVehicleInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.vehicle.GetVehicleInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.vehicle.UpdateVehicleInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.vehicle.AssignVehicleToDriverInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.vehicle.ActivateVehicleInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.payment.CalculateFareInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.payment.ProcessPaymentInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.payment.GetPaymentInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.payment.RefundPaymentInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.payment.GetFareConfigurationInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.payment.UpdateFareConfigurationInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.CompleteTripWithPaymentInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.GetTripWithPaymentDetailsInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.rating.CreateRatingInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.rating.GetDriverRatingSummaryInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.rating.GetPassengerRatingInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.rating.GetTripRatingsInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.rating.ReportOffensiveRatingInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.notification.SendNotificationInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.notification.GetUserNotificationsInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.notification.MarkNotificationAsReadInputPort.class)
            .orShould().implement(com.rappidrive.application.ports.input.notification.GetUnreadCountInputPort.class)
            .because("Use cases must implement their input port interfaces")
            .allowEmptyShould(true);

        rule.check(importedClasses);
    }

    @Test
    void jpaEntitiesShouldOnlyResideInInfrastructure() {
        ArchRule rule = classes()
            .that().areAnnotatedWith("jakarta.persistence.Entity")
            .should().resideInAPackage("..infrastructure.persistence.entities..")
            .because("JPA entities belong to infrastructure layer only")
            .allowEmptyShould(true);

        rule.check(importedClasses);
    }

    @Test
    void valueObjectsShouldBeFinal() {
        ArchRule rule = classes()
            .that().resideInAPackage("..domain.valueobjects..")
            .should().haveModifier(JavaModifier.FINAL)
            .because("Value objects should be immutable (final classes)")
            .allowEmptyShould(true);

        rule.check(importedClasses);
    }
}
