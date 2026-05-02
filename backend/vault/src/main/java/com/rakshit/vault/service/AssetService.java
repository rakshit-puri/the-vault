package com.rakshit.vault.service;

import com.rakshit.vault.dto.AssetRequest;
import com.rakshit.vault.dto.AssetResponse;
import com.rakshit.vault.exception.AssetNotFoundException;
import com.rakshit.vault.model.Asset;
import com.rakshit.vault.repository.AssetRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;

    public List<AssetResponse> listAssets(String ownerUserId) {
        List<Asset> assets = ownerUserId == null || ownerUserId.isBlank()
                ? assetRepository.findAll()
                : assetRepository.findByOwnerUserIdOrderByUpdatedAtDesc(ownerUserId);

        return assets.stream()
                .map(AssetResponse::from)
                .toList();
    }

    public AssetResponse getAsset(String id) {
        return AssetResponse.from(findAsset(id));
    }

    public AssetResponse createAsset(AssetRequest request) {
        Asset asset = new Asset();
        applyRequest(asset, request);
        asset.setSchemaVersion(1);
        return AssetResponse.from(assetRepository.save(asset));
    }

    public AssetResponse updateAsset(String id, AssetRequest request) {
        Asset asset = findAsset(id);
        applyRequest(asset, request);
        return AssetResponse.from(assetRepository.save(asset));
    }

    public void deleteAsset(String id) {
        assetRepository.delete(findAsset(id));
    }

    private Asset findAsset(String id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new AssetNotFoundException(id));
    }

    private void applyRequest(Asset asset, AssetRequest request) {
        asset.setOwnerUserId(request.ownerUserId());
        asset.setAssetType(request.assetType());
        asset.setSubType(request.subType());
        asset.setTitle(request.title());
        asset.setDescription(request.description());
        asset.setData(request.safeData());
    }
}
