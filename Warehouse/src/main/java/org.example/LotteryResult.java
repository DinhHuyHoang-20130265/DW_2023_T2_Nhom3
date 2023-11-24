package org.example;

import lombok.*;

import java.util.List;

@Getter
@ToString
@RequiredArgsConstructor
public class LotteryResult {
    @NonNull
    @Setter
    private String lotteryDomain;
    @Setter
    @NonNull
    private String province;
    @Setter
    @NonNull
    private String date;
    @Setter
    @NonNull
    private String tenGiai;
    @Setter
    @NonNull
    private String soTrungThuong;

}
