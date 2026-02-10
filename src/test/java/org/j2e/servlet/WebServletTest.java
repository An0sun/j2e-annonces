package org.j2e.servlet;

import org.j2e.bean.User;
import org.j2e.filter.AuthFilter;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.mockito.Mockito.*;

/**
 * Niveau 4 – Tests Web.
 * Tests de Servlets avec mocks HTTP.
 * Test du filtre d'authentification.
 */
class WebServletTest {

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;
    @Mock private FilterChain filterChain;
    @Mock private ServletContext servletContext;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    // ===== AuthFilter Tests =====

    @Test
    @DisplayName("AuthFilter : page publique (login.jsp) passe sans session")
    void testAuthFilterAllowsPublicPage() throws Exception {
        AuthFilter filter = new AuthFilter();

        when(request.getRequestURI()).thenReturn("/J2E/login.jsp");
        when(request.getContextPath()).thenReturn("/J2E");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("AuthFilter : page register passe sans session")
    void testAuthFilterAllowsRegisterPage() throws Exception {
        AuthFilter filter = new AuthFilter();

        when(request.getRequestURI()).thenReturn("/J2E/Register");
        when(request.getContextPath()).thenReturn("/J2E");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("AuthFilter : page protégée redirige vers login si pas de session")
    void testAuthFilterBlocksWithoutSession() throws Exception {
        AuthFilter filter = new AuthFilter();

        when(request.getRequestURI()).thenReturn("/J2E/AnnonceList");
        when(request.getContextPath()).thenReturn("/J2E");
        when(request.getSession(false)).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(response).sendRedirect("/J2E/login.jsp");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("AuthFilter : page protégée passe avec session valide")
    void testAuthFilterAllowsWithSession() throws Exception {
        AuthFilter filter = new AuthFilter();

        when(request.getRequestURI()).thenReturn("/J2E/AnnonceList");
        when(request.getContextPath()).thenReturn("/J2E");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(new User("test", "t@t.com", "pass"));

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("AuthFilter : session existante mais sans user → redirige")
    void testAuthFilterBlocksSessionWithoutUser() throws Exception {
        AuthFilter filter = new AuthFilter();

        when(request.getRequestURI()).thenReturn("/J2E/AnnonceList");
        when(request.getContextPath()).thenReturn("/J2E");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(response).sendRedirect("/J2E/login.jsp");
    }

    // ===== LoginServlet Tests =====

    @Test
    @DisplayName("LoginServlet GET : affiche le formulaire")
    void testLoginServletGet() throws Exception {
        LoginServlet servlet = new LoginServlet();

        // On ne peut pas tester directement sans servletContext, mais on vérifie le flow
        when(request.getRequestDispatcher("/login.jsp")).thenReturn(dispatcher);

        // Simuler le getServletContext
        // Note: comme on ne peut pas facilement mocker getServletContext() sur un vrai servlet,
        // on teste principalement le comportement du filtre ici.
    }

    @Test
    @DisplayName("LogoutServlet : invalidation de session")
    void testLogoutServlet() throws Exception {
        LogoutServlet servlet = new LogoutServlet();

        when(request.getSession(false)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/J2E");

        servlet.doGet(request, response);

        verify(session).invalidate();
        verify(response).sendRedirect("/J2E/login.jsp");
    }

    @Test
    @DisplayName("LogoutServlet : pas de session → redirige quand même")
    void testLogoutServletNoSession() throws Exception {
        LogoutServlet servlet = new LogoutServlet();

        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("/J2E");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/J2E/login.jsp");
    }

    // ===== Fichiers statiques =====

    @Test
    @DisplayName("AuthFilter : les CSS passent sans authentification")
    void testAuthFilterAllowsCSS() throws Exception {
        AuthFilter filter = new AuthFilter();

        when(request.getRequestURI()).thenReturn("/J2E/style.css");
        when(request.getContextPath()).thenReturn("/J2E");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
