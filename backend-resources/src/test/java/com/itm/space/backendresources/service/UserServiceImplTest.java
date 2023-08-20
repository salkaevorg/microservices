package com.itm.space.backendresources.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.controller.UserController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.ws.rs.core.Response;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
@WithMockUser(username = "test", password = "test", roles = "MODERATOR")
public class UserServiceImplTest extends BaseIntegrationTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserController userController;

    @MockBean
    private Keycloak keycloakClient;

    @Mock
    private RealmResource realm;

    @Mock
    private UsersResource usersResource;

    @Mock
    private Response response;

    @Test
    public void testCreateUser() throws Exception {
        UserRequest userRequest = new UserRequest(
                "mike",
                "test@email.com",
                "password",
                "test",
                "test"
        );

        when(keycloakClient.realm(anyString())).thenReturn(realm);
        when(realm.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(201);

        MockHttpServletRequestBuilder requestBuilder = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userRequest));

        mvc.perform(requestBuilder).andExpect(status().is2xxSuccessful());

        verify(userService, times(1)).createUser(any(UserRequest.class));
    }
}
