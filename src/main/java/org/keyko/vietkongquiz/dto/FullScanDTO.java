package org.keyko.vietkongquiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FullScanDTO {
    private String gameType;
    private int postCount;
    private int gamesHandledCount;
    private int gamesHandledWithErrorsCount;
}
