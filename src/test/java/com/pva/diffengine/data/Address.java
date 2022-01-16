package com.pva.diffengine.data;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Address {
    @Getter @Setter private Long recordId;
    @Getter @Setter private String city;
    @Getter @Setter private String street;
    @Getter @Setter private Integer house;
}
