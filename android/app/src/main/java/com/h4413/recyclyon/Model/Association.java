package com.h4413.recyclyon.Model;

import java.io.Serializable;

public class Association implements Serializable {

    public String _id;
    public String nom;
    public String description;
    public String logoUrl;

    public Association(String id, String nom, String description, String url) {
        this.logoUrl = url;
        this._id = id;
        this.description = description;
        this.nom = nom;
    }
}
