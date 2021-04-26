package org.keyko.vietkongquiz.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoundResultDTO {
    private long roundResultId;
    private long teamResultId;
    private short round;
    private String result;
}
