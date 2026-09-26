package de.comgaming.projectmanagmenttool.restapi.dto;

public record LoginRequest(
        String username,
        String password
) {
}
