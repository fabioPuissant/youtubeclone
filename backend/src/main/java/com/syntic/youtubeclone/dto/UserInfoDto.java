package com.syntic.youtubeclone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserInfoDto {
    private String id;
    @JsonProperty("sub")
    private String sub;

    @JsonProperty("given_name")
    private String givenName;
    @JsonProperty("family_name")

    private String familyName;
    @JsonProperty("name")

    private String name;
    @JsonProperty("picture")
    private String picture;
    @JsonProperty("email")
    private String email;


}
