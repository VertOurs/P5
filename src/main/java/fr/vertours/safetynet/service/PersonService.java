package fr.vertours.safetynet.service;

import fr.vertours.safetynet.dto.*;
import fr.vertours.safetynet.model.Address;
import fr.vertours.safetynet.model.FireStation;
import fr.vertours.safetynet.model.MedicalRecord;
import fr.vertours.safetynet.model.Person;
import fr.vertours.safetynet.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PersonService {

    private final PersonRepository personRepository;

    PersonDTO personDTO;

    @Autowired
    AddressService addressService;
    @Autowired
    FireStationService fireStationService;
    @Autowired
    MedicalRecordService medicalRecordService;
    @Autowired
    MedicationService medicationService;
    @Autowired
    AllergyService allergyService;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }


    public void addPerson(PersonDTO personDTO) {
        Person person = personDTO.createPerson();
        Address address = addressService.find(personDTO.getAddress());
        if(address == null) {
            address = addressService.save(personDTO.getAddress());
        }
        person.setAddress(address);
        personRepository.save(person);
    }
    public void saveAll(Collection<Person> collection) {
        personRepository.saveAll(collection);
    }
    public List<Person> getAllPersons() {
        return personRepository.findAll();
    }

    public void deletePerson(String firstName, String lastName) {
        Person person = find(firstName, lastName);
        personRepository.delete(person);
    }
    public Person find(String firstName, String lastName) {
        return personRepository.findOneByFirstNameAndLastName(firstName,lastName);
    }
    public List<Person> findByLastName(String lastname) {
        return personRepository.findByLastName(lastname);
    }
    public List<Person> findByCity(String city) {
        return personRepository.findAllByCity(city);
    }
    public List<Person> findByAddress(String address) {
        return personRepository.findByAddress_AddressName(address);
    }
    public List<Person> findByStation(int station) {
        FireStation fireStation = fireStationService.findOneStation(station);
        Set<Address> addressSet = fireStation.getAddress();
        return personRepository.findByAddressIn(addressSet);
    }
    public void updatePerson(String firstName, String lastName, PersonDTO personDTO) {
        Person person = personRepository.findOneByFirstNameAndLastName(firstName, lastName);

        if(personDTO.getAddress() != null) {
            Address address = new Address(personDTO.getAddress());
            addressService.save(address);
            person.setAddress(address);
        }
        if (personDTO.getCity() != null) {
            person.setCity(personDTO.getCity());
        }
        if (personDTO.getZip() != null) {
            person.setZip(personDTO.getZip());
        }
        if (personDTO.getPhone() != null) {
            person.setPhone(personDTO.getPhone());
        }
        if (personDTO.getEmail() != null) {
            person.setEmail(personDTO.getEmail());
        }
        personRepository.save(person);
    }
    public AllInfoPersonDTO createAllInfoPersonDTO(String firstName, String lastName) {
        Person person = personRepository.findOneByFirstNameAndLastName(firstName, lastName);
        MedicalRecord medicalRecord = medicalRecordService.find(firstName, lastName);
        Collection<FireStation> fireStationSet = fireStationService.findOneStationByAddress(person.getAddress());
        Set<Integer> stationSet = new HashSet<>();
        for(FireStation fireStation : fireStationSet) {
            stationSet.add(fireStation.getStation());
        }

        AllInfoPersonDTO dto = new AllInfoPersonDTO();

        dto.setFirstName(person.getFirstName());
        dto.setLastName(person.getLastName());
        dto.setAddress(person.getAddress().getAddressName());
        dto.setCity(person.getCity());
        dto.setZip(person.getZip());
        dto.setPhone(person.getPhone());
        dto.setEmail(person.getEmail());
        dto.setBirthdate(medicalRecord.getBirthDate());
        dto.setMedications(medicationService.makeStringSetFromMedication(medicalRecord.getMedications()));
        dto.setAllergies(allergyService.makeStringSetFromAllergy(medicalRecord.getAllergies()));
        dto.setStation(stationSet);
        return dto;
    }

    public List<FireDTO> getListOfPersonForOneAddressWithFireStation(String address) {
        return medicalRecordService.getFireURL(address);
    }
    public List<PersonForFireInfoDTO> personFromFireStation(List<Person> personList) {
       List<PersonForFireInfoDTO> infoDTOS = personList.stream().map(PersonForFireInfoDTO::fromPerson).collect(Collectors.toList());
      return infoDTOS;
    }

    public FireStationInfoDTO getFireStationInfoDTOFromList (List<PersonForFireInfoDTO> personInfoList, List<Person> personList) {
        FireStationInfoDTO fireStationInfoDTO = new FireStationInfoDTO();
        for(Person p : personList) {
            PersonForFireInfoDTO personFireInfo = new PersonForFireInfoDTO(p.getFirstName(), p.getLastName(),p.getAddress().getAddressName(), p.getPhone());
            MedicalRecord medicalRecord = medicalRecordService.find(p.getFirstName(), p.getLastName());
            LocalDate now = LocalDate.now();
            Period period = Period.between(now, medicalRecord.getBirthDate());
            int age = Math.abs(period.getYears());
            if(age >= 18) {
                fireStationInfoDTO.setNbAdultes(fireStationInfoDTO.getNbAdultes()+1);
            } else {
                fireStationInfoDTO.setNbEnfants(fireStationInfoDTO.getNbEnfants()+1);
            }
            personInfoList.add(personFireInfo);
        }
        fireStationInfoDTO.setPersonList(personInfoList);
        return fireStationInfoDTO;
    }

}
