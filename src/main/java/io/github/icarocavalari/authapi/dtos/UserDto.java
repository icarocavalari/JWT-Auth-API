package io.github.icarocavalari.authapi.dtos;

import java.util.Date;

public record UserDto(Integer id, String username, String fullName, Date createdAt) {}