package pl.jakubheppner.githubrepositoriesproxy;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GithubRepositoriesIntegrationTest {

    private static final WireMockServer githubApi = new WireMockServer(options().dynamicPort());

    @BeforeAll
    static void startWireMock() {
        githubApi.start();
    }

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void overrideGithubApiBaseUrl(DynamicPropertyRegistry registry) {
        registry.add("github.api.base-url", githubApi::baseUrl);
    }

    @BeforeEach
    void resetWireMock() {
        githubApi.resetAll();
    }

    @AfterAll
    static void stopWireMock() {
        githubApi.stop();
    }

    @Test
    void shouldReturnOnlyNonForkRepositoriesWithBranches() throws Exception {
        githubApi.stubFor(get(urlEqualTo("/users/octocat/repos"))
                .willReturn(jsonResponse("""
                        [
                          {
                            "name": "first-repo",
                            "fork": false,
                            "owner": {
                              "login": "octocat"
                            }
                          },
                          {
                            "name": "forked-repo",
                            "fork": true,
                            "owner": {
                              "login": "octocat"
                            }
                          },
                          {
                            "name": "second-repo",
                            "fork": false,
                            "owner": {
                              "login": "octocat"
                            }
                          }
                        ]
                        """)));
        githubApi.stubFor(get(urlEqualTo("/repos/octocat/first-repo/branches"))
                .willReturn(jsonResponse("""
                        [
                          {
                            "name": "main",
                            "commit": {
                              "sha": "first-main-sha"
                            }
                          },
                          {
                            "name": "develop",
                            "commit": {
                              "sha": "first-develop-sha"
                            }
                          }
                        ]
                        """)));
        githubApi.stubFor(get(urlEqualTo("/repos/octocat/second-repo/branches"))
                .willReturn(jsonResponse("""
                        [
                          {
                            "name": "main",
                            "commit": {
                              "sha": "second-main-sha"
                            }
                          }
                        ]
                        """)));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/users/octocat/repositories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].repositoryName").value("first-repo"))
                .andExpect(jsonPath("$[0].ownerLogin").value("octocat"))
                .andExpect(jsonPath("$[0].branches.length()").value(2))
                .andExpect(jsonPath("$[0].branches[0].name").value("main"))
                .andExpect(jsonPath("$[0].branches[0].lastCommitSha").value("first-main-sha"))
                .andExpect(jsonPath("$[0].branches[1].name").value("develop"))
                .andExpect(jsonPath("$[0].branches[1].lastCommitSha").value("first-develop-sha"))
                .andExpect(jsonPath("$[1].repositoryName").value("second-repo"))
                .andExpect(jsonPath("$[1].ownerLogin").value("octocat"))
                .andExpect(jsonPath("$[1].branches.length()").value(1))
                .andExpect(jsonPath("$[1].branches[0].name").value("main"))
                .andExpect(jsonPath("$[1].branches[0].lastCommitSha").value("second-main-sha"))
                .andExpect(jsonPath("$[?(@.repositoryName == 'forked-repo')]").isEmpty());

        githubApi.verify(0, getRequestedFor(urlEqualTo("/repos/octocat/forked-repo/branches")));
    }

    @Test
    void shouldReturnNotFoundResponseWhenGithubUserDoesNotExist() throws Exception {
        githubApi.stubFor(get(urlEqualTo("/users/unknown-user/repos"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", APPLICATION_JSON.toString())
                        .withBody("""
                                {
                                  "message": "Not Found"
                                }
                                """)));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/users/unknown-user/repositories"))
                .andExpect(status().isNotFound())
                .andExpect(content().json("""
                        {
                          "status": 404,
                          "message": "GitHub user not found"
                        }
                        """));
    }

    @Test
    void shouldReturnEmptyListWhenUserHasOnlyForkRepositories() throws Exception {
        githubApi.stubFor(get(urlEqualTo("/users/only-forks/repos"))
                .willReturn(jsonResponse("""
                        [
                          {
                            "name": "first-fork",
                            "fork": true,
                            "owner": {
                              "login": "only-forks"
                            }
                          },
                          {
                            "name": "second-fork",
                            "fork": true,
                            "owner": {
                              "login": "only-forks"
                            }
                          }
                        ]
                        """)));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/users/only-forks/repositories"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        githubApi.verify(0, getRequestedFor(urlEqualTo("/repos/only-forks/first-fork/branches")));
        githubApi.verify(0, getRequestedFor(urlEqualTo("/repos/only-forks/second-fork/branches")));
    }

    private static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder jsonResponse(String body) {
        return aResponse()
                .withStatus(200)
                .withHeader("Content-Type", APPLICATION_JSON.toString())
                .withBody(body);
    }
}
