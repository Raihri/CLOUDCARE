package com.example.cloud.care.var;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
public class VaccinationHistory {
    private Date date;
    private String vaccineType;
}