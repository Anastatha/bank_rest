package com.example.bankcards.controller;

import com.example.bankcards.dto.card.DepositInput;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.security.OptionalUserIdArgumentResolver;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CardController.class)
@AutoConfigureMockMvc(addFilters = false)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CardService cardService;

    @MockBean
    private OptionalUserIdArgumentResolver optionalUserIdArgumentResolver;

    private final Card card = new Card();

    @BeforeEach
    void setup() {
        var user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        card.setId(1L);
        card.setUser(user);

        Mockito.when(optionalUserIdArgumentResolver.supportsParameter(Mockito.any()))
                .thenAnswer(invocation -> {
                    var param = invocation.getArgument(0, org.springframework.core.MethodParameter.class);
                    return param.hasParameterAnnotation(com.example.bankcards.security.OptionalUserId.class);
                });
        Mockito.when(optionalUserIdArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .thenReturn(1L);
    }

    @Test
    void createCard_ShouldReturnCard() throws Exception {
        when(cardService.createCardForUser(anyLong())).thenReturn(card);
        when(cardService.getMaskedNumber(card)).thenReturn("**** **** **** 1234");

        mockMvc.perform(post("/cards/create").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.maskedNumber").value("**** **** **** 1234"));
    }

    @Test
    void getCards_ShouldReturnPage() throws Exception {
        when(cardService.findCardsByUserId(anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card)));
        when(cardService.getMaskedNumber(card)).thenReturn("**** **** **** 1234");

        mockMvc.perform(get("/cards/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    void getCardById_ShouldReturnCard() throws Exception {
        when(cardService.getCardByIdForUser(anyLong(), anyLong())).thenReturn(card);
        when(cardService.getMaskedNumber(card)).thenReturn("**** **** **** 1234");

        mockMvc.perform(get("/cards").param("cardId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void requestBlock_ShouldReturnCard() throws Exception {
        when(cardService.requestBlockCard(anyLong(), anyLong())).thenReturn(card);
        when(cardService.getMaskedNumber(card)).thenReturn("**** **** **** 1234");

        mockMvc.perform(post("/cards/block/request").param("cardId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void approveBlock_ShouldReturnCard() throws Exception {
        when(cardService.approveBlockCard(anyLong())).thenReturn(card);
        when(cardService.getMaskedNumber(card)).thenReturn("**** **** **** 1234");

        mockMvc.perform(post("/cards/block/approve").param("cardId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void deleteCard_ShouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(delete("/cards").param("cardId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Card deleted successfully"));
    }

    @Test
    void getAllCards_ShouldReturnPage() throws Exception {
        when(cardService.getAllCards(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card)));
        when(cardService.getMaskedNumber(card)).thenReturn("**** **** **** 1234");

        mockMvc.perform(get("/cards/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    void depositToCard_ShouldReturnCard() throws Exception {
        DepositInput request = new DepositInput(BigDecimal.valueOf(100));
        when(cardService.depositToCard(anyLong(), anyLong(), any(BigDecimal.class)))
                .thenReturn(card);
        when(cardService.getMaskedNumber(card)).thenReturn("**** **** **** 1234");

        mockMvc.perform(post("/cards/deposit")
                        .param("cardId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.maskedNumber").value("**** **** **** 1234"));
    }
}
