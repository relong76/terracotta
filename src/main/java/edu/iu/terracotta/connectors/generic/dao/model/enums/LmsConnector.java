package edu.iu.terracotta.connectors.generic.dao.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LmsConnector {

    BRIGHTSPACE("Brightspace", true),
    CANVAS("Canvas", true),
    GENERIC("Generic", false),
    ONE_ED_TECH("1EdTech", false);

    private String title;
    // whether title names a product people know as their LMS, so it can be shown to them
    // (1EdTech is a standards body, and Generic isn't a product)
    private boolean namedLms;

    public String title() {
        return title;
    }

}
