package com.github.azthec;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.net.URL;

@Entity
public class Todo {
    @Id
    public Long id;
    public String title;
    public Boolean completed;
    public Integer order;
    public URL url;
}
