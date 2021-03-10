
package com.ajay.example.userApp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Date;
import lombok.Data;


@Data
@JsonIgnoreProperties
@JsonSerialize
@JsonDeserialize
public class User {

    @JsonProperty
    private String name;
    @JsonProperty
    private String userId;
    @JsonProperty
    private Date creationDate;

}
