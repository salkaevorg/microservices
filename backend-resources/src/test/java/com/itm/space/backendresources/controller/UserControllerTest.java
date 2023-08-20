package com.itm.space.backendresources.controller;

import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.mapper.UserMapper;
import com.itm.space.backendresources.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WithMockUser(username = "test", password = "test", roles = "MODERATOR")
public class UserControllerTest extends BaseIntegrationTest {

    @MockBean
    private UserServiceImpl userService;

    @MockBean
    private Keycloak keycloakClient;

    @Mock
    private RealmResource realm;

    @Mock
    UserMapper userMapper;

    @Mock
    private UsersResource usersResource;

    @Mock
    private Response response;

    @Mock
    UserRepresentation userRepresentation;

    @BeforeEach
    public void setup() {
        when(keycloakClient.realm(anyString())).thenReturn(realm);
        when(realm.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(201);
    }


    @Test
    public void testCreate() throws Exception {
        UserRequest userRequest = new UserRequest(
                "mike",
                "test@email.com",
                "password",
                "firstName",
                "lastName"
        );
        mvc.perform(
                        requestWithContent(post("/api/users"), userRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is2xxSuccessful())
                .andDo(print());
        verify(userService, times(1)).createUser(any(UserRequest.class));
    }


    @Test
    public void testGetUserById() throws Exception {
        UUID userId = UUID.randomUUID();
        List<String> roles = Arrays.asList("ROLE_USER", "ROLE_MODERATOR");
        List<String> groups = Arrays.asList("Group1", "Group2");

        UserResponse expectedUserResponse = new UserResponse(
                "test",
                "test",
                "test@mail.ru",
                roles,
                groups
        );

        when(userService.getUserById(userId)).thenReturn(expectedUserResponse);

        mvc.perform(
                        get("/api/users/{id}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.firstName").value("test"))
                .andExpect(jsonPath("$.lastName").value("test"))
                .andExpect(jsonPath("$.email").value("test@mail.ru"));
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
        String expectedUsername = "test";

        assertEquals(expectedUsername, responseContent);
    }
}
