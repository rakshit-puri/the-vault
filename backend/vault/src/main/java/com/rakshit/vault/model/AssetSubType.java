package com.rakshit.vault.model;

import java.util.Set;

public enum AssetSubType {
    // IDENTITY Subtypes
    AADHAAR_CARD(AssetType.IDENTITY),
    PASSPORT(AssetType.IDENTITY),
    VOTER_ID(AssetType.IDENTITY),
    DRIVING_LICENSE(AssetType.IDENTITY),
    PAN_CARD(AssetType.IDENTITY),
    // BANK_ACCOUNT Subtypes
    SAVINGS_ACCOUNT(AssetType.BANK_ACCOUNT),
    CURRENT_ACCOUNT(AssetType.BANK_ACCOUNT),
    SALARY_ACCOUNT(AssetType.BANK_ACCOUNT),
    BANK_LOCKER(AssetType.BANK_ACCOUNT),
    // INSURANCE Subtypes
    LIFE_INSURANCE(AssetType.INSURANCE),
    TERM_INSURANCE(AssetType.INSURANCE),
    HEALTH_INSURANCE(AssetType.INSURANCE),
    VEHICLE_INSURANCE(AssetType.INSURANCE),
    TRAVEL_INSURANCE(AssetType.INSURANCE),
    // PHYSICAL_ASSET Subtypes
    PROPERTY_DOCUMENT(AssetType.PHYSICAL_ASSET),
    VEHICLE_DOCUMENT(AssetType.PHYSICAL_ASSET),
    JEWELLERY(AssetType.PHYSICAL_ASSET),
    COMMODITY(AssetType.PHYSICAL_ASSET),
    FIXED_DEPOSIT(AssetType.DEPOSIT),
    RECURRING_DEPOSIT(AssetType.DEPOSIT),
    MUTUAL_FUND(AssetType.INVESTMENT),
    STOCK(AssetType.INVESTMENT),
    PPF(AssetType.INVESTMENT),
    OTHER(AssetType.OTHER);

    private final Set<AssetType> assetTypes;

    AssetSubType(AssetType... assetTypes) {
        this.assetTypes = Set.of(assetTypes);
    }

    public boolean isValidFor(AssetType assetType) {
        return assetTypes.contains(assetType);
    }
}
