package de.comgaming.projectmanagmenttool.restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import de.comgaming.projectmanagmenttool.restapi.dto.*;
import de.comgaming.projectmanagmenttool.usermanagment.Account;
import de.comgaming.projectmanagmenttool.usermanagment.AccountManager;
import de.comgaming.projectmanagmenttool.usermanagment.PermissionManager;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class APIHandler implements HttpHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    private final AccountManager accounts = new AccountManager();
    private final PermissionManager permissions = new PermissionManager();
    private final JwtManager jwt = new JwtManager();

    @Override
    public void handle(HttpExchange e) throws IOException {
        try {
            cors(e);

            String method = e.getRequestMethod();
            String path = e.getRequestURI().getPath();

            if (method.equals("OPTIONS")) {
                empty(e, 204);
                return;
            }

            if (path.equals("/api/auth/login")) {
                if (!method.equals("POST")) {
                    methodNotAllowed(e);
                    return;
                }

                login(e);
                return;
            }

            if (!path.startsWith("/api/accounts")) {
                error(e, 404, "Endpoint nicht gefunden.");
                return;
            }

            if (!auth(e))
                return;

            accountRoute(e, method, path);

        } catch (Exception ex) {
            ex.printStackTrace();
            error(e, 500, "Interner Serverfehler.");
        }
    }

    private void login(HttpExchange e) throws IOException {
        LoginRequest request = read(e, LoginRequest.class);

        if (request.username() == null ||
                request.username().isBlank() ||
                request.password() == null ||
                request.password().isBlank()) {

            error(e, 400, "Username und Passwort sind erforderlich.");
            return;
        }

        Optional<Account> result =
                accounts.login(request.username(), request.password());

        if (result.isEmpty()) {
            error(e, 401, "Ungültige Zugangsdaten.");
            return;
        }

        Account account = result.get();

        json(e, 200, new LoginResponse(
                jwt.createToken(
                        account.getId(),
                        account.getUsername()),
                AccountResponse.from(account)
        ));
    }

    private void accountRoute(
            HttpExchange e,
            String method,
            String path
    ) throws IOException {

        if (path.equals("/api/accounts")) {

            if (method.equals("GET")) {
                if (!requirePermission(e, "accounts.view"))
                    return;

                List<AccountResponse> result = accounts.findAll()
                        .stream()
                        .map(AccountResponse::from)
                        .toList();

                json(e, 200, result);
                return;
            }

            methodNotAllowed(e);
            return;
        }

        String remaining =
                path.substring("/api/accounts/".length());

        if (remaining.equals("me")) {
            if (!method.equals("GET")) {
                methodNotAllowed(e);
                return;
            }

            json(e, 200,
                    AccountResponse.from(authenticated(e)));
            return;
        }

        String[] parts = remaining.split("/");

        if (parts.length == 0 || parts[0].isBlank()) {
            error(e, 404, "Account nicht gefunden.");
            return;
        }

        long id;

        try {
            id = Long.parseLong(parts[0]);
        } catch (NumberFormatException ex) {
            error(e, 400, "Ungültige AccountID.");
            return;
        }

        if (parts.length == 1 && method.equals("GET")) {
            if (!requirePermission(e, "accounts.view"))
                return;

            getAccount(e, id);
            return;
        }

        if (parts.length == 1 && method.equals("DELETE")) {
            if (!requirePermission(e, "accounts.delete"))
                return;

            deleteAccount(e, id);
            return;
        }

        error(e, 404, "Endpoint nicht gefunden.");
    }

    private void getAccount(HttpExchange e, long id)
            throws IOException {

        Optional<Account> account = accounts.findById(id);

        if (account.isEmpty()) {
            error(e, 404, "Account nicht gefunden.");
            return;
        }

        json(e, 200, AccountResponse.from(account.get()));
    }

    private void deleteAccount(HttpExchange e, long id)
            throws IOException {

        try {
            if (!accounts.deleteById(id)) {
                error(e, 404, "Account nicht gefunden.");
                return;
            }

            empty(e, 204);

        } catch (IllegalArgumentException ex) {
            error(e, 400, ex.getMessage());
        }
    }

    private boolean auth(HttpExchange e) throws IOException {
        String header =
                e.getRequestHeaders().getFirst("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            error(e, 401, "Authorization Token fehlt.");
            return false;
        }

        String token = header.substring(7).trim();

        if (token.isBlank()) {
            error(e, 401, "Authorization Token fehlt.");
            return false;
        }

        Long id = jwt.getAccountId(token);

        if (id == null) {
            error(e, 401,
                    "Ungültiger oder abgelaufener Token.");
            return false;
        }

        Optional<Account> account = accounts.findById(id);

        if (account.isEmpty()) {
            error(e, 401, "Account existiert nicht mehr.");
            return false;
        }

        if (!account.get().isActive()) {
            error(e, 403, "Account ist deaktiviert.");
            return false;
        }

        e.setAttribute("account", account.get());
        return true;
    }

    private boolean requirePermission(
            HttpExchange e,
            String permission
    ) throws IOException {

        Account account = authenticated(e);

        if (account == null) {
            error(e, 401, "Nicht authentifiziert.");
            return false;
        }

        if (!permissions.hasPermission(
                account.getGroupid(),
                permission)) {

            error(e, 403, "Keine Berechtigung.");
            return false;
        }

        return true;
    }

    private Account authenticated(HttpExchange e) {
        return (Account) e.getAttribute("account");
    }

    private <T> T read(
            HttpExchange e,
            Class<T> type
    ) throws IOException {
        return mapper.readValue(
                e.getRequestBody(), type);
    }

    private void json(
            HttpExchange e,
            int status,
            Object object
    ) throws IOException {

        byte[] data = mapper.writeValueAsBytes(object);

        e.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8");

        e.sendResponseHeaders(status, data.length);

        try (var out = e.getResponseBody()) {
            out.write(data);
        }
    }

    private void error(
            HttpExchange e,
            int status,
            String message
    ) throws IOException {
        json(e, status,
                new ErrorResponse(status, message));
    }

    private void empty(
            HttpExchange e,
            int status
    ) throws IOException {
        e.sendResponseHeaders(status, -1);
        e.close();
    }

    private void methodNotAllowed(
            HttpExchange e
    ) throws IOException {
        error(e, 405,
                "HTTP-Methode nicht erlaubt.");
    }

    private void cors(HttpExchange e) {
        e.getResponseHeaders().set(
                "Access-Control-Allow-Origin", "*");

        e.getResponseHeaders().set(
                "Access-Control-Allow-Methods",
                "GET, POST, PUT, PATCH, DELETE, OPTIONS");

        e.getResponseHeaders().set(
                "Access-Control-Allow-Headers",
                "Content-Type, Authorization");
    }

    private record ErrorResponse(
            int status,
            String message
    ) {}
}
