package cl.files;

import static java.util.stream.Collectors.*;

import static cl.files.Person.Gender.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("serial")
public class Person implements Serializable, Comparable<Person> {
    
    public enum Gender { 
        MALE, FEMALE;
        public static Gender m() { return MALE; }
        public static Gender f() { return FEMALE; }
    }
    
    @SuppressWarnings("serial")
    public static class Address implements Serializable {

        public Address(){}
        
        public Address(String street, int houseNumber) {
            this.street = street;
            this.houseNumber = houseNumber;
        }
        
        private String street;
        private int houseNumber;
        
        public String getStreet() {
            return street;
        }
        public void setStreet(String street) {
            this.street = street;
        }
        public int getHouseNumber() {
            return houseNumber;
        }
        public void setHouseNumber(int houseNumber) {
            this.houseNumber = houseNumber;
        }

        @Override
        public int hashCode() {
            return houseNumber + street.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            Address other = (Address) obj;
            return houseNumber == other.houseNumber && street.equals(other.street); 
        }
        
        @Override
        public String toString() {
            return houseNumber + " " + street;
        }
        
    }
    
    private String name;
    private LocalDate dob;
    private Gender gender;
    private Address address;
    
    public Person(){}
    
    public Person(String name, LocalDate dob, Gender gender, Address address) {
        this.name = name;
        this.dob = dob;
        this.gender = gender;
        this.address = address;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public LocalDate getDob() {
        return dob;
    }
    public void setDob(LocalDate dob) {
        this.dob = dob;
    }
    public Gender getGender() {
        return gender;
    }
    public void setGender(Gender gender) {
        this.gender = gender;
    }
    public Address getAddress() {
        return address;
    }
    public void setAddress(Address address) {
        this.address = address;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + address.hashCode();
        result = prime * result + dob.hashCode();
        result = prime * result + gender.hashCode();
        result = prime * result + name.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Person other = (Person) obj;
        if (name != null && gender != null && dob != null && address != null) {
            return name.equals(other.name) && gender == other.gender && dob.equals(other.dob) && address.equals(other.address);
        }
        return false;
    }
    
    @Override
    public String toString() {
        return name + " " + dob + " " + gender + " " + address;
    }
    
    @Override
    public int compareTo(Person o) {
        return name.compareTo(o.name);
    }

    public static List<Person> peopleDB() {
        return Arrays.asList(
                new Person("Vladimir", dob(1968, 2, 8), m(), a("Nelson Ave", 324)),
                new Person("Olga", dob(1967, 5, 23), f(), a("Nelson Ave", 324)),
                new Person("Alex", dob(1993, 11, 19), m(), a("Nelson Ave", 324)),
                new Person("Natali", dob(1971, 5, 21), f(), a("Bart Ave", 123)),
                new Person("Eugene", dob(1978, 10, 20), m(), a("12th Street", 324))
        );
    }
    
    public static List<Person> peopleDBInRussian() {
        return Arrays.asList(
                new Person("Влаимир", dob(1968, 2, 8), m(), a("Nelson Ave", 324)),
                new Person("Олга", dob(1967, 5, 23), f(), a("Nelson Ave", 324)),
                new Person("Алекс", dob(1993, 11, 19), m(), a("Nelson Ave", 324)),
                new Person("Наташа", dob(1971, 5, 21), f(), a("Bart Ave", 123)),
                new Person("Женя", dob(1978, 10, 20), m(), a("12th Street", 324))
        );
    }
    
    public static List<String> peopleDBStrings() {
        return peopleDB().stream().map(Objects::toString).collect(toList());
    }
    
    public static List<String> peopleDBStringsInRussian() {
        return peopleDBInRussian().stream().map(Objects::toString).collect(toList());
    }
    
    private static Address a(String street, int houseNumber) {
        return new Address(street, houseNumber);
    }
    
    private static LocalDate dob(int year, int month, int day) {
        return LocalDate.of(year, month, day);
    }
}
