package com.itm.space.backendresources;

import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.exception.BackendResourcesException;
import com.itm.space.backendresources.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "myuser", password = "test", roles = "MODERATOR")
public class UserControllerTest extends BaseIntegrationTest {

    private UserServiceImpl userService;
    @Mock
    private RoleMappingResource roleMappingResource;

    @Mock
    private MappingsRepresentation mappingsRepresentation;
    @Mock
    private RoleRepresentation roleRepresentation;

    @MockBean
    private Keycloak keycloakClient;

    private RealmResource realm;

    private UsersResource usersResource;

    private Response response;
    private UserResource userResource;

    private UserRepresentation userRepresentation;
    private UserRequest userRequest;

    @BeforeEach
     void setup() {
        realm = mock(RealmResource.class);
        usersResource = mock(UsersResource.class);
        response = mock(Response.class);
        userResource = mock(UserResource.class);
        userRequest = new UserRequest("mike", "test@email.com", "password", "firstName", "lastName"
        );
    }


    @Test
    public void testCreate() throws Exception {
        when(keycloakClient.realm(ArgumentMatchers.anyString())).thenReturn(realm);
        when(realm.users()).thenReturn(usersResource);
        when(usersResource.create(ArgumentMatchers.any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatusInfo()).thenReturn(Response.Status.CREATED);


        mvc.perform(requestWithContent(post("/api/users"), userRequest)).andDo(print()).andExpect(status().is2xxSuccessful());
    }
    @Test
    public void testCreateError() throws Exception {
        when(keycloakClient.realm(ArgumentMatchers.anyString())).thenReturn(realm);
        when(realm.users()).thenReturn(usersResource);
        when(usersResource.create(ArgumentMatchers.any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatusInfo()).thenThrow(new WebApplicationException("Error", Response.Status.BAD_REQUEST));


        mvc.perform(requestWithContent(post("/api/users"), userRequest))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }


    @Test
    public void testGetUserById() throws Exception {
        UUID userId = UUID.randomUUID();
        UserRepresentation userRepresentation = new UserRepresentation();
        List<RoleRepresentation> userRoles = Collections.singletonList(new RoleRepresentation());
        List<GroupRepresentation> userGroups = Collections.singletonList(new GroupRepresentation());

        when(keycloakClient.realm(anyString())).thenReturn(realm);
        when(realm.users()).thenReturn(usersResource);
        when(usersResource.get(userId.toString())).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(userRepresentation);

        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.getAll()).thenReturn(mappingsRepresentation);
        when(mappingsRepresentation.getRealmMappings()).thenReturn(userRoles);

        mvc.perform(get("/api/users/{id}", userId)).andDo(print()).andExpect(status().is2xxSuccessful());
    }

    @Test
    public void testGetUserByIdError() throws Exception {
        UUID userId = UUID.randomUUID();
        UserRepresentation userRepresentation = new UserRepresentation();
        List<RoleRepresentation> userRoles = Collections.singletonList(new RoleRepresentation());
        List<GroupRepresentation> userGroups = Collections.singletonList(new GroupRepresentation());

        when(keycloakClient.realm(anyString())).thenReturn(realm);
        when(realm.users()).thenReturn(usersResource);
        when(usersResource.get(userId.toString())).thenThrow(new BackendResourcesException("Bad request", HttpStatus.BAD_REQUEST));
        when(userResource.toRepresentation()).thenReturn(userRepresentation);

        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.getAll()).thenReturn(mappingsRepresentation);
        when(mappingsRepresentation.getRealmMappings()).thenReturn(userRoles);

        mvc.perform(get("/api/users/{id}", userId)).andDo(print()).andExpect(status().is5xxServerError());
    }


    @Test
    public void testHello() throws Exception {
        MvcResult mvcResult = mvc.perform(
                        get("/api/users/hello")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        String expectedUsername = "myuser";

        assertEquals(expectedUsername, responseContent);
    }
}
