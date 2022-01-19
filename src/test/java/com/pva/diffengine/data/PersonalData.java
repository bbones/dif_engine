package com.pva.diffengine.data;

import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class PersonalData {

    @Getter @Setter private Long taxCode;
    @Getter @Setter private String firstName;
    @Getter @Setter private String lastName;
    @Getter @Setter private LocalDate birthDate;
    @Getter @Setter private Address[] addresses;
    @Getter @Setter private Contact[] contacts;
    @Getter @Setter private String[] random;
}
