package com.example.bankcards.controller;

import com.example.bankcards.dto.ApiResult;
import com.example.bankcards.dto.transfer.TransferInput;
import com.example.bankcards.dto.transfer.TransferResponse;
import com.example.bankcards.security.CurrentUserId;
import com.example.bankcards.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Переводы", description = "Управление переводами между картами")
@RestController
@RequestMapping("/transfers")
@SecurityRequirement(name = "bearerAuth")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @Operation(summary = "Перевод между своими картами")
    @PostMapping("/own")
    @PreAuthorize("hasRole('USER')")
    public ApiResult<TransferResponse> transferBetweenOwnCards(
            @Parameter(hidden = true) @CurrentUserId Long userId,
            @Parameter(description = "Данные перевода") @RequestBody TransferInput request
    ) {
        TransferResponse transfer = transferService.transferBetweenOwnCards(userId, request);
        return new ApiResult.Success<>(transfer);
    }

    @Operation(summary = "История переводов пользователя")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/history/user")
    public ApiResult<Page<TransferResponse>> getTransfersByUser(
            @Parameter(hidden = true) @CurrentUserId Long userId,
            @ParameterObject Pageable pageable
    ) {
        return new ApiResult.Success<>(transferService.getTransfersByUser(userId, pageable));
    }

    @Operation(summary = "История переводов по карте")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/history/card")
    public ApiResult<Page<TransferResponse>> getTransfersByCard(
            @Parameter(description = "ID карты")
            @RequestParam(name = "cardId") Long cardId,
            @ParameterObject Pageable pageable
    ) {
        return new ApiResult.Success<>(transferService.getTransfersByCard(cardId, pageable));
    }
}
