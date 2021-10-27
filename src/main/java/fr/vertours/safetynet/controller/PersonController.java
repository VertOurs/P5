package fr.vertours.safetynet.controller;

import fr.vertours.safetynet.dto.EmailDTO;
import fr.vertours.safetynet.dto.FireDTO;
import fr.vertours.safetynet.dto.PersonDTO;
import fr.vertours.safetynet.dto.PersonInfoDTO;
import fr.vertours.safetynet.model.MedicalRecord;
import fr.vertours.safetynet.model.Person;
import fr.vertours.safetynet.service.MedicalRecordService;
import fr.vertours.safetynet.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class PersonController {

    private final PersonService personService;
    private MedicalRecordService medicalRecordService;


    public PersonController(PersonService personService, MedicalRecordController medicalRecordController) {
        this.personService = personService;
    }


    @GetMapping("/person/all")
    public List<PersonDTO> getListOfPersons() {
        List<Person> personList = this.personService.getAllPersons();
        List<PersonDTO> personDTOList = personList.stream()
                .map(PersonDTO::fromPerson)
                .collect(Collectors.toList());
        return personDTOList;
    }

    @GetMapping(path = "/person/{firstName}/{lastName}")
    public PersonDTO getOnePerson(@PathVariable ("firstName") String firstName,
                                  @PathVariable ("lastName") String lastName) {
        Person person = personService.find(firstName, lastName);
        PersonDTO personDTO = new PersonDTO(person.getFirstName(),
                person.getLastName(),
                person.getAddress().getAddressName(),
                person.getCity(),
                person.getZip(),
                person.getPhone(),
                person.getEmail());
        return personDTO;
    }
    /* ****************************************  ENDPOINT 7  ************************************************************ */
    @GetMapping("/communityEmail{city}")
    public List<EmailDTO> getAllEmailForOneCity(@PathVariable("city") String city) {
        List<Person> personList = this.personService.findByCity(city);
        List<EmailDTO> emailDTO = personList.stream().map(EmailDTO::fromPerson).collect(Collectors.toList());
        return emailDTO;
    }
    /* ****************************************  ENDPOINT 6  ************************************************************* */
    @GetMapping("/personInfo{firstName}{lastName}")
    public PersonInfoDTO getNameAddressAgeMailMedicationsAndAllergies(@PathVariable("firstName") String firstName, @PathVariable("lastName") String LastName) {
        Person person = this.personService.find(firstName, LastName);
        MedicalRecord medicalRecord = new MedicalRecord(person);
        PersonInfoDTO personInfoDTO =  new PersonInfoDTO(person, medicalRecord);
        return personInfoDTO;
    }
    /* *****************************************  ENDPOINT 4  ************************************************************* */
    @GetMapping("/fire{address}")
    public List<FireDTO> getListOfPersonForOneAddressWithFireStation(@PathVariable("address") String address) {
    List<Person> personList = this.personService.findByAddress(address);
    List<FireDTO> fireDTOList = new ArrayList<>();
    for(Person p : personList) {
        MedicalRecord medicalRecord = new MedicalRecord(p);
        FireDTO fireDTO = new FireDTO();
        fireDTO.setFirstName(p.getFirstName());
        fireDTO.setLastName(p.getLastName());
        fireDTO.setPhone(p.getPhone());
        fireDTO.setAge(medicalRecord.getBirthDate().toString());
        fireDTO.setMedicationSet(medicalRecord.getMedications());
        fireDTO.setAllergySet((medicalRecord.getAllergies()));
        fireDTOList.add(fireDTO);
    }
        return fireDTOList;
    }





    @PostMapping("/person")
    public void registerNewPerson(@RequestBody PersonDTO personDTO) {
        personService.addPerson(personDTO);
    }
    // visiblement il y a un probleme (cascade)                                            !!*************!!
    @PutMapping(path = "/person/{firstName}/{lastName}")
    public void updatePerson(@PathVariable("firstName") String firstName,
                             @PathVariable("lastName") String lastName,
                             @RequestBody PersonDTO personDTO ) {
        personService.updatePerson(firstName, lastName, personDTO);
    }


    @DeleteMapping(path = "/person/{firstName}/{lastName}")
    public void deletePerson(@PathVariable("firstName") String firstName,
                             @PathVariable("lastName") String lastName) {
        personService.deletePerson(firstName, lastName);
    }
}
