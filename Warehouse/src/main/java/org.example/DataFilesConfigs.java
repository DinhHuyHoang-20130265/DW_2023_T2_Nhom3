package org.example;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DataFilesConfigs {
    private int id;
    private String name;
    private String description;
    private String source_path;
    private String location;
    private int flag;

}
