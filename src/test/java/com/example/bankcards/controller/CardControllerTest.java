package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateDto;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardService cardService;

    private CardDto cardDto;
    private CardCreateDto cardCreateDto;

    @BeforeEach
    void setUp() {
        cardDto = CardDto.builder()
                .id(1L)
                .maskedCardNumber("**** **** **** 1234")
                .cardHolder("TEST USER")
                .expiryDate(LocalDate.now().plusYears(3))
                .status(Card.CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .ownerId(1L)
                .ownerUsername("testuser")
                .build();

        cardCreateDto = CardCreateDto.builder()
                .cardHolder("Test User")
                .expiryDate(LocalDate.now().plusYears(3))
                .initialBalance(BigDecimal.valueOf(500))
                .ownerId(1L)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_AsAdmin_Success() throws Exception {
        // Arrange
        when(cardService.createCard(any(CardCreateDto.class))).thenReturn(cardDto);

        // Act & Assert
        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.maskedCardNumber").value("**** **** **** 1234"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createCard_AsUser_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardCreateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCard_Success() throws Exception {
        // Arrange
        when(cardService.getCardById(1L)).thenReturn(cardDto);

        // Act & Assert
        mockMvc.perform(get("/api/cards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCards_AsAdmin_Success() throws Exception {
        // Arrange
        Page<CardDto> cardPage = new PageImpl<>(List.of(cardDto));
        when(cardService.getAllCards(any(PageRequest.class))).thenReturn(cardPage);

        // Act & Assert
        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockCard_Success() throws Exception {
        // Arrange
        cardDto.setStatus(Card.CardStatus.BLOCKED);
        when(cardService.blockCard(1L)).thenReturn(cardDto);

        // Act & Assert
        mockMvc.perform(put("/api/cards/1/block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/cards/1"))
                .andExpect(status().isNoContent());
    }
}
