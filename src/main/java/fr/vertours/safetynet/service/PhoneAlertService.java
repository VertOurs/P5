package fr.vertours.safetynet.service;

import fr.vertours.safetynet.model.Address;
import fr.vertours.safetynet.model.FireStation;
import fr.vertours.safetynet.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class PhoneAlertService implements IPhoneAlertService {

    private final static Logger LOGGER = LoggerFactory.getLogger(PhoneAlertService.class);

    @Autowired
    FireStationService fireStationService;

    @Autowired
    PersonService personService;

    /**
     * find list of person by Station number.
     * @param station
     * @return a list of Person entity.
     */
    @Override
    public List<Person> findByStation(int station) {
        LOGGER.info("Call findByStation method");
        FireStation fireStation = fireStationService.findOneStation(station);
        Set<Address> addressSet = fireStation.getAddress();
        return personService.findByAddressIn(addressSet);
    }
}
