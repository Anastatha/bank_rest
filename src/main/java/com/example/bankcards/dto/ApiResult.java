package com.example.bankcards.dto;

public sealed interface ApiResult<T> permits ApiResult.Success, ApiResult.Error {
    record Success<T>(T data) implements ApiResult<T> {}
    record Error<T>(String message) implements ApiResult<T> {}
}

