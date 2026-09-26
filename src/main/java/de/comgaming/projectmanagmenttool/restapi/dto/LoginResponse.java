package de.comgaming.projectmanagmenttool.restapi.dto;

public record LoginResponse(
        String token,
        AccountResponse account
) {
}
