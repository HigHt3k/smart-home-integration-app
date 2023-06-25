package com.home_app.controller.task;

import com.home_app.model.dump1090.Dump1090Data;
import com.home_app.model.dump1090.FlightLog;
import com.home_app.model.dump1090.FlightLogRepository;
import com.home_app.model.dump1090.FlightPathRepository;
import com.home_app.model.planespotting.Aircraft;
import com.home_app.model.planespotting.AircraftRepository;
import com.home_app.service.Dump1090DataQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Optional;

@Component
public class SaveDump1090DataToDatabaseTask {

    private final Logger logger = LoggerFactory.getLogger(SaveDump1090DataToDatabaseTask.class);

    private final Dump1090DataQueue dataQueue;

    private final FlightLogRepository flightLogRepository;
    private final AircraftRepository aircraftRepository;
    private final FlightPathRepository flightPathRepository;

    @Autowired
    public SaveDump1090DataToDatabaseTask(Dump1090DataQueue dataQueue,
                                          FlightLogRepository flightLogRepository,
                                          AircraftRepository aircraftRepository,
                                          FlightPathRepository flightPathRepository) {
        this.dataQueue = dataQueue;
        this.flightLogRepository = flightLogRepository;
        this.aircraftRepository = aircraftRepository;
        this.flightPathRepository = flightPathRepository;
    }

    @Scheduled(fixedRate = 1000)
    public void saveDataToDatabase() {
        int currentQueueSize = dataQueue.currentQueueSize();
        for(int i = 0; i < currentQueueSize; i++) {
            Dump1090Data data = dataQueue.getNextData();
            updateFlightLog(data);
        }
    }

    private void updateFlightLog(Dump1090Data data) {
        int icao24 = data.getIcao24Decimal();
        Optional<FlightLog> flightLog = flightLogRepository.findExistingFlight(icao24, new Timestamp(System.currentTimeMillis() - 1000*60*60));
        if(flightLog.isEmpty()) {
            FlightLog newFlightLog = new FlightLog();
            Optional<Aircraft> aircraft = aircraftRepository.findById(icao24);
            aircraft.ifPresent(newFlightLog::setAircraft);
            newFlightLog.setFirstAltitude(data.getAltitude());
            newFlightLog.setLastAltitude(data.getAltitude());
            newFlightLog.setFirstLatitude(data.getLatitude());
            newFlightLog.setFirstLongitude(data.getLongitude());
            newFlightLog.setLastLatitude(data.getLatitude());
            newFlightLog.setLastLongitude(data.getLongitude());
            newFlightLog.setEmergencyFlag(data.getEmergencyFlag());
            if(data.getCallsign() != null) {
                newFlightLog.setCallsign(data.getCallsign());
            } else {
                newFlightLog.setCallsign("");
            }

            Timestamp timestamp;
            if(data.getDateMessageGenerated() != null && data.getTimeMessageGenerated() != null) {
                timestamp = Timestamp.valueOf(data.getDateMessageGenerated().replace("/", "-")
                        + " " + data.getTimeMessageGenerated());
            } else {
                timestamp = new Timestamp(System.currentTimeMillis());
            }
            newFlightLog.setFirstTs(timestamp);
            newFlightLog.setLastTs(timestamp);
            flightLogRepository.save(newFlightLog);
        } else {
            if(data.getCallsign() != null && !data.getCallsign().isEmpty() && flightLog.get().getCallsign().isEmpty()) {
                flightLog.get().setCallsign(data.getCallsign());
            }
            if(data.getLatitude() != 0) {
                flightLog.get().setLastLatitude(data.getLatitude());
                if(flightLog.get().getFirstLatitude() == 0) {
                    flightLog.get().setFirstLatitude(data.getLatitude());
                }
            }
            if(data.getLongitude() != 0) {
                flightLog.get().setLastLongitude(data.getLongitude());
                if(flightLog.get().getFirstLongitude() == 0) {
                    flightLog.get().setFirstLongitude(data.getLongitude());
                }
            }
            if(data.getAltitude() != 0) {
                flightLog.get().setLastAltitude(data.getAltitude());
                if(flightLog.get().getFirstAltitude() == 0) {
                    flightLog.get().setFirstAltitude(data.getAltitude());
                }
            }
            if(data.getDateMessageGenerated() != null && data.getTimeMessageGenerated() != null) {
                flightLog.get().setLastTs(Timestamp.valueOf(data.getDateMessageGenerated().replace("/", "-")
                        + " " + data.getTimeMessageGenerated()));
            }
            flightLogRepository.save(flightLog.get());
        }

        // update the flight path

    }
}
