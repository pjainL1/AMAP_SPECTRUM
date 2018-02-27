package com.korem.spectrum;

/**
 *
 * @author jphoude
 */
public class BoundaryInput {
        private Double longitude;
        private Double latitude;
        private String cost;
        
        public BoundaryInput(Double longitude, Double latitude, String cost) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.cost = cost;
        }

        public Double getLongitude() {
            return longitude;
        }

        public Double getLatitude() {
            return latitude;
        }

        public String getCost() {
            return cost;
        }
    }
