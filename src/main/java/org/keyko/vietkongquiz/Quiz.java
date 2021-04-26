package org.keyko.vietkongquiz;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Quiz {
    WOW_QUIZ_NSK("wowquiz_nsk", "WOW_Quiz", 185, 33),
    EINSTEIN_PARTY_NSK("einsteinparty_nsk", "Einstein_Party", 185, 36);

    private final String domain;
    private final String gameType;
    private final int leftBlockSize;
    private final int headerBlockSize;
}
