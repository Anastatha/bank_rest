package com.example.bankcards.controller;

import com.example.bankcards.dto.ApiResult;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.DepositInput;
import com.example.bankcards.entity.Card;
import com.example.bankcards.security.CurrentUserId;
import com.example.bankcards.security.OptionalUserId;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springdoc.core.annotations.ParameterObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Карты", description = "Управление банковскими картами")
@RestController
@RequestMapping("/cards")
@SecurityRequirement(name = "bearerAuth")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @Operation(summary = "Создание карты для пользователя (админ)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ApiResult<CardResponse> createCard(
            @Parameter(
                    description = "ID пользователя, для которого создаем карту",
                    required = true
            )
            @RequestParam(name = "id") Long userId) {
        Card card = cardService.createCardForUser(userId);
        String masked = cardService.getMaskedNumber(card);
        return new ApiResult.Success<>(CardResponse.fromEntity(card, masked));
    }

    @Operation(
            summary = "Получение карт пользователя",
            description = "Для USER: возвращает свои карты. Для ADMIN: может указать userId для просмотра карт другого пользователя",
            parameters = {
                    @Parameter(
                            name = "userId",
                            description = "ID пользователя (только для ADMIN, опционально)",
                            required = false
                    ),
                    @Parameter(
                            name = "number",
                            description = "Фильтр по номеру карты (необязательно)",
                            required = false
                    )
            }
    )
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/user")
    public ApiResult<Page<CardResponse>> getCards(
            @Parameter(hidden = true) @OptionalUserId Long userId,
            @RequestParam(name = "number", required = false) String number,
            @ParameterObject Pageable pageable) {
        Page<Card> cards = (number == null || number.isEmpty())
                ? cardService.findCardsByUserId(userId, pageable)
                : cardService.findCardsByUserIdAndNumber(userId, number, pageable);

        Page<CardResponse> response = cards.map(c ->
                CardResponse.fromEntity(c, cardService.getMaskedNumber(c))
        );

        return new ApiResult.Success<>(response);
    }

    @Operation(
            summary = "Получение конкретной карты пользователя",
            description = "Для USER: возвращает свою карту. Для ADMIN: может указать userId для просмотра карты другого пользователя",
            parameters = {
                    @Parameter(
                            name = "cardId",
                            description = "ID карты",
                            required = true
                    ),
                    @Parameter(
                            name = "userId",
                            description = "ID пользователя (только для ADMIN, опционально)",
                            required = false
                    )
            }
    )
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping
    public ApiResult<CardResponse> getCardById(
            @Parameter(hidden = true) @OptionalUserId Long userId,
            @RequestParam(name = "cardId") Long cardId) {
        Card card = cardService.getCardByIdForUser(cardId, userId);
        String masked = cardService.getMaskedNumber(card);
        return new ApiResult.Success<>(CardResponse.fromEntity(card, masked));
    }

    @Operation(
            summary = "Запрос на блокировку карты пользователем",
            description = "USER может запросить блокировку только своей карты"
    )
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/block/request")
    public ApiResult<CardResponse> requestBlock(
            @Parameter(description = "ID карты")
            @RequestParam(name = "cardId") Long cardId,
            @Parameter(hidden = true) @CurrentUserId Long userId) {
        Card card = cardService.requestBlockCard(userId, cardId);
        String masked = cardService.getMaskedNumber(card);
        return new ApiResult.Success<>(CardResponse.fromEntity(card, masked));
    }

    @Operation(summary = "Подтверждение блокировки карты админом")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/block/approve")
    public ApiResult<CardResponse> approveBlock(
            @Parameter(description = "ID карты") @RequestParam(name = "cardId") Long cardId) {
        Card card = cardService.approveBlockCard(cardId);
        String masked = cardService.getMaskedNumber(card);
        return new ApiResult.Success<>(CardResponse.fromEntity(card, masked));
    }

    @Operation(summary = "Удаление карты (админ)")
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<String> deleteCard(@Parameter(description = "ID карты") @RequestParam(name = "cardId") Long cardId) {
        cardService.deleteCard(cardId);
        return new ApiResult.Success<>("Card deleted successfully");
    }

    @Operation(summary = "Получение всех карт (админ)")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<Page<CardResponse>> getAllCards(@ParameterObject Pageable pageable) {
        Page<CardResponse> response = cardService.getAllCards(pageable)
                .map(c -> CardResponse.fromEntity(c, cardService.getMaskedNumber(c)));
        return new ApiResult.Success<>(response);
    }

    @Operation(summary = "Пополнение конкретной карты пользователем")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/deposit")
    public ApiResult<CardResponse> depositToCard(
            @Parameter(description = "ID карты") @RequestParam(name = "cardId") Long cardId,
            @Parameter(hidden = true) @CurrentUserId Long userId,
            @Parameter(description = "Сумма пополнения") @RequestBody DepositInput request) {

        Card card = cardService.depositToCard(userId, cardId, request.amount());
        String masked = cardService.getMaskedNumber(card);
        return new ApiResult.Success<>(CardResponse.fromEntity(card, masked));
    }
}

