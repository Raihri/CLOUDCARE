package com.example.cloud.care.var;
import lombok.Data;

import java.sql.Date;

@Data
public class AccidentHistory {

    private Date date;
    private String accidentType;
    private String currentState;
    private String restrictions;

}

