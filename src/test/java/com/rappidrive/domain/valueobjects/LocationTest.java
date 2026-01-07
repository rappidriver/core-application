package com.rappidrive.domain.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocationTest {

    @Test
    void shouldCreateValidLocation() {
        Location location = new Location(-23.550520, -46.633308); // São Paulo
        assertEquals(-23.550520, location.getLatitude());
        assertEquals(-46.633308, location.getLongitude());
    }

    @Test
    void shouldAcceptZeroCoordinates() {
        Location location = new Location(0.0, 0.0); // Null Island
        assertEquals(0.0, location.getLatitude());
        assertEquals(0.0, location.getLongitude());
    }

    @Test
    void shouldAcceptMaxLatitude() {
        Location location = new Location(90.0, 0.0); // North Pole
        assertEquals(90.0, location.getLatitude());
    }

    @Test
    void shouldAcceptMinLatitude() {
        Location location = new Location(-90.0, 0.0); // South Pole
        assertEquals(-90.0, location.getLatitude());
    }

    @Test
    void shouldAcceptMaxLongitude() {
        Location location = new Location(0.0, 180.0);
        assertEquals(180.0, location.getLongitude());
    }

    @Test
    void shouldAcceptMinLongitude() {
        Location location = new Location(0.0, -180.0);
        assertEquals(-180.0, location.getLongitude());
    }

    @Test
    void shouldRejectLatitudeTooHigh() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Location(90.1, 0.0));
    }

    @Test
    void shouldRejectLatitudeTooLow() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Location(-90.1, 0.0));
    }

    @Test
    void shouldRejectLongitudeTooHigh() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Location(0.0, 180.1));
    }

    @Test
    void shouldRejectLongitudeTooLow() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Location(0.0, -180.1));
    }

    @Test
    void shouldCalculateZeroDistanceForSameLocation() {
        Location location1 = new Location(-23.550520, -46.633308);
        Location location2 = new Location(-23.550520, -46.633308);
        
        double distance = location1.distanceTo(location2);
        assertEquals(0.0, distance, 0.001);
    }

    @Test
    void shouldCalculateDistanceBetweenSaoPauloAndRio() {
        // São Paulo
        Location sp = new Location(-23.550520, -46.633308);
        // Rio de Janeiro
        Location rio = new Location(-22.906847, -43.172896);
        
        double distance = sp.distanceTo(rio);
        // Approximate distance between SP and Rio is ~360 km
        assertEquals(360.0, distance, 10.0);
    }

    @Test
    void shouldCalculateDistanceBetweenNewYorkAndLondon() {
        // New York
        Location ny = new Location(40.712776, -74.005974);
        // London
        Location london = new Location(51.507351, -0.127758);
        
        double distance = ny.distanceTo(london);
        // Approximate distance between NY and London is ~5570 km
        assertEquals(5570.0, distance, 50.0);
    }

    @Test
    void shouldCalculateDistanceBetweenNorthAndSouthPole() {
        Location northPole = new Location(90.0, 0.0);
        Location southPole = new Location(-90.0, 0.0);
        
        double distance = northPole.distanceTo(southPole);
        // Distance should be approximately half Earth's circumference (~20,000 km)
        assertEquals(20000.0, distance, 100.0);
    }

    @Test
    void shouldRejectNullLocationInDistanceCalculation() {
        Location location = new Location(-23.550520, -46.633308);
        assertThrows(IllegalArgumentException.class, 
            () -> location.distanceTo(null));
    }

    @Test
    void shouldHaveEqualityBasedOnCoordinates() {
        Location location1 = new Location(-23.550520, -46.633308);
        Location location2 = new Location(-23.550520, -46.633308);
        Location location3 = new Location(-22.906847, -43.172896);
        
        assertEquals(location1, location2);
        assertNotEquals(location1, location3);
    }

    @Test
    void shouldHaveSameHashCodeForEqualLocations() {
        Location location1 = new Location(-23.550520, -46.633308);
        Location location2 = new Location(-23.550520, -46.633308);
        
        assertEquals(location1.hashCode(), location2.hashCode());
    }

    @Test
    void shouldFormatToStringWithCoordinates() {
        Location location = new Location(-23.550520, -46.633308);
        String str = location.toString();
        
        assertTrue(str.contains("-23.55052"));
        assertTrue(str.contains("-46.63330"));
    }

    @Test
    void shouldBeSymmetricInDistanceCalculation() {
        Location sp = new Location(-23.550520, -46.633308);
        Location rio = new Location(-22.906847, -43.172896);
        
        double distance1 = sp.distanceTo(rio);
        double distance2 = rio.distanceTo(sp);
        
        assertEquals(distance1, distance2, 0.001);
    }
}
