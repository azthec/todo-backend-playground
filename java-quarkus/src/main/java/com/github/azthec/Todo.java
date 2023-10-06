package com.github.azthec;

import jakarta.persistence.*;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class Todo extends PanacheEntity {
    public String title;
    public Boolean completed = false;
    @Column(name = "\"order\"")
    public Integer order = 0;
    @Transient
    public String url;
}
