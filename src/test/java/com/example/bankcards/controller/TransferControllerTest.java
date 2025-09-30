package com.example.bankcards.controller;

import com.example.bankcards.dto.transfer.TransferInput;
import com.example.bankcards.dto.transfer.TransferResponse;
import com.example.bankcards.security.CurrentUserIdArgumentResolver;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.TransferService;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TransferController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransferService transferService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CurrentUserIdArgumentResolver currentUserIdArgumentResolver;

    private final TransferResponse response =
            new TransferResponse(1L, 10L, 20L, new BigDecimal("100.00"), LocalDateTime.now());

    @BeforeEach
    void setup() {
        Mockito.when(currentUserIdArgumentResolver.supportsParameter(Mockito.any()))
                .thenAnswer(invocation -> {
                    var param = invocation.getArgument(0, org.springframework.core.MethodParameter.class);
                    return param.hasParameterAnnotation(com.example.bankcards.security.CurrentUserId.class);
                });

        Mockito.when(currentUserIdArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .thenReturn(1L);
    }

    @Test
    void transferBetweenOwnCards_ShouldReturnSuccess() throws Exception {
        TransferInput input = new TransferInput(10L, 20L, new BigDecimal("100.00"));
        Mockito.when(transferService.transferBetweenOwnCards(eq(1L), any(TransferInput.class)))
                .thenReturn(response);

        mockMvc.perform(post("/transfers/own")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.amount").value(100.00));
    }

    @Test
    void getTransfersByUser_ShouldReturnPage() throws Exception {
        Mockito.when(transferService.getTransfersByUser(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/transfers/history/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].amount").value(100.00));
    }

    @Test
    void getTransfersByCard_ShouldReturnPage() throws Exception {
        Long cardId = 10L;

        Mockito.when(transferService.getTransfersByCard(eq(cardId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/transfers/history/card")
                        .param("cardId", String.valueOf(cardId))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].amount").value(100.00));
    }
}