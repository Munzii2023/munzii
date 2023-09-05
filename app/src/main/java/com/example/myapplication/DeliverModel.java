package com.example.myapplication;

import com.google.type.DateTime;
import com.naver.maps.geometry.Coord;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliverModel {
    private int pm10value;
    private int pm25value;
    private String location; //String? Coord? ->String으로 받고 Coord로 캐스트해도됨
    private String data_time;
}
