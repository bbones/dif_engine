package com.pva.diffengine.data;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Client {
    @Getter @Setter
    private Long clientId;
    @Getter @Setter
    private String clientClass;
    @Getter @Setter
    private Integer type;

    @Getter @Setter
    private PersonalData personalData;
}
