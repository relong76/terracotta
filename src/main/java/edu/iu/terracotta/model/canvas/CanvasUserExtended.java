package edu.iu.terracotta.model.canvas;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.ksu.canvas.annotation.CanvasObject;
import edu.ksu.canvas.model.User;

@CanvasObject(postKey = "user")
public class CanvasUserExtended extends User {

    @JsonProperty("enrollment_type[]")
    private String enrollmentType;

    @JsonProperty("enrollment_state[]")
    private String enrollmentState;

}
