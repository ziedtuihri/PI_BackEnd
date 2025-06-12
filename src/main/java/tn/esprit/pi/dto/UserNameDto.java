package tn.esprit.pi.dto;

public class UserNameDto {

    private Integer id;
    private String firstName;
    private String lastName;

    public UserNameDto(String firstName, String lastName,Integer id) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getters
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Integer getId() {
        return id;
    }
}