package com.rakshit.vault.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rakshit.vault.dto.AssetRequest;
import com.rakshit.vault.dto.AssetResponse;
import com.rakshit.vault.model.AssetSubType;
import com.rakshit.vault.model.AssetType;
import com.rakshit.vault.service.AssetService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AssetController.class)
class AssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AssetService assetService;

    @Test
    void listsAssetsForOwner() throws Exception {
        when(assetService.listAssets("user-1")).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/assets").param("ownerUserId", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("asset-1"))
                .andExpect(jsonPath("$[0].title").value("HDFC Account"));
    }

    @Test
    void createsAsset() throws Exception {
        when(assetService.createAsset(any(AssetRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/assets/asset-1"))
                .andExpect(jsonPath("$.id").value("asset-1"))
                .andExpect(jsonPath("$.assetType").value("BANK_ACCOUNT"));
    }

    @Test
    void rejectsInvalidSubtypeForAssetType() throws Exception {
        Map<String, Object> request = Map.of(
                "ownerUserId", "user-1",
                "assetType", "BANK_ACCOUNT",
                "subType", "LIFE_INSURANCE",
                "title", "Wrong subtype",
                "data", Map.of());

        mockMvc.perform(post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verifyNoInteractions(assetService);
    }

    @Test
    void updatesAsset() throws Exception {
        when(assetService.updateAsset(eq("asset-1"), any(AssetRequest.class))).thenReturn(response());

        mockMvc.perform(put("/api/assets/asset-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("asset-1"));
    }

    @Test
    void deletesAsset() throws Exception {
        mockMvc.perform(delete("/api/assets/asset-1"))
                .andExpect(status().isNoContent());

        verify(assetService).deleteAsset("asset-1");
    }

    private AssetRequest request() {
        return new AssetRequest(
                "user-1",
                AssetType.BANK_ACCOUNT,
                AssetSubType.SAVINGS_ACCOUNT,
                "HDFC Account",
                "Primary savings account",
                Map.of("bankName", "HDFC Bank"));
    }

    private AssetResponse response() {
        Instant now = Instant.parse("2026-05-02T00:00:00Z");
        return new AssetResponse(
                "asset-1",
                "user-1",
                AssetType.BANK_ACCOUNT,
                AssetSubType.SAVINGS_ACCOUNT,
                "HDFC Account",
                "Primary savings account",
                Map.of("bankName", "HDFC Bank"),
                1,
                now,
                now);
    }
}
