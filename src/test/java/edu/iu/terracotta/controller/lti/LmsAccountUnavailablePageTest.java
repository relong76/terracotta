package edu.iu.terracotta.controller.lti;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

// the page a launch gets while its LMS account isn't approved, rendered from the real template
public class LmsAccountUnavailablePageTest {

    private static String render(boolean pending) {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);

        Context context = new Context();
        context.setVariable("pending", pending);

        return engine.process(Lti3Controller.LMS_ACCOUNT_UNAVAILABLE, context);
    }

    @Test
    public void testAPendingSchoolIsToldItsWaitingForApproval() {
        String page = render(true);

        assertTrue(page.contains("<title>Terracotta is waiting for approval</title>"), page);
        assertTrue(page.contains("approves your school"), page);
        assertTrue(page.contains("mailto:info@terracotta.education"), page);
        assertFalse(page.contains("can&#39;t be used at your school right now") || page.contains("can't be used at your school right now"), page);
    }

    @Test
    public void testARejectedOrRevokedSchoolIsToldTerracottaIsntAvailable() {
        String page = render(false);

        assertTrue(page.contains("Terracotta isn") && page.contains("t available</title>"), page);
        assertTrue(page.contains("mailto:info@terracotta.education"), page);
        assertFalse(page.contains("waiting for approval"), page);
    }

}
