package com.demo;

import lombok.Data;

import java.io.Serializable;

@Data
public class DemoColumn implements Serializable {
    private String name;
    private String type;
}
