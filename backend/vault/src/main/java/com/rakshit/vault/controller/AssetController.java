package com.rakshit.vault.controller;

import com.rakshit.vault.dto.AssetRequest;
import com.rakshit.vault.dto.AssetResponse;
import com.rakshit.vault.service.AssetService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @GetMapping
    public List<AssetResponse> listAssets(@RequestParam(required = false) String ownerUserId) {
        return assetService.listAssets(ownerUserId);
    }

    @GetMapping("/{id}")
    public AssetResponse getAsset(@PathVariable String id) {
        return assetService.getAsset(id);
    }

    @PostMapping
    public ResponseEntity<AssetResponse> createAsset(@Valid @RequestBody AssetRequest request) {
        AssetResponse response = assetService.createAsset(request);
        return ResponseEntity
                .created(URI.create("/api/assets/" + response.id()))
                .body(response);
    }

    @PutMapping("/{id}")
    public AssetResponse updateAsset(
            @PathVariable String id,
            @Valid @RequestBody AssetRequest request) {
        return assetService.updateAsset(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsset(@PathVariable String id) {
        assetService.deleteAsset(id);
        return ResponseEntity.noContent().build();
    }
}
