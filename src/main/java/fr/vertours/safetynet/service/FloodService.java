package fr.vertours.safetynet.service;

import fr.vertours.safetynet.dto.FloodContactDTO;
import fr.vertours.safetynet.dto.FloodDTO;
import fr.vertours.safetynet.model.Address;
import fr.vertours.safetynet.model.FireStation;
import fr.vertours.safetynet.model.MedicalRecord;
import fr.vertours.safetynet.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FloodService implements IFloodService {

    @Autowired
    FireStationService fireStationService;

    @Autowired
    MedicalRecordService medicalRecordService;

    @Autowired
    PersonService personService;


    @Override
    public List<FloodDTO> getFloodByListOfStation(List<Integer> stationList) {
        List<FireStation> fireStationList = fireStationService.getListFireStationByNb(stationList);
        List<FloodDTO> floodDTOList = getListFloodDTOWithFireStationList(fireStationList);
        return floodDTOList;

    }

    private List<FloodDTO> getListFloodDTOWithFireStationList(List<FireStation> fireStationList) {
        List<FloodDTO> floodDTOList = new ArrayList<>();
        for (FireStation f: fireStationList) {
            for (Address a : f.getAddress()) {
                List<Person> personList = personService.findListOfPersonByAddress(a) ;
                List<MedicalRecord> medicalRecordList = medicalRecordService.getMedicalRecordByListOfPerson(personList);
                List<FloodContactDTO> floodContactDTOList = FloodContactDTO.fromListPersonMr(personList,medicalRecordList);
                FloodDTO floodDTO = new FloodDTO();
                floodDTO.setStation(f.getStation());
                floodDTO.setAddress(a.getAddressName());
                floodDTO.setContacts(floodContactDTOList);
                floodDTOList.add(floodDTO);
            }
        }
        return floodDTOList;
    }
}
