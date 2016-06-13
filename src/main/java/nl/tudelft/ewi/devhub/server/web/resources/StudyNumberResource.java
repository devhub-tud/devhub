package nl.tudelft.ewi.devhub.server.web.resources;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.resteasy.plugins.validation.hibernate.ValidateRequest;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by jwgmeligmeyling on 13-6-16.
 */
@Slf4j
@Path(StudyNumberResource.STUDY_NUMBER_PATH)
@RequestScoped
public class StudyNumberResource extends Resource {

    public static final String STUDY_NUMBER_PATH = "/study-number";

    @Inject
    TemplateEngine engine;

    @Context
    HttpServletRequest httpServletRequest;

    @Inject
    @Named("current.user")
    User currentUser;

    @Inject
    Users users;

    @GET
    public String serveStudyNumberRequest(@QueryParam("error") String error) throws IOException {
        List<Locale> locales = Collections.list(httpServletRequest.getLocales());
        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("user", currentUser);
        if (!Strings.isNullOrEmpty(error)) {
            parameters.put("error", error);
        }

        return engine.process("study-number.ftl", locales, parameters);
    }

    @POST
    @Transactional
    public Response handleStudyNumberRequest(
        @NotEmpty @FormParam("studentNumber") String studentNumber,
        @NotEmpty @FormParam("studentNumberConfirm") String studentNumberConfirm,
        @QueryParam("redirect") @DefaultValue("courses") String redirectTo
    )
        throws URISyntaxException, LdapException, IOException {

        if (Strings.isNullOrEmpty(studentNumber)) {
            return Response.seeOther(new URI("/study-number?error=error.invalid.study-number-empty")).build();
        }

        if (!studentNumber.equals(studentNumberConfirm)) {
            return Response.seeOther(new URI("/study-number?error=error.invalid.study-number-confirm")).build();
        }

        currentUser.setStudentNumber(studentNumber);
        users.merge(currentUser);

        return Response.seeOther(new URI("/" + redirectTo)).build();

    }

}
