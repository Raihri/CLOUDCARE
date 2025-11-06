package com.example.cloud.care.var;
import lombok.Data;

import java.sql.Date;

@Data
public class DiseaseHistory {
    private Date date;
    private String diseaseType;
    private String currentState;
}