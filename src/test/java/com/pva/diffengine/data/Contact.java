package com.pva.diffengine.data;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Contact {
    @Getter @Setter private Long id;
    @Getter @Setter private Integer type;
    @Getter @Setter private String value;
}
